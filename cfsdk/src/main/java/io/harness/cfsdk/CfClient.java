package io.harness.cfsdk;

import android.content.Context;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

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
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.sse.SSEControlling;
import io.harness.cfsdk.common.Destroyable;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.utils.GuardObjectWrapper;

/**
 * Main class used for any operation on SDK. Operations include, but not limited to, reading evaluations and
 * observing changes in state of SDK.
 * Before it can be used, one of the {@link CfClient#initialize} methods <strong>must be</strong>  called
 */
public class CfClient implements Destroyable {

    protected ICloud cloud;
    protected Target target;
    protected static CfClient instance;
    protected boolean analyticsEnabled;
    protected NetworkInfoProviding networkInfoProvider;

    private AuthInfo authInfo;
    private boolean useStream;
    private final String logTag;
    private final Executor executor;
    private final AtomicBoolean ready;
    private SSEControlling sseController;
    private CfConfiguration configuration;
    private final CloudFactory cloudFactory;
    private AnalyticsManager analyticsManager;
    private FeatureRepository featureRepository;
    private EvaluationPolling evaluationPolling;
    private final Executor listenerUpdateExecutor;
    private final Set<EventsListener> eventsListenerSet;
    private final ConcurrentHashMap<String, Set<EvaluationListener>> evaluationListenerSet;
    private GuardObjectWrapper evaluationPollingLock;

    {

        ready = new AtomicBoolean();
        logTag = CfClient.class.getSimpleName();
        executor = Executors.newSingleThreadExecutor();
        evaluationListenerSet = new ConcurrentHashMap<>();
        listenerUpdateExecutor = Executors.newSingleThreadExecutor();
        eventsListenerSet = Collections.synchronizedSet(new LinkedHashSet<>());
        evaluationPollingLock = new GuardObjectWrapper();

    }

    private final EventsListener eventsListener = statusEvent -> {

        CfLog.OUT.v(logTag, "SSE event received: " + statusEvent.getEventType());

        if (!ready.get()) {

            CfLog.OUT.w(logTag, "SSE event ignored, client is not ready");
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
                CfLog.OUT.v(logTag, "SSE connection resumed, reloading all evaluations");


                final List<Evaluation> evaluations = featureRepository.getAllEvaluations(

                        environmentID,
                        target.getIdentifier(),
                        cluster
                );

                statusEvent = new StatusEvent(statusEvent.getEventType(), evaluations);


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

                final Evaluation evaluation = statusEvent.extractPayload();
                final Evaluation e = featureRepository.getEvaluation(

                        authInfo.getEnvironmentIdentifier(),
                        target.getIdentifier(),
                        evaluation.getFlag(),
                        cluster
                );

                statusEvent = new StatusEvent(statusEvent.getEventType(), e);
                notifyListeners(e);

                break;

            case EVALUATION_REMOVE:

                final Evaluation eval = statusEvent.extractPayload();
                featureRepository.remove(authInfo.getEnvironmentIdentifier(), target.getIdentifier(), eval.getFlag());
                break;

            case EVALUATION_RELOAD:

                CfLog.OUT.v(logTag, "Reloading all evaluations");


                final List<Evaluation> evaluations = featureRepository.getAllEvaluations(

                        environmentID,
                        target.getIdentifier(),
                        cluster
                );

                statusEvent = new StatusEvent(statusEvent.getEventType(), evaluations);

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

    /**
     * Base constructor.
     */
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

        CfLog.OUT.v(logTag, "sendEvent(): " + statusEvent.getEventType());

        listenerUpdateExecutor.execute(() -> {

            for (final EventsListener listener : eventsListenerSet) {
                if (checkForInvalidEvent(statusEvent)) {
                    continue;
                }
                listener.onEventReceived(statusEvent);
            }
        });
    }

    private boolean checkForInvalidEvent(StatusEvent statusEvent) {
        if (statusEvent.getEventType() == StatusEvent.EVENT_TYPE.EVALUATION_RELOAD) {
            Object payload = statusEvent.extractPayload();
            return payload != null && !(payload instanceof List);
        }
        return false;
    }

    private void notifyListeners(final Evaluation evaluation) {

        if (evaluation == null) {

            CfLog.OUT.e(logTag, "Evaluation is null");
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

        CfLog.OUT.i(logTag, "Reschedule");

        executor.execute(() -> {

            try {

                if (!ready.get()) {

                    boolean success = cloud.initialize();
                    if (success) {

                        ready.set(true);
                        this.authInfo = cloud.getAuthInfo();

                        if (analyticsEnabled) {

                            final String environmentID = authInfo.getEnvironment();
                            final String cluster = authInfo.getCluster();

                            this.analyticsManager.destroy();
                            this.analyticsManager = getAnalyticsManager(

                                    configuration, environmentID, cluster
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

                CfLog.OUT.v(logTag, "Evaluations count: " + evaluations.size());

                if (useStream) {

                    startSSE(true);

                } else {

                    evaluationPolling.start(this::reschedule);
                }

            } catch (Exception e) {

                CfLog.OUT.e(logTag, e.getMessage(), e);

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

        CfLog.OUT.v(logTag, "Start SSE");

        final SSEConfig config = cloud.getConfig();

        if (config.isValid()) {

            sseController.start(config, eventsListener, isRescheduled);
        }
    }

    private synchronized void stopSSE() {

        CfLog.OUT.v(logTag, "Stop SSE");

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

    protected void doInitialize(

            final String apiKey,
            final CfConfiguration configuration,
            final Target target,
            final CloudCache cloudCache,
            @Nullable final AuthCallback authCallback
    ) {

        this.configuration = configuration;

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
                this.target = target;
                this.cloud = cloudFactory.cloud(

                        configuration.getStreamURL(),
                        configuration.getBaseURL(),
                        apiKey,
                        target
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
                    final AuthResult result = new AuthResult(false, e);
                    if (authCallback != null) {
                        authCallback.authorizationSuccess(null, result);
                    }
                    return;
                }
                if (success) {

                    this.authInfo = cloud.getAuthInfo();
                    this.sseController = cloudFactory.sseController(cloud, this.authInfo);

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

                        this.analyticsManager = getAnalyticsManager(

                                configuration, environmentID, cluster
                        );
                    }

                    if (authCallback != null) {

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

            CfLog.OUT.e(logTag, e.getMessage(), e);
            if (authCallback != null) {

                final AuthResult result = new AuthResult(false, e);
                authCallback.authorizationSuccess(authInfo, result);
            }

            destroy();
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
    private <T> Evaluation getEvaluationById(

            String evaluationId,
            String target,
            T defaultValue
    ) {

        Evaluation result = new Evaluation();

        if (ready.get()) {

            final String cluster = authInfo.getCluster();
            final String identifier = authInfo.getEnvironmentIdentifier();

            result = featureRepository.getEvaluation(

                    identifier, target, evaluationId, cluster
            );

        } else {

            result.value(defaultValue)
                    .flag(evaluationId);
        }

        if (result == null) {

            CfLog.OUT.w(logTag, "Result is null, creating the default one");

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

                CfLog.OUT.e(logTag, "Error adding into the metrics queue");
            }
        }

        return result;
    }

    public boolean boolVariation(String evaluationId, boolean defaultValue) {

        final Evaluation evaluation = getEvaluationById(

                evaluationId,
                target.getIdentifier(),
                defaultValue
        );

        final Object value = evaluation.getValue();
        if (value instanceof Boolean) {

            return (Boolean) value;
        }
        if (value instanceof String) {

            return "true".equals(value);
        }
        return defaultValue;
    }

    public String stringVariation(String evaluationId, String defaultValue) {

        return getEvaluationById(evaluationId, target.getIdentifier(), defaultValue).getValue();
    }

    public double numberVariation(String evaluationId, double defaultValue) {

        final Evaluation evaluation = getEvaluationById(

                evaluationId,
                target.getIdentifier(),
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

                CfLog.OUT.e(logTag, e.getMessage(), e);
            }
        }
        return defaultValue;
    }

    public JSONObject jsonVariation(String evaluationId, JSONObject defaultValue) {

        try {

            final Evaluation e = getEvaluationById(

                    evaluationId,
                    target.getIdentifier(),
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
                String eval = StringEscapeUtils.unescapeJava(e.getValue().toString());
                eval = eval.substring(1, eval.length() - 1);
                return new JSONObject(eval);
            }
        } catch (JSONException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
        return null;
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
    public void destroy() {

        if (analyticsManager != null) {

            analyticsManager.destroy();
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
            String environmentID,
            String cluster
    ) {

        return new AnalyticsManager(

                environmentID,
                cluster,
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

        target = null;
    }

    /* Package private */

    void setNetworkInfoProvider(NetworkInfoProviding networkInfoProvider) {
        this.networkInfoProvider = networkInfoProvider;
    }
}