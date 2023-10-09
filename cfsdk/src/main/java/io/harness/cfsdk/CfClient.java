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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.Variation;
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
                break;

            case SSE_RESUME:
                evaluationPolling.stop();
                log.debug("SSE connection resumed, reloading all evaluations");

                final List<Evaluation> resumedEvaluations = featureRepository.getAllEvaluations(

                        environmentID,
                        target.getIdentifier(),
                        cluster
                );

                statusEvent = new StatusEvent(statusEvent.getEventType(), resumedEvaluations);

            case SSE_END:

                if (networkInfoProvider.isNetworkAvailable()) {

                    this.featureRepository.getAllEvaluations(

                            environmentID,
                            target.getIdentifier(),
                            cluster
                    );

                    evaluationPolling.start(this::reschedule);
                }
                break;

            case EVALUATION_CHANGE:
                List<Evaluation> evaluations = statusEvent.extractEvaluationListPayload();

                // if evaluations are present in sse event save it directly, else fetch from server
                if(areEvaluationsValid(evaluations)) {
                    for (int i = 0; i < evaluations.size(); i++) {
                        featureRepository.save(authInfo.getEnvironmentIdentifier(), target.getIdentifier(), evaluations.get(i));
                        statusEvent = new StatusEvent(statusEvent.getEventType(), evaluations.get(i));
                        notifyListeners(evaluations.get(i));
                    }
                } else {
                    for (int i = 0; i < evaluations.size(); i++) {
                        Evaluation evaluation = featureRepository.getEvaluationFromServer(

                                authInfo.getEnvironmentIdentifier(),
                                target.getIdentifier(),
                                evaluations.get(i).getFlag(),
                                cluster
                        );

                        if (evaluation != null) {
                            statusEvent = new StatusEvent(statusEvent.getEventType(), evaluation);
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
                break;

            case EVALUATION_RELOAD:
                // TODO - add a try around this payload - possibly triggered by other things too
                evaluations = statusEvent.extractEvaluationListPayload();

                // if evaluations are present in sse event save it directly, else fetch from server
                if(areEvaluationsValid(evaluations)) {
                    for (int i = 0; i < evaluations.size(); i++) {
                        featureRepository.save(authInfo.getEnvironmentIdentifier(), target.getIdentifier(), evaluations.get(i));
                        // TODO - do we need to notify listeners - the other path doesn't
                        notifyListeners(evaluations.get(i));
                    }
                    statusEvent = new StatusEvent(statusEvent.getEventType(), evaluations);
                } else {
                    log.debug("Reloading all evaluations");


                    final List<Evaluation> fetchedEvaluations = featureRepository.getAllEvaluations(

                            environmentID,
                            target.getIdentifier(),
                            cluster
                    );

                    statusEvent = new StatusEvent(statusEvent.getEventType(), fetchedEvaluations);
                }

                break;
        }

        sendEvent(statusEvent);
    };

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

        executor.execute(() -> {

            try {

                if (!ready.get()) {

                    boolean success = cloud.initialize();
                    if (success) {

                        ready.set(true);
                        this.authInfo = cloud.getAuthInfo();

                        if (analyticsEnabled) {

                            this.analyticsManager.close();
                            this.analyticsManager = getAnalyticsManager(

                                    configuration, authInfo
                            );
                        }
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

            } catch (Exception e) {

               log.error(e.getMessage(), e);

                if (networkInfoProvider.isNetworkAvailable()) {

                    evaluationPolling.start(this::reschedule);
                }
            }
        });
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

        setupNetworkInfo(context);  // evaluationPoll.stop()

        doInitialize(       // evaluationPoll - defined.

                apiKey,
                configuration,
                target,
                cloudCache,
                authCallback
        );


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

        initialize(

                context,
                apiKey,
                configuration,
                target,
                cloudFactory.defaultCache(context),
                authCallback
        );
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

        initialize(

                context,
                apiKey,
                configuration,
                target,
                cloudCache,
                null
        );
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

        initialize(

                context,
                apiKey,
                configuration,
                target,
                cloudFactory.defaultCache(context)
        );
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

        if (apiKey == null || apiKey.trim().isEmpty()) {
            SdkCodes.errorMissingSdkKey();
            throw new IllegalArgumentException("missing sdk key");
        }

        if (target == null || configuration == null) {
            if (authCallback != null) {

                final String message = "Target and configuration must not be null!";
                final IllegalArgumentException error = new IllegalArgumentException(message);
                final AuthResult result = new AuthResult(false, error);
                authCallback.authorizationSuccess(authInfo, result);
            }
            return;
        }

        try {
            setTargetDefaults(target);
        } catch (IllegalArgumentException e) {
            final AuthResult result = new AuthResult(false, e);
            if (authCallback != null) {
                authCallback.authorizationSuccess(null, result);
            }
            return;
        }
        try {

            executor.execute(() -> {

                unregister();
                this.cloud = cloudFactory.cloud(

                        configuration.getStreamURL(),
                        configuration.getBaseURL(),
                        apiKey,
                        target,
                        configuration
                );

                featureRepository = cloudFactory.getFeatureRepository(cloud, cloudCache, networkInfoProvider);
                evaluationPolling = cloudFactory.evaluationPolling(configuration.getPollingInterval(), TimeUnit.SECONDS);
                // release the lock.
                evaluationPollingLock.set(evaluationPolling);

                this.useStream = configuration.getStreamEnabled();
                this.analyticsEnabled = configuration.isAnalyticsEnabled();

                boolean success = false;
                try {
                    success = cloud.initialize();
                } catch (ApiException e) {
                    log.warn(e.getMessage(), e);
                    final AuthResult result = new AuthResult(false, e);
                    if (authCallback != null) {
                        authCallback.authorizationSuccess(null, result);
                    }
                    return;
                }
                if (success) {

                    this.authInfo = cloud.getAuthInfo();
                    this.sseController = new EventSource(configuration.getStreamURL() + "?cluster=" + authInfo.getCluster(), makeHeadersFrom(cloud.getAuthToken(), apiKey, authInfo), eventsListener, 1, 2_000, configuration.getTlsTrustedCAs());

                    final String environmentID = authInfo.getEnvironment();
                    final String cluster = authInfo.getCluster();

                    ready.set(true);

                    if (networkInfoProvider.isNetworkAvailable()) {

                        List<Evaluation> evaluations = featureRepository.getAllEvaluations(

                                this.authInfo.getEnvironmentIdentifier(),
                                target.getIdentifier(),
                                cluster
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
                } else {

                    if (authCallback != null) {

                        final String message = "Authorization was not successful";
                        final Exception error = new Exception(message);
                        final AuthResult result = new AuthResult(false, error);
                        authCallback.authorizationSuccess(authInfo, result);
                    }
                }
            });
        } catch (RejectedExecutionException e) {

            log.error(e.getMessage(), e);
            if (authCallback != null) {

                final AuthResult result = new AuthResult(false, e);
                authCallback.authorizationSuccess(authInfo, result);
            }

            close();
        }
    }

    private void setTargetDefaults(Target target) {
        if ((target.getIdentifier() == null || target.getIdentifier().isEmpty())
                && (target.getName() == null || target.getName().isEmpty())) {
            throw new IllegalArgumentException("Target identifier and name are both missing");
        }

        if (target.getIdentifier() == null || target.getIdentifier().isEmpty()) {
            // TargetIDs cannot have spaces in them.
            String StrippedName = target.getName().replace(" ", "_");
            target.identifier(StrippedName);
        }
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
     * Retrieves single {@link Evaluation instance} based on provided id. If no such evaluation is found,
     * returns one with provided default value.
     *
     * @param evaluationId Identifier of target evaluation
     * @param defaultValue Default value to be used in case when evaluation is not found
     * @return Evaluation for a given id
     */
    <T> Evaluation getEvaluationById(

            String evaluationId,
            Target target,
            T defaultValue
    ) {

        Evaluation result = new Evaluation();

        if (ready.get()) {

            final String cluster = authInfo.getCluster();
            final String identifier = authInfo.getEnvironmentIdentifier();

            result = featureRepository.getEvaluation(

                    identifier, target.getIdentifier(), evaluationId, cluster
            );

        } else {

            result.value(defaultValue)
                    .flag(evaluationId);
        }

        if (result == null) {

            log.warn("Result is null, creating the default one");
            SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
            result = new Evaluation()
                    .value(defaultValue)
                    .flag(evaluationId);
        }

        if (canPushToMetrics(result)) {

            final Variation variation = new Variation();
            variation.setName(evaluationId);
            variation.setValue(String.valueOf(result.value));
            variation.setIdentifier(result.getIdentifier());

            if (!analyticsManager.pushToQueue(this.target, evaluationId, variation)) {

                log.warn("Error adding into the metrics queue");
            }
        }

        return result;
    }

    public boolean boolVariation(String evaluationId, boolean defaultValue) {

        final Evaluation evaluation = getEvaluationById(

                evaluationId,
                target,
                defaultValue
        );

        final Object value = evaluation.getValue();
        if (value instanceof Boolean) {

            return (Boolean) value;
        }
        if (value instanceof String) {

            return "true".equals(value);
        }

        SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
        return defaultValue;
    }

    public String stringVariation(String evaluationId, String defaultValue) {

        return getEvaluationById(evaluationId, target, defaultValue).getValue();
    }

    public double numberVariation(String evaluationId, double defaultValue) {

        final Evaluation evaluation = getEvaluationById(

                evaluationId,
                target,
                defaultValue
        );

        final Object value = evaluation.getValue();
        if (value instanceof Number) {

            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {

            final String strValue = (String) value;
            try {

                return Double.parseDouble(strValue);
            } catch (NumberFormatException e) {

                log.error(e.getMessage(), e);
            }
        }
        SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
        return defaultValue;
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

                if (e.getValue() instanceof JSONObject) {

                    return e.getValue();
                }
                return new JSONObject(e.getValue().toString());
            }
        } catch (JSONException e) {

            log.error(e.getMessage(), e);
        }
        SdkCodes.warnDefaultVariationServed(evaluationId, target, String.valueOf(defaultValue));
        return defaultValue;
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

    /**
     * Clears the occupied resources and shut's down the sdk.
     * After calling this method, the {@link #initialize} must be called again. It will also
     * remove any registered event listeners.
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

    @NotNull
    protected AnalyticsManager getAnalyticsManager(

            CfConfiguration configuration,
            AuthInfo authInfo
    ) {

        return new AnalyticsManager(

                authInfo,
                cloud.getAuthToken(),
                configuration
        );
    }

    protected boolean canPushToMetrics(Evaluation result) {

        return this.target.isValid() &&
                result.isValid() &&
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

    /* Package private */

    void setNetworkInfoProvider(NetworkInfoProviding networkInfoProvider) {
        this.networkInfoProvider = networkInfoProvider;
    }

    void reset() {
        unregister();
    }
}
