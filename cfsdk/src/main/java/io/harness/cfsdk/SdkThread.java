package io.harness.cfsdk;

import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.SECONDS;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;
import static io.harness.cfsdk.utils.CfUtils.EvaluationUtil.areEvaluationsValid;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.DefaultCache;
import io.harness.cfsdk.cloud.events.AuthCallback;
import io.harness.cfsdk.cloud.events.AuthResult;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkChecker;
import io.harness.cfsdk.cloud.network.NewRetryInterceptor;
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
import okhttp3.OkHttpClient;

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
    private final AuthCallback authCallback;
    private final NetworkChecker networkChecker;


    /* ---- Mutable state ---- */
    private final AtomicReference<Instant> lastPollTime = new AtomicReference<>(Instant.EPOCH);
    private ClientApi api;
    private String bearerToken;
    private AuthInfo authInfo;
    private boolean sseRescheduled = false;
    // Used for emitting the initialize callback only once
    private boolean isAuthSuccessfulOnce = false;

    private volatile boolean running = true;


    SdkThread(Context context, String apiKey, CfConfiguration config, Target target, Map<String, Set<EvaluationListener>> evaluationListenerMap, Set<EventsListener> eventsListenerSet, AuthCallback authCallback, NetworkChecker networkChecker)  {
        this.context = context;
        this.apiKey = apiKey;
        this.config = config;
        this.target = target;
        this.cache = (config.getCache() != null) ? config.getCache() : new DefaultCache(context, target.getIdentifier(), apiKey);
        this.callbackExecutor.execute(() -> Thread.currentThread().setName("CallbackThread"));
        this.evaluationListenerMap = evaluationListenerMap;
        this.eventsListenerSet = eventsListenerSet;
        this.authCallback = authCallback;
        this.networkChecker = networkChecker;
    }

    void mainSdkThread(ClientApi api) throws ApiException {
        /* For SDK v2 we have three states the SDK can be in:
               - authenticating()
               - streaming()
               - polling()
           Each of these methods block the sdkThread.
           Any spurious exceptions (e.g. socket timeout) will be caught by the root exception
           handler in run() and this method will be restarted.
           Make sure any network calls correctly timeout and don't hang because we must not block
           the sdkThread. */

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

        if (fallbackToPolling && config.isPollingEnabled()) {
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

        try {
            bearerToken = api.authenticate(authRequest).getAuthToken();
        } catch (ApiException ex) {
            if (authCallback != null && ex.getCode() != 200 && !isAuthSuccessfulOnce) {
                authCallback.authorizationSuccess(null, new AuthResult(false, ex));
            }
            throw ex;
        }

        addHeader(api, "Authorization", "Bearer " + bearerToken);

        final AuthInfo ai = new AuthResponseDecoder().extractInfo(bearerToken);
        if (ai != null) {
            authInfo = ai;

            addHeader(api, "Harness-EnvironmentID", authInfo.getEnvironmentTrackingHeader());
            addHeader(api, "Harness-AccountID", authInfo.getAccountID());

            pollOnce(api, authInfo);

            SdkCodes.infoSdkAuthOk(AndroidSdkVersion.ANDROID_SDK_VERSION);
            initLatch.countDown();

            // Only emit this callback once
            if (!isAuthSuccessfulOnce) {
                isAuthSuccessfulOnce = true;
                if (authCallback != null) {
                    authCallback.authorizationSuccess(ai, new AuthResult(true));
                }
            }
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
            log.debug("streaming() SSE event received: {}", statusEvent.getEventType());

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
                    log.info("streaming() SSE network went offline");
                } else {
                    logExceptionAndWarn("streaming() Exception in event handler: " + ex.getMessage(), ex);
                }
                endStreamLatch.countDown();
            }
        };

        final String streamUrl = config.getStreamURL() + "?cluster=" + authInfo.getCluster();
        try (EventSource eventSource = new EventSource(streamUrl, makeHeadersFrom(bearerToken, apiKey, authInfo), eventsListener, 1, config)) {
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
        log.debug("streaming() SSE connection resumed, reloading all evaluations");
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
                    log.warn("streaming() EVALUATION_CHANGE event failed to get evaluation for target '{}' from server", target.getIdentifier());
                }
            }
        }
    }

    void streamSseEvaluationRemove(ClientApi api, AuthInfo authInfo, StatusEvent statusEvent) {
        final Evaluation eval = statusEvent.extractEvaluationPayload();
        repoRemoveEvaluation(authInfo.getEnvironmentIdentifier(), eval.getFlag());
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
            log.debug("streaming() Reloading all evaluations");
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

        log.debug("Poll got {} evaluations", evaluations.size());

        // Notify users that evaluations have been reloaded. This happens first on client init,
        // then if polling is enabled, on each interval.
        sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations));
        return evaluations;
    }

    public void refreshEvaluations() {
        final Instant now = Instant.now();
        final Duration duration = Duration.between(lastPollTime.getAndSet(now), now); // called outside SDK thread

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

        lastPollTime.set(now);
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

            final List<EventsListener> toNotify;
            synchronized (eventsListenerSet) {
                toNotify = new ArrayList<>(eventsListenerSet);
            }

            toNotify.forEach(l -> l.onEventReceived(statusEvent));
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
        log.warn(msg + " " + ex.getMessage());
        if (config.isDebugEnabled()) {
            log.warn(msg + " STACKTRACE ", ex);
        }
    }

    ApiClient makeApiClient() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new NewRetryInterceptor(current().nextInt(2000, 5000)));

        final ApiClient apiClient = new ApiClient(builder.build())
            .setBasePath(config.getBaseURL())
            .setDebugging(config.isDebugEnabled())
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
        return !networkChecker.isNetworkAvailable(context);
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
            } catch (ApiException ex) {
                if (ex.getCode() == 403) {
                    SdkCodes.warnAuthFailedSrvDefaults(ex.getMessage());
                    break;
                } else {
                    logExceptionAndWarn("API exception encountered, SDK will be restarted in 1 minute:", ex);
                }
            } catch (Throwable ex) {
                logExceptionAndWarn("Root SDK exception handler invoked, SDK will be restarted in 1 minute:", ex);
            }

            /* should the sdk thread abort, it will conditionally be restarted here */

            // If both streaming and polling are disabled, then we don't need the sdk thread to run anymore
            if (!config.isStreamEnabled() && !config.isPollingEnabled()) {
                log.info("Streaming and Polling are disabled. Initial setup complete. Exiting SDK main thread.");
                break;
            }

            // Restart the thread here as it has aborted unexpectedly
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                log.trace("sdk restart delay interrupted", e);
            }
        } while (running && !Thread.currentThread().isInterrupted());
    }



    private void waitForNetworkToGoOnline() {
        log.info("Network is offline, SDK going to sleep");

        int counter = 30;
        do {
            try {
                if (networkChecker.isNetworkAvailable(context)) {
                    log.info("Network is online, restarting SDK");
                    return;
                }
                SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.trace("sdk network wait interrupted", e);
            }
        } while (counter-- > 0);
    }

    void stop() {
        running = false;
        Thread.currentThread().interrupt();
    }

}
