package io.harness.cfsdk;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.NetworkInfoProvider;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.events.AuthCallback;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.model.StatusEvent;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.cloud.oksse.EventsListener;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.sse.SSEController;

/**
 * Main class used for any operation on SDK. Operations include, but not limited to, reading evaluations and
 * observing changes in state of SDK.
 * Before it can be used, one of the {@link CFClient#initialize} methods <strong>must be</strong>  called
 */
public final class CFClient {
    private volatile boolean ready = false;

    private Cloud cloud;

    private String target;
    private AuthInfo authInfo;
    private EvaluationPolling evaluationPolling;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Executor listenerUpdateExecutor = Executors.newSingleThreadExecutor();
    private boolean useStream;

    private final ConcurrentHashMap<String, Set<EvaluationListener>> evaluationListenerSet = new ConcurrentHashMap<>();
    private final Set<EventsListener> eventsListenerSet = Collections.synchronizedSet(new LinkedHashSet<>());

    private FeatureRepository featureRepository;
    private SSEController sseController;
    private NetworkInfoProvider networkInfoProvider;
    private final CloudFactory cloudFactory;

    private static CFClient instance;

    private final EventsListener eventsListener = statusEvent -> {
        if (!ready) return;
        switch (statusEvent.getEventType()) {
            case SSE_START:
                evaluationPolling.stop();
                break;
            case SSE_END:
                if (networkInfoProvider.isNetworkAvailable()) {
                    this.featureRepository.getAllEvaluations(authInfo.getEnvironmentIdentifier(), target, false);
                    evaluationPolling.start(new Runnable() {
                        @Override
                        public void run() {
                            reschedule();
                        }
                    });
                }
                break;
            case EVALUATION_CHANGE:
                Evaluation evaluation = statusEvent.extractPayload();
                Evaluation e = featureRepository.getEvaluation(authInfo.getEnvironmentIdentifier(), target, evaluation.getFlag(), false);
                notifyListeners(e);
                break;
            case EVALUATION_REMOVE:
                Evaluation eval = statusEvent.extractPayload();
                featureRepository.remove(authInfo.getEnvironmentIdentifier(), target, eval.getFlag());
                break;
        }
        sendEvent(statusEvent);
    };

    /**
     * Base constructor, used internally. Use {@link CFClient#getInstance()} to get instance of this class.
     */
    CFClient(CloudFactory cloudFactory) {
        this.cloudFactory = cloudFactory;
    }

    /**
     * Retrieves the single instance of {@link CFClient} to be used for SDK operation
     * @return single instance used as entry point of SDK
     */
    public static CFClient getInstance() {
        if (instance == null) instance = new CFClient(new CloudFactory());
        return instance;
    }

    private void sendEvent(StatusEvent statusEvent) {
        listenerUpdateExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Iterator<EventsListener> iterator = eventsListenerSet.iterator();
                while (iterator.hasNext()) {
                    EventsListener listener = iterator.next();
                    listener.onEventReceived(statusEvent);
                }
            }
        });
    }

    private void notifyListeners(Evaluation evaluation) {
        if (evaluationListenerSet.containsKey(evaluation.getFlag())) {
            Set<EvaluationListener> callbacks = evaluationListenerSet.get(evaluation.getFlag());
            for (EvaluationListener listener : callbacks) {
                listener.onEvaluation(evaluation);
            }
        }
    }

    private void reschedule() {
        executor.execute(() -> {
            try {
                if(!ready) {
                    boolean success = cloud.initialize();
                    if (success) {
                        ready = true;
                        this.authInfo = cloud.getAuthInfo();
                    }
                }
                if (!ready) return;
                List<Evaluation> evaluations = this.featureRepository.getAllEvaluations(authInfo.getEnvironmentIdentifier(), target, false);
                sendEvent(new StatusEvent(StatusEvent.EVENT_TYPE.EVALUATION_RELOAD, evaluations));

                if (useStream) startSSE();
                else evaluationPolling.start(new Runnable() {
                    @Override
                    public void run() {
                        reschedule();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                if (networkInfoProvider.isNetworkAvailable()) {
                    evaluationPolling.start(new Runnable() {
                        @Override
                        public void run() {
                            reschedule();
                        }
                    });
                }
            }
        });
    }


    private void setupNetworkInfo(Context context) {
        if (networkInfoProvider != null) {
            networkInfoProvider.unregisterAll();
        } else networkInfoProvider = cloudFactory.networkInfoProvider(context);

        networkInfoProvider.register(new NetworkInfoProvider.NetworkListener() {
            @Override
            public void onChange(NetworkInfoProvider.NetworkStatus status) {
                if (status == NetworkInfoProvider.NetworkStatus.CONNECTED) {
                    reschedule();
                } else evaluationPolling.stop();
            }
        });

    }

    private synchronized void startSSE() {
        SSEConfig config = cloud.getConfig();
        if (config.isValid()) sseController.start(config, eventsListener);
    }

    private synchronized void stopSSE() {
        this.useStream = false;
        if (sseController != null) sseController.stop();
    }


    /**
     * Initialize the client and sets up needed dependencies. Upon called, it is dispatched to another thread and result is returned trough
     * provided {@link AuthCallback} instance.
     * @param context Context of application
     * @param clientId API key used for authentication
     * @param configuration Collection of different configuration flags, which defined the behaviour of SDK
     * @param cloudCache Custom {@link CloudCache} implementation. If non provided, the default implementation will be used
     * @param authCallback The callback that will be invoked when initialization is finished
     */
    public void initialize(Context context, String clientId, CFConfiguration configuration, CloudCache cloudCache, AuthCallback authCallback) {
        executor.execute(() -> {
            unregister();
            this.cloud = cloudFactory.cloud(configuration.getStreamURL(), configuration.getBaseURL(), clientId);
            setupNetworkInfo(context);
            featureRepository = cloudFactory.getFeatureRepository(cloud, cloudCache);
            sseController = cloudFactory.sseController();
            evaluationPolling = cloudFactory.evaluationPolling(configuration.getPollingInterval(), TimeUnit.SECONDS);

            this.target = configuration.getTarget();
            this.useStream = configuration.getStreamEnabled();

            boolean success = cloud.initialize();
            if (success) {
                this.authInfo = cloud.getAuthInfo();
                ready = true;
                if (networkInfoProvider.isNetworkAvailable()) {
                    featureRepository.getAllEvaluations(this.authInfo.getEnvironmentIdentifier(), target, false);
                    if (useStream) {
                        startSSE();
                    } else {
                        evaluationPolling.start(new Runnable() {
                            @Override
                            public void run() {
                                reschedule();
                            }
                        });
                    }
                }

                if (authCallback != null)
                    authCallback.authorizationSuccess(authInfo);
            }
        });
    }

    public void initialize(Context context, String clientId, CFConfiguration configuration, AuthCallback authCallback) {
        initialize(context, clientId, configuration, cloudFactory.defaultCache(context), authCallback);
    }

    public void initialize(Context context, String clientId, CFConfiguration configuration, CloudCache cloudCache) {
        initialize(context, clientId, configuration, cloudCache, null);
    }
    
    public void initialize(Context context, String clientId, CFConfiguration configuration) {
        initialize(context, clientId, configuration, cloudFactory.defaultCache(context), null);
    }

    /**
     * Register a listener to observe changes on a evaluation with given id. The change <strong>will not</strong> be triggered
     * in case of reloading all evaluations, but only when single evaluation is changed.
     * It is possible to register multiple observers for a single evaluatio.
     * @param evaluationId Evaluation identifier we would like to observe.
     * @param listener {@link EvaluationListener} instance that will be invoked when evaluation is changed
     */
    public void registerEvaluationListener(String evaluationId, EvaluationListener listener) {
        if (listener == null) return;
        Set<EvaluationListener> set = this.evaluationListenerSet.get(evaluationId);
        if (set == null) set = new HashSet<>();
        set.add(listener);
        this.evaluationListenerSet.put(evaluationId, set);
    }


    /**
     * Removes specified listener for an evaluation with given id.
     * @param evaluationId Evaluation identifier.
     * @param listener {@link EvaluationListener} instance we want to remove
     */
    public void unregisterEvaluationListener(String evaluationId, EvaluationListener listener) {
        if (listener == null) return;
        Set<EvaluationListener> set = this.evaluationListenerSet.get(evaluationId);
        if (set == null) return;
        set.remove(listener);
    }


    /**
     * Retrieves single {@link Evaluation instance} based on provided id. If no such evaluation is found,
     * returns one with provided default value.
     * @param evaluationId Identifier of target evaluation
     * @param defaultValue Default value to be used in case when evaluation is not found
     * @return Evaluation for a given id
     */
    private <T> Evaluation getEvaluationById(String evaluationId, String target, T defaultValue) {
        Evaluation result = new Evaluation();
        if (ready) {
            Evaluation evaluation = featureRepository.getEvaluation(authInfo.getEnvironmentIdentifier(), target, evaluationId, true);
            if (evaluation == null) {
                result.value(defaultValue);
                result.flag(evaluationId);
            } else {
                result.flag(evaluation.getFlag());
                result.value(evaluation.getValue());
            }
        } else {
            result.value(defaultValue);
            result.flag(evaluationId);
        }
        return result;
    }

    public boolean boolVariation(String evaluationId, String target, boolean defaultValue) {
        return getEvaluationById(evaluationId, target, defaultValue).getValue();
    }

    public String stringVariation(String evaluationId, String target, String defaultValue) {
        return getEvaluationById(evaluationId, target, defaultValue).getValue();
    }

    public double numberVariation(String evaluationId, String target, int defaultValue) {
        return ((Number)getEvaluationById(evaluationId, target, defaultValue).getValue()).doubleValue();
    }

    public JSONObject jsonVariation(String evaluationId, String target, JSONObject defaultValue) {
        try {
            return new JSONObject((String)getEvaluationById(evaluationId, target, defaultValue).getValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    /**
     * Adds new listener for various SDK events. See {@link StatusEvent.EVENT_TYPE} for possible types.
     * @param observer {@link EventsListener} implementation that will be triggered when there is a change in state of SDK
     */
    public void registerEventsListener(EventsListener observer) {
        if (observer != null) eventsListenerSet.add(observer);
    }

    /**
     * Removes registered listener from list of registered events listener
     * @param observer {@link EventsListener} implementation that needs to be removed
     */
    public void unregisterEventsListener(EventsListener observer) {
        eventsListenerSet.remove(observer);
    }

    /**
     * Clears the occupied resources and shut's down the sdk.
     * After calling this method, the {@link #initialize} must be called again. It will also
     * remove any registered event listeners.
     */
    public void destroy() {
        unregister();
        this.evaluationListenerSet.clear();
        eventsListenerSet.clear();
    }

    private void unregister() {
        ready = false;
        stopSSE();
        if (evaluationPolling != null) evaluationPolling.stop();
        if (featureRepository != null) featureRepository.clear();

    }
}