package io.harness.cfsdk;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;
import static io.harness.cfsdk.cloud.network.NetworkStatus.CONNECTED;
import static io.harness.cfsdk.utils.CfUtils.EvaluationUtil.areEvaluationsValid;

import android.content.Context;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.DefaultCache;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProvider;
import io.harness.cfsdk.cloud.openapi.client.ApiClient;
import io.harness.cfsdk.cloud.openapi.client.ApiException;
import io.harness.cfsdk.cloud.openapi.client.api.ClientApi;
import io.harness.cfsdk.cloud.openapi.client.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.openapi.client.model.AuthenticationRequestTarget;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;
import io.harness.cfsdk.cloud.sse.EventSource;
import io.harness.cfsdk.cloud.sse.EventsListener;
import io.harness.cfsdk.cloud.sse.StatusEvent;
import io.harness.cfsdk.common.SdkCodes;
import io.harness.cfsdk.utils.TlsUtils;

class SdkThread implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(SdkThread.class);

    private static final String HARNESS_SDK_INFO = String.format("Android %s Client", AndroidSdkVersion.ANDROID_SDK_VERSION);
    private final Duration minimumRefreshIntervalSecs = Duration.ofSeconds(60);
    private final Context context;
    private final CountDownLatch initLatch = new CountDownLatch(1);
    private final CfConfiguration config;
    private final String apiKey;
    private final Target target;
    private final CloudCache cache;
    private final Executor callbackExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, Set<EvaluationListener>> evaluationListenerMap;
    private final Set<EventsListener> eventsListenerSet;
    private final NetworkInfoProvider network;

    /* ---- Mutable state ---- */
    private ClientApi api;
    private String bearerToken;
    private AuthInfo authInfo;
    private boolean sseRescheduled = false;
    private Instant lastPollTime = Instant.EPOCH;

    SdkThread(Context context, String apiKey, CfConfiguration config, Target target, Map<String, Set<EvaluationListener>> evaluationListenerMap, Set<EventsListener> eventsListenerSet)  {
        this.context = context;
        this.apiKey = apiKey;
        this.config = config;
        this.target = target;
        this.cache = (config.getCache() != null) ? config.getCache() : new DefaultCache(context, target.getIdentifier(), apiKey);
        this.callbackExecutor.execute(() -> Thread.currentThread().setName("CallbackThread"));
        this.evaluationListenerMap = evaluationListenerMap;
        this.eventsListenerSet = eventsListenerSet;
        this.network = new NetworkInfoProvider(context);
    }

    void mainSdkThread(ClientApi api) throws ApiException {
        /* For SDK v2 we have three states the SDK can be in:
               - authenticating()
               - streaming()
               - polling()
           Each of these methods block the sdkThread.
           Any spurious exceptions (e.g. socket timeout) will be caught by the root exception
           handler in run() and this method will be restarted. */

        final AuthInfo authInfo = authenticating(api, apiKey, target);
        if (authInfo == null) {
            throw new RuntimeException("Authentication failed");
        }

        boolean fallbackToPolling;
        try {
            fallbackToPolling = !streaming(api, authInfo);
        } catch (Throwable ex) {
            fallbackToPolling = true;
        }

        if (fallbackToPolling) {
            log.debug("SSE stream {}, falling back to polling mode", config.isStreamEnabled() ? "failed" : "disabled");

            try {
                int pollDelayInSeconds = Math.max(config.getPollingInterval(), 60);
                SdkCodes.infoPollStarted(pollDelayInSeconds);

                polling(api, authInfo, pollDelayInSeconds);

            } catch (Throwable ex) {
                logExceptionAndWarn("Polling failed", ex);
            } finally {
                SdkCodes.infoPollingStopped();
            }
        }

        if (networkUnavailable()) {
            throw new NetworkOffline();
        }
    }

    AuthInfo authenticating(ClientApi api, String apiKey, Target target) throws ApiException {

        if (networkUnavailable()) {
            log.info("Will not auth, network offline");
            throw new NetworkOffline();
        }

        final AuthenticationRequestTarget authTarget = new AuthenticationRequestTarget();
        authTarget.identifier(target.getIdentifier());
        authTarget.attributes(target.getAttributes());
        authTarget.setName(target.getName());
        authTarget.setAnonymous(false);

        final AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.apiKey(apiKey);
        authRequest.setTarget(authTarget);
        bearerToken = api.authenticate(authRequest).getAuthToken();

        addHeader(api, "Authorization", "Bearer " + bearerToken);

        final AuthInfo ai = new AuthResponseDecoder().extractInfo(bearerToken);
        if (ai != null) {
            authInfo = ai;

            addHeader(api, "Harness-EnvironmentID", authInfo.getEnvironmentTrackingHeader());
            addHeader(api, "Harness-AccountID", authInfo.getAccountID());

            pollOnce(api, authInfo);

            SdkCodes.infoSdkAuthOk();
            initLatch.countDown();
        }

        return authInfo;
    }

    boolean streaming(ClientApi api, AuthInfo authInfo) throws ApiException, InterruptedException {
        if (networkUnavailable()) {
            throw new NetworkOffline();
        }

        if (!config.isStreamEnabled()) {
            return false;
        }

        final CountDownLatch endStreamLatch = new CountDownLatch(1);

        pollOnce(api, authInfo);

        final EventsListener eventsListener = statusEvent -> {
            log.debug("SSE event received: {}", statusEvent.getEventType());

            try {
                switch (statusEvent.getEventType()) {
                    case SSE_START:
                        streamSseStart();
                        break;

                    case SSE_RESUME:
                        streamSseResume(api, authInfo);
                        break;

                    case SSE_END:
                        streamSseEnd(api, authInfo);
                        endStreamLatch.countDown();
                        break;

                    case EVALUATION_CHANGE:
                        streamSseEvaluationChange(api, authInfo, statusEvent);
                        break;

                    case EVALUATION_REMOVE:
                        streamSseEvaluationRemove(api, authInfo, statusEvent);
                        break;

                    case EVALUATION_RELOAD:
                        streamSseEvaluationReload(api, authInfo, statusEvent);
                        break;
                }
            } catch (Throwable ex) {
                if (ex instanceof NetworkOffline) {
                    log.info("SSE network went offline");
                } else {
                    logExceptionAndWarn("Exception in event handler", ex);
                }
                endStreamLatch.countDown();
            }
        };

        final String streamUrl = config.getStreamURL() + "?cluster=" + authInfo.getCluster();
        try (EventSource eventSource = new EventSource(streamUrl, makeHeadersFrom(bearerToken, apiKey, authInfo), eventsListener, 1, config.getTlsTrustedCAs())) {
            eventSource.start(sseRescheduled);

            endStreamLatch.await();
        } finally {
            sseRescheduled = true;
        }

        return true;
    }

    void streamSseStart() {
        final StatusEvent startEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START);
        sendEvent(startEvent);
    }

    void streamSseResume(ClientApi api, AuthInfo authInfo) throws ApiException {
        log.debug("SSE connection resumed, reloading all evaluations");
        final List<Evaluation> resumedEvaluations = pollOnce(api, authInfo);
        final StatusEvent resumeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_RESUME, resumedEvaluations);
        sendEvent(resumeEvent);
    }

    void streamSseEvaluationChange(ClientApi api, AuthInfo authInfo, StatusEvent statusEvent) throws ApiException {
        final List<Evaluation> changeEvaluations = statusEvent.extractEvaluationListPayload();

        // if evaluations are present in sse event save it directly, else fetch from server
        if (areEvaluationsValid(changeEvaluations)) {
            for (int i = 0; i < changeEvaluations.size(); i++) {
                repoSetEvaluation(authInfo.getEnvironmentIdentifier(), changeEvaluations.get(i).getFlag(), changeEvaluations.get(i));
                final StatusEvent preEvalChangeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, changeEvaluations.get(i));
                sendEvent(preEvalChangeEvent);
                notifyListeners(changeEvaluations.get(i));
            }
        } else {
            for (int i = 0; i < changeEvaluations.size(); i++) {
                final Evaluation evaluation = api.getEvaluationByIdentifier(
                        authInfo.getEnvironment(),
                        changeEvaluations.get(i).getFlag(),
                        target.getIdentifier(),
                        authInfo.getCluster()
                );

                if (evaluation != null) {
                    repoSetEvaluation(authInfo.getEnvironmentIdentifier(), evaluation.getFlag(), evaluation);
                    final StatusEvent evalChangeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluation);
                    sendEvent(evalChangeEvent);
                    notifyListeners(evaluation);
                } else {
                    log.warn("EVALUATION_CHANGE event failed to get evaluation for target '{}' from server", target.getIdentifier());
                }
            }
        }
    }

    void streamSseEvaluationRemove(ClientApi api, AuthInfo authInfo, StatusEvent statusEvent) {
        final Evaluation eval = statusEvent.extractEvaluationPayload();
        repoRemoveEvaluation(authInfo.getEnvironmentIdentifier(), target.getIdentifier());
        final StatusEvent evalRemoveEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, eval);
        sendEvent(evalRemoveEvent);
        notifyListeners(eval);
    }

    void streamSseEvaluationReload(ClientApi api, AuthInfo authInfo, StatusEvent statusEvent) throws ApiException {
        // if evaluations are present in sse event save it directly, else fetch from server
        if (areEvaluationsValid(statusEvent.extractEvaluationListPayload())) {
            final List<Evaluation> reloadEvaluations = statusEvent.extractEvaluationListPayload();
            for (int i = 0; i < reloadEvaluations.size(); i++) {
                repoSetEvaluation(authInfo.getEnvironmentIdentifier(), reloadEvaluations.get(i).getFlag(), reloadEvaluations.get(i));
                notifyListeners(reloadEvaluations.get(i));
            }
            final StatusEvent preEvalReloadEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, reloadEvaluations);
            sendEvent(preEvalReloadEvent);

        } else {
            log.debug("Reloading all evaluations");
            final List<Evaluation> fetchedEvaluations = pollOnce(api, authInfo);
            for (int i = 0; i < fetchedEvaluations.size(); i++) {
                notifyListeners(fetchedEvaluations.get(i));
            }

            final StatusEvent evalReloadEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, fetchedEvaluations);
            sendEvent(evalReloadEvent);
        }
    }

    void streamSseEnd(ClientApi api, AuthInfo authInfo) throws ApiException {
        pollOnce(api, authInfo);
        final StatusEvent endEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END);
        sendEvent(endEvent);
    }

    void polling(ClientApi api, AuthInfo authInfo, int pollDelayInSeconds) throws ApiException, InterruptedException {
        do {
            SECONDS.sleep(pollDelayInSeconds);

            pollOnce(api, authInfo);
        } while (true);
    }

    List<Evaluation> pollOnce(ClientApi api, AuthInfo authInfo) throws ApiException {
        if (networkUnavailable()) {
            throw new NetworkOffline();
        }

        final List<Evaluation> evaluations = api.getEvaluations(authInfo.getEnvironment(), target.getIdentifier(), authInfo.getCluster());

        for (Evaluation eval : evaluations) {
            repoSetEvaluation(authInfo.getEnvironmentIdentifier(), eval.getFlag(), eval);
        }

        // Notify users that evaluations have been reloaded. This happens first on client init,
        // then if polling is enabled, on each interval.
        sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations));
        return evaluations;
    }

    public void refreshEvaluations() {
        final Instant now = Instant.now();
        final Duration duration = Duration.between(lastPollTime, now);

        if (authInfo == null || duration.compareTo(minimumRefreshIntervalSecs) < 0) {
            log.debug("cannot refresh evaluations: not authenticated or below minimum delay");
            return; // not enough seconds elapsed
        }

        if (networkUnavailable()) {
            log.debug("cannot refresh evaluations: no network available");
            return;
        }

        if (target == null) {
            log.warn("cannot refresh evaluations: no target");
            return;
        }

        log.debug("Refreshing flags");

        try {
            final List<Evaluation> evaluations = pollOnce(api, authInfo);
            log.debug("Refresh got {} evaluations", evaluations.size());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }

        lastPollTime = now;
    }

    void repoSetEvaluation(String env, String flagIdentifier, Evaluation eval) {
        cache.saveEvaluation(env, flagIdentifier, eval);
    }

    void repoRemoveEvaluation(String env, String flagIdentifier) {
        cache.removeEvaluation(env, flagIdentifier);
    }

    void notifyListeners(final Evaluation evaluation) {

        if (evaluation == null) {
            logExceptionAndWarn("Evaluation null for notifyListeners", new Exception());
            return;
        }

        if (evaluationListenerMap.containsKey(evaluation.getFlag())) {

            final Set<EvaluationListener> callbacks = evaluationListenerMap.get(evaluation.getFlag());
            if (callbacks != null) {

                for (EvaluationListener listener : callbacks) {

                    listener.onEvaluation(evaluation);
                }
            }
        }
    }

    void sendEvent(StatusEvent statusEvent) {
        callbackExecutor.execute(() -> {
            Thread.currentThread().setName("RegisteredListenersThread");
            log.debug("send event {} to registered listeners", statusEvent.getEventType());

            for (final EventsListener listener : eventsListenerSet) {
                listener.onEventReceived(statusEvent);
            }
        });
    }

    Map<String, String> makeHeadersFrom(String token, String apiKey, AuthInfo authInfo) {
        return new HashMap<String, String>() {{
            put("Authorization", "Bearer " + token);
            put("API-Key", apiKey);
            put("Harness-SDK-Info", HARNESS_SDK_INFO);
            if (authInfo.getEnvironmentIdentifier() != null)
                put("Harness-EnvironmentID", authInfo.getEnvironmentIdentifier());
            if (authInfo.getAccountID() != null)
                put("Harness-AccountID", authInfo.getAccountID());
        }};
    }

    void addHeader(ClientApi api, String header, String value) {
        if (value != null) {
            api.getApiClient().addDefaultHeader(header, value);
        }
    }

    void logExceptionAndWarn(String msg, Throwable ex) {
        log.warn(msg);
        if (config.isDebugEnabled()) {
            log.warn(msg + " STACKTRACE", ex);
        }
    }

    ApiClient makeApiClient() {
        final ApiClient apiClient = new ApiClient()
            .setBasePath(config.getBaseURL())
            .setDebugging(false)
            .setUserAgent("android " + ANDROID_SDK_VERSION)
            .addDefaultHeader("Hostname", getHostname())
            .addDefaultHeader("Harness-SDK-Info", "Android " + ANDROID_SDK_VERSION + " Client");

        TlsUtils.setupTls(apiClient, config);
        return apiClient;
    }

    String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logExceptionAndWarn("Failed to get host", e);
            return "UnknownHost";
        }
    }

    boolean waitForInitialization(long timeoutMs) {
        try {
            SdkCodes.infoSdkWaitingForInit();

            if (timeoutMs <= 0) {
                initLatch.await();
                return true;
            }

            if (initLatch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logExceptionAndWarn("waitForInit interrupted", e);
        }
        log.warn("Failed to initialize within the {}ms timeout window. Defaults will be served.", timeoutMs);
        return false;
    }

    Evaluation getEvaluationById(String evaluationId, StringBuilder failureReason) {
        if (authInfo == null) {
            failureReason.append("SDK not authenticated");
            return null;
        }
        return cache.getEvaluation(authInfo.getEnvironmentIdentifier(), evaluationId);
    }

    String getBearerToken() {
        if (bearerToken == null) {
            throw new IllegalStateException("SDK not authenticated");
        }
        return bearerToken;
    }

    AuthInfo getAuthInfo() {
        if (authInfo == null) {
            throw new IllegalStateException("SDK not authenticated");
        }
        return authInfo;
    }

    boolean networkUnavailable() {
        return !network.isNetworkAvailable();
    }

    static class NetworkOffline extends RuntimeException { NetworkOffline() { super("No Internet"); }}

    @Override
    public void run() {
        do {
            try {
                api = new ClientApi(makeApiClient());
                mainSdkThread(api);
            } catch (NetworkOffline ex) {
                if (config.isDebugEnabled()) {
                    log.debug("Network offline trace", ex);
                }
                waitForNetworkToGoOnline();
                continue;
            } catch (Throwable ex) {
                logExceptionAndWarn("Root SDK exception handler invoked, SDK will be restarted in 1 minute:", ex);
            }
            /* should the sdk thread abort unexpectedly it will be restarted here */
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                log.trace("sdk restart delay interrupted", e);
            }
        } while (true);
    }

    private void waitForNetworkToGoOnline() {
        log.info("Network is offline, SDK going to sleep");

        final CountDownLatch networkLatch = new CountDownLatch(1);
        final NetworkInfoProvider networkSleeper = new NetworkInfoProvider(context);

        networkSleeper.register(status -> {
            if (status == CONNECTED) {
                networkLatch.countDown();
            }
        });

        try {
            if (!networkLatch.await(1, MINUTES)) {
                log.info("Network connected, wake up SDK");
            } else {
                log.info("Restart SDK/Check network");
            }
        } catch (InterruptedException e) {
            logExceptionAndWarn("Network sleep interrupted", e);
        }
    }

}
