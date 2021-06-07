package io.harness.cfsdk.cloud.analytics;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.harness.cfsdk.BuildConfig;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.analytics.model.KeyValue;
import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.analytics.model.MetricsData;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;

/**
 * This class prepares the message body for metrics and posts it to the server
 */
public class AnalyticsPublisherService {

    private static final String FEATURE_NAME_ATTRIBUTE;
    private static final String VARIATION_VALUE_ATTRIBUTE;
    private static final String VARIATION_IDENTIFIER_ATTRIBUTE;
    private static final String TARGET_ATTRIBUTE;
    private static final Set<Target> globalTargetSet;
    private static final Set<Target> stagingTargetSet;
    private static final String JAR_VERSION;
    private static final String SDK_TYPE;
    private static final String CLIENT;
    private static final String SDK_LANGUAGE;
    private static final String SDK_VERSION;

    static {

        CLIENT = "client";
        SDK_TYPE = "SDK_TYPE";
        JAR_VERSION = "JAR_VERSION";
        TARGET_ATTRIBUTE = "target";
        SDK_VERSION = "SDK_VERSION";
        SDK_LANGUAGE = "SDK_LANGUAGE";
        globalTargetSet = new HashSet<>();
        stagingTargetSet = new HashSet<>();
        FEATURE_NAME_ATTRIBUTE = "featureName";
        VARIATION_VALUE_ATTRIBUTE = "featureValue";
        VARIATION_IDENTIFIER_ATTRIBUTE = "variationIdentifier";
    }

    private final String logTag;
    private final DefaultApi metricsAPI;
    private final Cache analyticsCache;
    private final String environmentID;

    {

        logTag = AnalyticsPublisherService.class.getSimpleName();
    }

    public AnalyticsPublisherService(

            final String authToken,
            final CfConfiguration config,
            final String environmentID,
            final Cache analyticsCache
    ) {

        metricsAPI = MetricsApiFactory.create(authToken, config);
        this.analyticsCache = analyticsCache;
        this.environmentID = environmentID;
    }

    /**
     * This method sends the metrics data to the analytics server and resets the cache
     */
    public void sendDataAndResetCache() {

        CfLog.OUT.d(logTag, "Reading from queue and building cache");
        final Map<Analytics, Integer> all = analyticsCache.getAll();
        if (!all.isEmpty()) {
            try {

                Metrics metrics = prepareMessageBody(all);
                CfLog.OUT.d(logTag, "metrics " + metrics);
                final List<MetricsData> metricsData = metrics.getMetricsData();
                if ((metricsData != null && !metricsData.isEmpty())) {
                    metricsAPI.postMetrics(environmentID, metrics);
                }
                globalTargetSet.addAll(stagingTargetSet);
                stagingTargetSet.clear();
                CfLog.OUT.d(logTag, "Successfully sent analytics data to the server");
                CfLog.OUT.i(logTag, "Invalidating the cache");
                analyticsCache.resetCache();
            } catch (ApiException e) {

                // Clear the set because the cache is only invalidated when there is no
                // exception, so the targets will reappear in the next iteration
                final String msg = String.format(
                        "Failed to send metricsData %s : %S", e.getMessage(), e.getCode()
                );
                CfLog.OUT.e(logTag, msg);
            }
        }
    }

    private Metrics prepareMessageBody(Map<Analytics, Integer> all) {
        Metrics metrics = new Metrics();

        // using for-each loop for iteration over Map.entrySet()
        for (Map.Entry<Analytics, Integer> entry : all.entrySet()) {

            // Set Metrics data
            MetricsData metricsData = new MetricsData();

            Analytics analytics = entry.getKey();

            final Target target = analytics.getTarget();
            final FeatureConfig featureConfig = analytics.getFeatureConfig();
            final Variation variation = analytics.getVariation();

            if (!globalTargetSet.contains(target)) {
                stagingTargetSet.add(target);
                final Map<String, Object> attributes = target.getAttributes();
                for (final String k : attributes.keySet()) {

                    final Object v = attributes.get(k);
                    KeyValue keyValue = new KeyValue();
                    keyValue.setKey(k);
                    keyValue.setValue(v.toString());
                }
            }

            metricsData.setTimestamp(System.currentTimeMillis());
            metricsData.count(entry.getValue());
            metricsData.setMetricsType(MetricsData.MetricsTypeEnum.FFMETRICS);
            setMetricsAttriutes(metricsData, FEATURE_NAME_ATTRIBUTE, featureConfig.getFeature());
            setMetricsAttriutes(metricsData, VARIATION_IDENTIFIER_ATTRIBUTE, variation.getIdentifier());
            setMetricsAttriutes(metricsData, VARIATION_VALUE_ATTRIBUTE, variation.getValue());
            setMetricsAttriutes(metricsData, TARGET_ATTRIBUTE, target.getIdentifier());
            setMetricsAttriutes(metricsData, JAR_VERSION, getVersion());
            setMetricsAttriutes(metricsData, SDK_TYPE, CLIENT);

            setMetricsAttriutes(metricsData, SDK_LANGUAGE, "android");
            setMetricsAttriutes(metricsData, SDK_VERSION, getVersion());
            metrics.addMetricsDataItem(metricsData);
        }

        return metrics;
    }

    private void setMetricsAttriutes(MetricsData metricsData, String key, String value) {

        KeyValue metricsAttributes = new KeyValue();
        metricsAttributes.setKey(key);
        metricsAttributes.setValue(value);
        metricsData.addAttributesItem(metricsAttributes);
    }

    private String getVersion() {

        return BuildConfig.APP_VERSION_NAME;
    }
}
