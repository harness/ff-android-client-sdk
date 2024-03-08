package io.harness.cfsdk;

import static java.util.concurrent.TimeUnit.SECONDS;

import static io.harness.cfsdk.utils.CfUtils.Text.isEmpty;

import android.content.Context;
import androidx.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherService;
import io.harness.cfsdk.cloud.events.EvaluationListener;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;
import io.harness.cfsdk.cloud.openapi.client.model.Variation;
import io.harness.cfsdk.cloud.sse.EventsListener;
import io.harness.cfsdk.common.SdkCodes;
import io.harness.cfsdk.cloud.events.AuthCallback;
public class CfClient implements Closeable, Client {

    private static final Logger log = LoggerFactory.getLogger(CfClient.class);
    private static volatile CfClient instance;
    private final Set<EventsListener> eventsListenerSet = Collections.synchronizedSet(new LinkedHashSet<>());
    private final ConcurrentHashMap<String, Set<EvaluationListener>> evaluationListenerSet = new ConcurrentHashMap<>();
    private final ExecutorService threadExecutor = Executors.newFixedThreadPool(2);
    private SdkThread sdkThread;
    private AnalyticsManager metricsThread;
    private CfConfiguration configuration;

    public CfClient() {
    }

    public static CfClient getInstance() {
        if (CfClient.instance == null) {
            synchronized (CfClient.class) {
                if (CfClient.instance == null) {
                    CfClient.instance = new CfClient();
                }
            }
        }
        return CfClient.instance;
    }

    @Override
    public void initialize(
            final Context context,
            final String apiKey,
            final CfConfiguration configuration,
            final Target target

    ) throws IllegalStateException {
        initializeInternal(context, apiKey, configuration, target, null, null);
    }

    private void initializeInternal(final Context context, final String apiKey, final CfConfiguration config, final Target target, final CloudCache cloudCache, @Nullable final AuthCallback authCallback) {
        this.configuration = config;

        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                SdkCodes.errorMissingSdkKey();
                throw new IllegalArgumentException("Missing SDK key");
            }

            if (target == null || configuration == null) {
                throw new IllegalArgumentException("Target and configuration must not be null!");
            }

            if (!target.isValid()) {
                throw new IllegalArgumentException("Target not valid");
            }

            if (cloudCache != null) {
                configuration.setCache(cloudCache);
            }

            setTargetDefaults(target);

            sdkThread = new SdkThread(context, apiKey, configuration, target, evaluationListenerSet, eventsListenerSet, authCallback);
            threadExecutor.execute(sdkThread);

            if (configuration.isAnalyticsEnabled()) {
                if (isEmpty(configuration.getEventURL())) {
                    throw new IllegalArgumentException("Event URL is null or empty");
                }
                sdkThread.waitForInitialization(0);
                metricsThread = new AnalyticsManager(context, configuration, target, new AnalyticsPublisherService(configuration, sdkThread.getBearerToken(), sdkThread.getAuthInfo()));
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean waitForInitialization(long timeoutMs) {
        if (timeoutMs < 2000) {
            timeoutMs = 2000;
        }

        if (sdkThread == null) throw new IllegalStateException("SDK not initialized");
        return sdkThread.waitForInitialization(timeoutMs);
    }

    @Override
    public void waitForInitialization() throws InterruptedException {
        if (sdkThread == null) throw new IllegalStateException("SDK not initialized");
        sdkThread.waitForInitialization(0L);
    }

    @Override
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

    @Override
    public boolean unregisterEvaluationListener(String evaluationId, EvaluationListener listener) {
        if (listener != null) {
            Set<EvaluationListener> set = this.evaluationListenerSet.get(evaluationId);
            if (set != null) {
                return set.remove(listener);
            }
        }
        return false;
    }

    @Override
    public boolean registerEventsListener(final EventsListener observer) {
        if (observer != null) {
            return eventsListenerSet.add(observer);
        }
        return false;
    }

    @Override
    public boolean unregisterEventsListener(final EventsListener observer) {
        return eventsListenerSet.remove(observer);
    }

    @Override
    public boolean boolVariation(String evaluationId, boolean defaultValue) {
        final StringBuilder failureReason = new StringBuilder();
        final Evaluation evaluation = sdkThread.getEvaluationById(evaluationId, failureReason);

        if (evaluation == null || evaluation.getValue() == null) {
            if (failureReason.length() == 0) {
                failureReason.append(evaluationId).append(" not in cache");
            }

            SdkCodes.warnDefaultVariationServed(evaluationId, String.valueOf(defaultValue), failureReason.toString());
            return defaultValue;
        }

        pushToMetrics(evaluationId, evaluation);
        return "true".equals(evaluation.getValue());
    }

    @Override
    public String stringVariation(String evaluationId, String defaultValue) {
        final StringBuilder failureReason = new StringBuilder();
        final Evaluation evaluation = sdkThread.getEvaluationById(evaluationId, failureReason);

        if (evaluation == null || evaluation.getValue() == null) {
            if (failureReason.length() == 0) {
                failureReason.append(evaluationId).append(" not in cache");
            }

            SdkCodes.warnDefaultVariationServed(evaluationId, String.valueOf(defaultValue), failureReason.toString());
            return defaultValue;
        }

        pushToMetrics(evaluationId, evaluation);
        return evaluation.getValue();
    }

    @Override
    public double numberVariation(String evaluationId, double defaultValue) {
        final StringBuilder failureReason = new StringBuilder();
        final Evaluation evaluation = sdkThread.getEvaluationById(evaluationId, failureReason);

        if (evaluation == null || evaluation.getValue() == null) {
            if (failureReason.length() == 0) {
                failureReason.append(evaluationId).append(" not in cache");
            }

            SdkCodes.warnDefaultVariationServed(evaluationId, String.valueOf(defaultValue), failureReason.toString());
            return defaultValue;
        }

        try {
            double parsedVal = Double.parseDouble(evaluation.getValue());
            pushToMetrics(evaluationId, evaluation);
            return parsedVal;

        } catch (NumberFormatException e) {
            SdkCodes.warnDefaultVariationServed(evaluationId, String.valueOf(defaultValue), "Failed to parse double: " + e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public JSONObject jsonVariation(String evaluationId, JSONObject defaultValue) {
        final StringBuilder failureReason = new StringBuilder();
        final Evaluation evaluation = sdkThread.getEvaluationById(evaluationId, failureReason);

        if (evaluation == null || evaluation.getValue() == null) {
            if (failureReason.length() == 0) {
                failureReason.append(evaluationId).append(" not in cache");
            }

            SdkCodes.warnDefaultVariationServed(evaluationId, String.valueOf(defaultValue), failureReason.toString());
            return defaultValue;
        }

        try {
            String originalValue = evaluation.getValue();

            // Remove outer quotes if they exist. Having to unescape by hand as we don't want
            // to use large libraries like commons/gson
            if (originalValue.startsWith("\"") && originalValue.endsWith("\"") && originalValue.length() > 1) {
                originalValue = originalValue.substring(1, originalValue.length() - 1);
            }

            // Replace escaped inner quotes
            originalValue = originalValue.replace("\\\"", "\"");

            JSONObject parsedVal = new JSONObject(originalValue);
            // Attempt to parse the evaluation value as a JSONObject
            pushToMetrics(evaluationId, evaluation);
            return parsedVal;
        } catch (JSONException e) {
            log.warn("Error parsing evaluation value as JSONObject: " + e.getMessage(), e);
            SdkCodes.warnDefaultVariationServed(evaluationId, defaultValue.toString(), "JSONException: " + e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public void refreshEvaluations() {
        if (sdkThread != null) {
            threadExecutor.submit(() -> sdkThread.refreshEvaluations());
        }
    }

    @Deprecated
    @Override
    public void initialize(final Context context, final String apiKey, final CfConfiguration config,
            final Target target, final CloudCache cloudCache, @Nullable final AuthCallback authCallback) throws IllegalStateException {
        initializeInternal(context, apiKey, config, target, cloudCache, authCallback);
    }

    @Override
    public void initialize(final Context context, final String apiKey, final CfConfiguration config,
                           final Target target, final AuthCallback authCallback) throws IllegalStateException {
        initializeInternal(context, apiKey, config, target, null, authCallback);
    }

    @Deprecated
    @Override
    public void initialize(final Context context, final String apiKey, final CfConfiguration config,
            final Target target, final CloudCache cloudCache) throws IllegalStateException {
        initializeInternal(context, apiKey, config, target, cloudCache, null);
    }

    @Override
    public void close() {
        log.debug("Closing SDK");

        if (metricsThread != null) {
            metricsThread.close();
        }

        threadExecutor.shutdownNow();

        synchronized (CfClient.class) {
            if (instance == this) {
                instance = null;
            }
        }
    }

    void setTargetDefaults(Target target) {
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

    void pushToMetrics(String evaluationId, Evaluation result) {
        if (configuration.isAnalyticsEnabled() && metricsThread != null) {
            final Variation variation = new Variation();
            variation.setName(evaluationId);
            variation.setValue(result.getValue());
            variation.setIdentifier(result.getIdentifier());
            metricsThread.registerEvaluation(evaluationId, variation);
        }
    }

    void sleepSeconds(int seconds) {
        try {
            SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
