package io.harness.cfsdk;

import static io.harness.cfsdk.utils.CfUtils.EvaluationUtil.areEvaluationsValid;

import android.content.Context;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherService;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.openapi.client.ApiException;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;
import io.harness.cfsdk.cloud.openapi.client.model.Variation;
import io.harness.cfsdk.cloud.events.AuthCallback;
import io.harness.cfsdk.cloud.events.AuthResult;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.network.NetworkStatus;
import io.harness.cfsdk.cloud.sse.EventSource;
import io.harness.cfsdk.cloud.sse.EventsListener;
import io.harness.cfsdk.cloud.sse.StatusEvent;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.common.SdkCodes;
import io.harness.cfsdk.utils.GuardObjectWrapper;

/**
 * Main class used for any operation on SDK. Operations include, but not limited to, reading evaluations and
 * observing changes in state of SDK.
 * Before it can be used, one of the {@link CfClient#initialize} methods <strong>must be</strong>  called
 */
public class CfClient implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(CfClient.class);

    private static final String HARNESS_SDK_INFO =
            String.format("Android %s Client", AndroidSdkVersion.ANDROID_SDK_VERSION);

    private static CfClient instance;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean ready = new AtomicBoolean();
    private final CloudFactory cloudFactory;
    private final Executor listenerUpdateExecutor = Executors.newSingleThreadExecutor();
    private final Set<EventsListener> eventsListenerSet = Collections.synchronizedSet(new LinkedHashSet<>());
    private final ConcurrentHashMap<String, Set<EvaluationListener>> evaluationListenerSet = new ConcurrentHashMap<>();
    private final GuardObjectWrapper evaluationPollingLock = new GuardObjectWrapper();
    private final Duration minimumRefreshIntervalSecs = Duration.ofSeconds(60);
    private final CountDownLatch initLatch = new CountDownLatch(1);

    private ICloud cloud;
    private Target target;
    private NetworkInfoProviding networkInfoProvider;
    private EventSource sseController;
    private CfConfiguration configuration;
    private AnalyticsManager analyticsManager;
    private FeatureRepository featureRepository;
    private EvaluationPolling evaluationPolling;
    private AuthInfo authInfo;
    private boolean useStream;
    private boolean analyticsEnabled;
    private Instant lastPollTime = Instant.EPOCH;

    /**
     * Base constructor.
     *
     * @param cloudFactory Cloud factory responsible for handling API.
     */
    public CfClient(CloudFactory cloudFactory) {

        this.cloudFactory = cloudFactory;
    }

    public CfClient() {

        cloudFactory = new CloudFactory();
    }

    /**
     * Retrieves the single instance of {@link CfClient} to be used for SDK operation
     *
     * @return single instance used as entry point of SDK
     */
    public static CfClient getInstance() {

        if (instance == null) {
            synchronized (CfClient.class) {

                if (instance == null) {
                    instance = new CfClient(new CloudFactory());
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the client and sets up needed dependencies. Upon called, it is dispatched to another thread and result is returned trough
     * provided {@link AuthCallback} instance.
     *
     * @param context       Context of application
     * @param apiKey        API key used for authentication
     * @param configuration Collection of different configuration flags, which defined the behaviour of SDK
     * @param target        Desired target against which we want features to be evaluated
     * @param cloudCache    Custom {@link CloudCache} implementation. If non provided, the default implementation will be used
     * @param authCallback  The callback that will be invoked when initialization is finished
     * @throws IllegalStateException If already initialized
     */
    public void initialize(
            final Context context,
            final String apiKey,
            final CfConfiguration configuration,
            final Target target,
            final CloudCache cloudCache,
            @Nullable final AuthCallback authCallback

    ) throws IllegalStateException {

        if (ready.get()) {
            throw new IllegalStateException("Already initialized");
        }

        setupNetworkInfo(context);

        doInitialize(apiKey, configuration, target, cloudCache, authCallback);
    }

    /**
     * Initialize the client and sets up needed dependencies. Upon called, it is dispatched to another thread and result is returned trough
     * provided {@link AuthCallback} instance.
     *
     * @param context       Context of application
     * @param apiKey        API key used for authentication
     * @param configuration Collection of different configuration flags, which defined the behaviour of SDK
     * @param target        Desired target against which we want features to be evaluated
     * @param authCallback  The callback that will be invoked when initialization is finished
     * @throws IllegalStateException If already initialized
     */
    public void initialize(

            final Context context,
            final String apiKey,
            final CfConfiguration configuration,
            final Target target,
            final AuthCallback authCallback

    ) throws IllegalStateException {

        initialize(context, apiKey, configuration, target, cloudFactory.defaultCache(context), authCallback);
    }

    /**
     * Initialize the client and sets up needed dependencies. Upon called, it is dispatched to another thread and result is returned trough
     * provided {@link AuthCallback} instance.
     *
     * @param context       Context of application
     * @param apiKey        API key used for authentication
     * @param configuration Collection of different configuration flags, which defined the behaviour of SDK
     * @param target        Desired target against which we want features to be evaluated
     * @param cloudCache    Custom {@link CloudCache} implementation. If non provided, the default implementation will be used
     * @throws IllegalStateException If already initialized
     */
    public void initialize(

            final Context context,
            final String apiKey,
            final CfConfiguration configuration,
            final Target target,
            final CloudCache cloudCache

    ) throws IllegalStateException {

        initialize(context, apiKey, configuration, target, cloudCache, null);
    }

    /**
     * Initialize the client and sets up needed dependencies. Upon called, it is dispatched to another thread and result is returned trough
     * provided {@link AuthCallback} instance.
     *
     * @param context       Context of application
     * @param apiKey        API key used for authentication
     * @param configuration Collection of different configuration flags, which defined the behaviour of SDK
     * @param target        Desired target against which we want features to be evaluated
     * @throws IllegalStateException If already initialized
     */
    public void initialize(

            final Context context,
            final String apiKey,
            final CfConfiguration configuration,
            final Target target

    ) throws IllegalStateException {

        initialize(context, apiKey, configuration, target, cloudFactory.defaultCache(context));
    }

    /**
     * Register a listener to observe changes on a evaluation with given id. The change <strong>will not</strong> be triggered
     * in case of reloading all evaluations, but only when single evaluation is changed.
     * It is possible to register multiple observers for a single evaluatio.
     *
     * @param evaluationId Evaluation identifier we would like to observe.
     * @param listener     {@link EvaluationListener} instance that will be invoked when evaluation is changed
     * @return Was evaluation registered with success?
     */
    public boolean registerEvaluationListener(String evaluationId, EvaluationListener listener) {

        if (listener != null) {

            Set<EvaluationListener> set = evaluationListenerSet.get(evaluationId);
            if (set == null) {

                set = new HashSet<>();
            }
            boolean success = set.add(listener);
            evaluationListenerSet.put(evaluationId, set);
            return success;
        }
        return false;
    }

    /**
     * Removes specified listener for an evaluation with given id.
     *
     * @param evaluationId Evaluation identifier.
     * @param listener     {@link EvaluationListener} instance we want to remove
     * @return Was evaluation un-registered with success?
     */
    public boolean unregisterEvaluationListener(String evaluationId, EvaluationListener listener) {

        if (listener != null) {

            Set<EvaluationListener> set = this.evaluationListenerSet.get(evaluationId);
            if (set != null) {

                return set.remove(listener);
            }
        }
        return false;
    }

    /**
     * Adds new listener for various SDK events. See {@link StatusEvent.EVENT_TYPE} for possible types.
     *
     * @param observer {@link EventsListener} implementation that will be triggered when there is a change in state of SDK
     * @return Was listener registered with success?
     */
    public boolean registerEventsListener(final EventsListener observer) {

        if (observer != null) {

            return eventsListenerSet.add(observer);
        }
        return false;
    }

    /**
     * Removes registered listener from list of registered events listener
     *
     * @param observer {@link EventsListener} implementation that needs to be removed
     * @return Was listener un-registered with success?
     */
    public boolean unregisterEventsListener(final EventsListener observer) {

        return eventsListenerSet.remove(observer);
    }

    public boolean boolVariation(String evaluationId, boolean defaultValue) {

        final Evaluation evaluation = getEvaluationById(evaluationId, target);

        if (evaluation == null) {
            log.warn("Evaluation for {} is null, returning default value", evaluationId);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            return defaultValue;
        }

        final String value = evaluation.getValue();
        if (value == null) {
            log.warn("Evaluation was found for '{}', but the value was null, " +
                    "returning default variation", evaluationId);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
        }

        return "true".equals(value);
    }

    public String stringVariation(String evaluationId, String defaultValue) {

        Evaluation evaluation = getEvaluationById(evaluationId, target);

        if (evaluation == null) {
            log.warn("Evaluation for {} is null, returning default value", evaluationId);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            return defaultValue;
        }

        if (evaluation.getValue() == null) {
            log.warn("Evaluation was found for '{}', but the value was null, " +
                    "returning default variation", evaluationId);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            return defaultValue;
        }

        return evaluation.getValue();
    }

    public double numberVariation(String evaluationId, double defaultValue) {

        final Evaluation evaluation = getEvaluationById(evaluationId, target);

        if (evaluation == null) {
            log.warn("Evaluation for {} is null, returning default value", evaluationId);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            return defaultValue;
        }

        String value = evaluation.getValue();

        if (value == null) {
            log.warn("Evaluation was found for '{}', but the value was null, " +
                    "returning default variation", evaluationId);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            return defaultValue;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.error("Error parsing evaluation value as double: " + e.getMessage(), e);
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            return defaultValue;
        }
    }

    public JSONObject jsonVariation(String evaluationId, JSONObject defaultValue) {

        try {

            final Evaluation e = getEvaluationById(

                    evaluationId,
                    target,
                    defaultValue
            );

            if (e.getValue() == null) {

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put(evaluationId, null);
                return new JSONObject(resultMap);

            } else {

                return new JSONObject(e.getValue());
            }
        } catch (JSONException e) {

            log.error(e.getMessage(), e);
        }
        SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
        return defaultValue;
    }

    /**
     * Perform a soft-poll. This method will get the latest flag evaluations from the server. It
     * is useful for forcing the SDK's state to refresh when coming to the foreground, after
     * a period of background inactivity where stream events may have been missed.
     * This method will only call out to the server once every minute.
     */
    public void refreshEvaluations() {
        final Instant now = Instant.now();
        final Duration duration = Duration.between(lastPollTime, now);

        if (duration.compareTo(minimumRefreshIntervalSecs) < 0) {
            return; // not enough time elapsed
        }

        if (target == null || authInfo == null) {
            log.warn("cannot refresh evaluations: no target/not authenticated");
            return;
        }

        log.debug("Refreshing flags");

        final List<Evaluation> evaluations = this.featureRepository.getAllEvaluations(
            authInfo.getEnvironmentIdentifier(),
            target.getIdentifier(),
            authInfo.getCluster()
        );

        lastPollTime = now;
        log.debug("Refresh got {} evaluations", evaluations.size());
    }


    /**
     * Clears the occupied resources and shut's down the sdk.
     * After calling this method, the {@link #initialize} must be called again. It will also
     * remove any registered event listeners.
     * @since 1.2.0
     */
    @Override
    public void close() {

        if (analyticsManager != null) {
            analyticsManager.close();
        }

        unregister();

        eventsListenerSet.clear();
        evaluationListenerSet.clear();

        if (networkInfoProvider != null) {

            networkInfoProvider.unregisterAll();
        }

        instance = null;
    }

    /**
     * Deprecated since 1.2.0 use close() instead as this will be removed in the future
     * @deprecated since 1.2.0
     */
    @Deprecated
    public void destroy() {
        close();
    }


    /**
     * Wait for the SDK to authenticate and populate its cache.
     * @param timeoutMs Timeout in milliseconds to wait for SDK to initialize
     * @return Returns true if successfully initialized else false if timed out or something went
     * wrong. If false is returned, your code may proceed to use xVariation functions however
     * default variations may be served (SDKCODE 2001). For failure cause check logs leading up.
     * @since 1.2.0
     */
    public boolean waitForInitialization(long timeoutMs) {
        try {
            SdkCodes.infoSdkWaitingForInit();
            if (initLatch.await(timeoutMs, TimeUnit.MILLISECONDS) && (authInfo != null)) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("waitForInitialization interrupted", e);
        }
        log.warn("Failed to initialize within the {}ms timeout window. Defaults will be served.", timeoutMs);
        return false;
    }
    /**
     * Wait for the SDK to authenticate and populate it cache. This version waits indefinitely, if
     * you require control over startup time then use {@link #waitForInitialization(long) waitForInitialization(timeoutMs)}
     * @throws InterruptedException if the thread was interrupted while waiting
     * @since 1.2.0
     */
    public void waitForInitialization() throws InterruptedException {
        SdkCodes.infoSdkWaitingForInit();
        initLatch.await();
    }

    private final EventsListener eventsListener = statusEvent -> {

        log.debug("SSE event received: {}", statusEvent.getEventType());

        if (!ready.get()) {

            log.warn("SSE event ignored, client is not ready");
            return;
        }

        final String environmentID = authInfo.getEnvironmentIdentifier();
        final String cluster = authInfo.getCluster();

        switch (statusEvent.getEventType()) {

            case SSE_START:
                evaluationPolling.stop();
                StatusEvent startEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_START);
                sendEvent(startEvent);
                break;

            case SSE_RESUME:
                evaluationPolling.stop();
                log.debug("SSE connection resumed, reloading all evaluations");

                final List<Evaluation> resumedEvaluations = featureRepository.getAllEvaluations(

                        environmentID,
                        target.getIdentifier(),
                        cluster
                );

                StatusEvent resumeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_RESUME, resumedEvaluations);
                sendEvent(resumeEvent);
                break;

            case SSE_END:

                if (networkInfoProvider.isNetworkAvailable()) {

                    this.featureRepository.getAllEvaluations(

                            environmentID,
                            target.getIdentifier(),
                            cluster
                    );

                    evaluationPolling.start(this::reschedule);
                }
                StatusEvent endEvent = new StatusEvent(StatusEvent.EVENT_TYPE.SSE_END);
                sendEvent(endEvent);
                break;

            case EVALUATION_CHANGE:
                List<Evaluation> changeEvaluations = statusEvent.extractEvaluationListPayload();

                // if evaluations are present in sse event save it directly, else fetch from server
                if(areEvaluationsValid(changeEvaluations)) {
                    for (int i = 0; i < changeEvaluations.size(); i++) {
                        featureRepository.save(authInfo.getEnvironmentIdentifier(), target.getIdentifier(), changeEvaluations.get(i));
                        StatusEvent preEvalChangeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, changeEvaluations.get(i));
                        sendEvent(preEvalChangeEvent);
                        notifyListeners(changeEvaluations.get(i));
                    }
                } else {
                    for (int i = 0; i < changeEvaluations.size(); i++) {
                        Evaluation evaluation = featureRepository.getEvaluationFromServer(

                                authInfo.getEnvironmentIdentifier(),
                                target.getIdentifier(),
                                changeEvaluations.get(i).getFlag(),
                                cluster
                        );

                        if (evaluation != null) {
                            StatusEvent evalChangeEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_CHANGE, evaluation);
                            sendEvent(evalChangeEvent);
                            notifyListeners(evaluation);
                        } else {
                            log.warn("EVALUATION_CHANGE event failed to get evaluation for target '{}' from server", target.getIdentifier());
                        }
                    }
                }

                break;

            case EVALUATION_REMOVE:

                final Evaluation eval = statusEvent.extractEvaluationPayload();
                featureRepository.remove(authInfo.getEnvironmentIdentifier(), target.getIdentifier(), eval.getFlag());
                StatusEvent evalRemoveEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_REMOVE, eval);
                sendEvent(evalRemoveEvent);
                notifyListeners(eval);
                break;

            case EVALUATION_RELOAD:
                // if evaluations are present in sse event save it directly, else fetch from server
                if(areEvaluationsValid(statusEvent.extractEvaluationListPayload())) {
                    List<Evaluation> reloadEvaluations = statusEvent.extractEvaluationListPayload();
                    for (int i = 0; i < reloadEvaluations.size(); i++) {
                        featureRepository.save(authInfo.getEnvironmentIdentifier(), target.getIdentifier(), reloadEvaluations.get(i));
                        notifyListeners(reloadEvaluations.get(i));
                    }
                    StatusEvent preEvalReloadEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, reloadEvaluations);
                    sendEvent(preEvalReloadEvent);

                } else {
                    log.debug("Reloading all evaluations");


                    final List<Evaluation> fetchedEvaluations = featureRepository.getAllEvaluations(

                            environmentID,
                            target.getIdentifier(),
                            cluster
                    );

                    for (int i = 0; i < fetchedEvaluations.size(); i++) {
                        notifyListeners(fetchedEvaluations.get(i));
                    }

                    StatusEvent evalReloadEvent = new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, fetchedEvaluations);
                    sendEvent(evalReloadEvent);
                }

                break;
        }

    };

    void sendEvent(StatusEvent statusEvent) {
        listenerUpdateExecutor.execute(() -> {
            Thread.currentThread().setName("RegisteredListenersThread");
            log.debug("send event {} to registered listeners", statusEvent.getEventType());

            for (final EventsListener listener : eventsListenerSet) {
                listener.onEventReceived(statusEvent);
            }
        });
    }

    private void notifyListeners(final Evaluation evaluation) {

        if (evaluation == null) {

            log.error("Evaluation is null");
            return;
        }

        if (evaluationListenerSet.containsKey(evaluation.getFlag())) {

            final Set<EvaluationListener> callbacks = evaluationListenerSet.get(evaluation.getFlag());
            if (callbacks != null) {

                for (EvaluationListener listener : callbacks) {

                    listener.onEvaluation(evaluation);
                }
            }
        }
    }

    private void reschedule() {
        log.debug("Reschedule");
        executor.execute(this::runRescheduleThreadWrapEx);
    }

    private void runRescheduleThreadWrapEx() {
        try {
            runRescheduleThread();
        } catch (InterruptedException ex) {
            log.warn("Reschedule delay interrupted", ex);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            log.error("Failed to call reschedule() " + ex.getMessage(), ex);

            if (networkInfoProvider.isNetworkAvailable()) {
                evaluationPolling.start(this::reschedule);
            }

            throw new RejectedExecutionException(ex);
        }
    }

    private void runRescheduleThread() throws ApiException, InterruptedException {

        long delayMs = ThreadLocalRandom.current().nextLong(5_000, 10_000);
        log.info("SDK will restart in {}ms", delayMs);
        TimeUnit.MILLISECONDS.sleep(delayMs);

        if (!ready.get() && cloud.initialize()) {
            ready.set(true);
            this.authInfo = cloud.getAuthInfo();

            if (analyticsEnabled) {
                this.analyticsManager.close();
                this.analyticsManager = getAnalyticsManager(configuration, authInfo);
            }
        }

        if (!ready.get()) {
            return;
        }

        final String environmentID = authInfo.getEnvironmentIdentifier();
        final String cluster = authInfo.getCluster();

        final List<Evaluation> evaluations = this.featureRepository.getAllEvaluations(

                environmentID,
                target.getIdentifier(),
                cluster
        );

        log.debug("Evaluations count: {}", evaluations.size());

        // Notify users that evaluations have been reloaded. This happens first on client init,
        // then if polling is enabled, on each interval.
        sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations));

        if (useStream) {
            startSSE(true);
        } else {
            evaluationPolling.start(this::reschedule);
        }
    }

    protected void setupNetworkInfo(Context context) {

        if (networkInfoProvider != null) {
            networkInfoProvider.unregisterAll();
        } else {
            networkInfoProvider = cloudFactory.networkInfoProvider(context);
        }

        networkInfoProvider.register(status -> {
            if (status == NetworkStatus.CONNECTED) {
                reschedule();
            } else {
                // waiting for the lock to be release.
                evaluationPollingLock.get();
                evaluationPolling.stop();
            }
        });
    }

    private synchronized void startSSE(boolean isRescheduled) {
        log.debug("Start SSE");
        sseController.start(isRescheduled);
    }

    private synchronized void stopSSE() {
        log.debug("Stop SSE");
        this.useStream = false;
        if (sseController != null) {
            sseController.stop();
        }
    }

    private Map<String, String> makeHeadersFrom(String token, String apiKey, AuthInfo authInfo) {
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

    protected void doInitialize(
            final String apiKey,
            final CfConfiguration configuration,
            final Target target,
            final CloudCache cloudCache,
            @Nullable final AuthCallback authCallback
    ) {
        this.configuration = configuration;
        this.target = target;

        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                SdkCodes.errorMissingSdkKey();
                throw new IllegalArgumentException("missing sdk key");
            }

            if (target == null || configuration == null) {
                throw new IllegalArgumentException("Target and configuration must not be null!");
            }

            setTargetDefaults(target);

            executor.execute(() -> runInitThreadWrapEx(apiKey, cloudCache, authCallback));

        } catch (Exception e) {
            log.error(e.getMessage(), e);

            if (authCallback != null) {
                final Throwable cause = e instanceof RejectedExecutionException ? e.getCause() : e;
                final AuthResult result = new AuthResult(false, cause);
                authCallback.authorizationSuccess(authInfo, result);
            }

            close();
        }
    }

    private void runInitThread(final String apiKey, final CloudCache cloudCache, final AuthCallback authCallback) throws ApiException {

        unregister();
        cloud = cloudFactory.cloud(configuration.getStreamURL(), configuration.getBaseURL(), apiKey, target, configuration);

        featureRepository = cloudFactory.getFeatureRepository(cloud, cloudCache, networkInfoProvider);
        evaluationPolling = cloudFactory.evaluationPolling(configuration.getPollingInterval(), TimeUnit.SECONDS);
        // release the lock.
        evaluationPollingLock.set(evaluationPolling);

        this.useStream = configuration.getStreamEnabled();
        this.analyticsEnabled = configuration.isAnalyticsEnabled();

        if (!cloud.initialize() && (authCallback != null)) {
            final String message = "Authorization was not successful - cloudFactory.initialize() failed";
            final Exception error = new Exception(message);
            final AuthResult result = new AuthResult(false, error);
            authCallback.authorizationSuccess(authInfo, result);
        }

        authInfo = cloud.getAuthInfo();
        final String streamUrl = configuration.getStreamURL() + "?cluster=" + authInfo.getCluster();
        sseController = new EventSource(streamUrl, makeHeadersFrom(cloud.getAuthToken(), apiKey, authInfo), eventsListener, 1, 2_000, configuration.getTlsTrustedCAs());

        ready.set(true);

        if (networkInfoProvider.isNetworkAvailable()) {

            final List<Evaluation> evaluations = featureRepository.getAllEvaluations(
                    authInfo.getEnvironmentIdentifier(), target.getIdentifier(), authInfo.getCluster()
            );

            sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations));

            if (useStream) {
                startSSE(false);
            } else {
                evaluationPolling.start(this::reschedule);
            }
        }

        if (analyticsEnabled) {
            this.analyticsManager = getAnalyticsManager(configuration, authInfo);
        }

        if (authCallback != null) {
            SdkCodes.infoSdkAuthOk();
            final AuthResult result = new AuthResult(true);
            authCallback.authorizationSuccess(authInfo, result);
        }

        initLatch.countDown();
    }

    private void runInitThreadWrapEx(final String apiKey, final CloudCache cloudCache, final AuthCallback authCallback) {
        try {
            runInitThread(apiKey, cloudCache, authCallback);
        } catch (ApiException e) {
            throw new RejectedExecutionException(e);
        }
    }

    private void setTargetDefaults(Target target) {
        if ((target.getIdentifier() == null || target.getIdentifier().isEmpty())
                && (target.getName() == null || target.getName().isEmpty())) {
            throw new IllegalArgumentException("Target identifier and name are both missing");
        }

        if (target.getIdentifier() == null || target.getIdentifier().isEmpty()) {
            // TargetIDs cannot have spaces in them.
            String strippedName = target.getName().replace(" ", "_");
            target.identifier(strippedName);
        }
    }

    @NotNull
    protected AnalyticsManager getAnalyticsManager(CfConfiguration configuration, AuthInfo authInfo) {

        return new AnalyticsManager(
                configuration, new AnalyticsPublisherService(configuration, authInfo, cloud.getAuthToken())
        );
    }

    protected boolean canPushToMetrics() {

        return this.target.isValid() &&
                analyticsEnabled &&
                analyticsManager != null;
    }

    private void unregister() {

        ready.set(false);

        stopSSE();

        if (evaluationPolling != null) {

            evaluationPolling.stop();
        }

        if (featureRepository != null) {

            featureRepository.clear();
        }
    }

    /* ---------- Package private ---------- */

    /**
     * Retrieves a single {@link Evaluation} instance based on the provided id. If the SDK is not ready or
     * if no evaluation is found for the given id, the method returns null.
     *
     * This method is responsible only for fetching the evaluation and does not handle default values.
     * The calling methods should handle the case when null is returned.
     *
     * @param evaluationId Identifier of the target evaluation
     * @param target       The target for which the evaluation is to be fetched
     * @return Evaluation for the given id if found, otherwise null
     */
    <T> Evaluation getEvaluationById(

            String evaluationId,
            Target target
    ) {

        if (!ready.get()) {
            // SDK isn't ready, so return early.
            log.warn("SDK not initialized yet, not evaluating: '{}' ", evaluationId);
            return null;
        }

        final String cluster = authInfo.getCluster();
        final String identifier = authInfo.getEnvironmentIdentifier();

        Evaluation result = featureRepository.getEvaluation(

                identifier, target.getIdentifier(), evaluationId, cluster
        );

        // Return early if the evaluation wasn't found
        if (result == null) {
            log.warn("Evaluation not found: '{}' ", evaluationId);
            return null;
        }

        return result;
    }

    private void pushToMetrics(String evaluationId, Evaluation result) {
        if (canPushToMetrics()) {
            final Variation variation = new Variation();
            variation.setName(evaluationId);
            variation.setValue(result.getValue());
            variation.setIdentifier(result.getIdentifier());

            analyticsManager.registerEvaluation(this.target, evaluationId, variation);
        }
    }

    void setNetworkInfoProvider(NetworkInfoProviding networkInfoProvider) {
        this.networkInfoProvider = networkInfoProvider;
    }

    void reset() {
        unregister();
    }

    CfClient(Target target, AuthInfo authInfo, FeatureRepository featureRepository) { /* tests only */
        this.target = target;
        this.authInfo = authInfo;
        this.featureRepository = featureRepository;
        this.cloudFactory = null;
    }
}
