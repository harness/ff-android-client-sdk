package io.harness.cfsdk.cloud.analytics;


import java.util.HashMap;
import java.util.Map;

import io.harness.cfsdk.BuildConfig;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.analytics.model.KeyValue;
import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.analytics.model.MetricsData;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.logging.CfLog;

/**
 * This class prepares the message body for metrics and posts it to the server
 */
public class AnalyticsPublisherService {

    private static final String CLIENT;
    private static final String SDK_TYPE;
    private static final String SDK_VERSION;
    private static final String SDK_LANGUAGE;
    private static final String GLOBAL_TARGET;
    private static final String TARGET_ATTRIBUTE;
    private static final String FEATURE_NAME_ATTRIBUTE;
    private static final String VARIATION_IDENTIFIER_ATTRIBUTE;

    static {

        CLIENT = "client";
        SDK_TYPE = "SDK_TYPE";
        TARGET_ATTRIBUTE = "target";
        SDK_VERSION = "SDK_VERSION";
        SDK_LANGUAGE = "SDK_LANGUAGE";
        GLOBAL_TARGET = "__global__cf_target";
        FEATURE_NAME_ATTRIBUTE = "featureName";
        VARIATION_IDENTIFIER_ATTRIBUTE = "variationIdentifier";
    }

    private final String logTag;
    private final String cluster;
    private final Cache analyticsCache;
    private final String environmentID;
    private final DefaultApi metricsAPI;
    private final CfConfiguration config;

    {

        logTag = AnalyticsPublisherService.class.getSimpleName();
    }

    public AnalyticsPublisherService(

            final String authToken,
            final CfConfiguration config,
            final String environmentID,
            final String cluster,
            final Cache analyticsCache
    ) {

        this.config = config;
        this.cluster = cluster;
        this.analyticsCache = analyticsCache;
        this.environmentID = environmentID;

        metricsAPI = MetricsApiFactory.create(authToken, config);
    }

    /**
     * This method sends the metrics data to the analytics server and resets the cache
     */
    public void sendDataAndResetCache() {

        CfLog.OUT.d(logTag, "Reading from queue and building cache");
        final Map<Analytics, Integer> all = analyticsCache.getAll();
        if (all.isEmpty()) {

            CfLog.OUT.d(logTag, "Cache is empty");
        } else {
            try {

                Metrics metrics = prepareSummaryMetricsBody(all);
                if (metrics.getMetricsData() != null && !metrics.getMetricsData().isEmpty()) {

                    long startTime = System.currentTimeMillis();
                    metricsAPI.postMetrics(environmentID, cluster, metrics);
                    long endTime = System.currentTimeMillis();
                    if ((endTime - startTime) > config.getMetricsServiceAcceptableDuration()) {
                        CfLog.OUT.w(logTag, "Metrics service API duration=" + (endTime - startTime));
                    }
                }

                CfLog.OUT.v(logTag, "Successfully sent analytics data to the server");
                analyticsCache.resetCache();
            } catch (ApiException e) {

                // Clear the set because the cache is only invalidated when there is no
                // exception, so the targets will reappear in the next iteration
                CfLog.OUT.e(logTag, e.getMessage(), e);
            }
        }
    }


    private Metrics prepareSummaryMetricsBody(Map<Analytics, Integer> data) {

        final Metrics metrics = new Metrics();
        final Map<SummaryMetrics, Integer> summaryMetricsData = new HashMap<>();

        for (Map.Entry<Analytics, Integer> entry : data.entrySet()) {

            final SummaryMetrics summaryMetrics = prepareSummaryMetricsKey(entry.getKey());
            final Integer summaryCount = summaryMetricsData.get(summaryMetrics);

            if (summaryCount == null) {
                summaryMetricsData.put(summaryMetrics, entry.getValue());
            } else {
                summaryMetricsData.put(summaryMetrics, summaryCount + entry.getValue());
            }
        }

        for (Map.Entry<SummaryMetrics, Integer> entry : summaryMetricsData.entrySet()) {

            MetricsData metricsData = new MetricsData();
            metricsData.setTimestamp(System.currentTimeMillis());
            metricsData.count(entry.getValue());
            metricsData.setMetricsType(MetricsData.MetricsTypeEnum.FFMETRICS);

            setMetricsAttributes(metricsData, FEATURE_NAME_ATTRIBUTE, entry.getKey().getFeatureName());
            setMetricsAttributes(metricsData, VARIATION_IDENTIFIER_ATTRIBUTE, entry.getKey().getVariationIdentifier());
            setMetricsAttributes(metricsData, TARGET_ATTRIBUTE, GLOBAL_TARGET);
            setMetricsAttributes(metricsData, SDK_TYPE, CLIENT);
            setMetricsAttributes(metricsData, SDK_LANGUAGE, "android");
            setMetricsAttributes(metricsData, SDK_VERSION, BuildConfig.APP_VERSION_NAME);

            metrics.addMetricsDataItem(metricsData);
        }
        return metrics;
    }

    private SummaryMetrics prepareSummaryMetricsKey(Analytics key) {

        return new SummaryMetrics(

                key.getFeatureConfig().getFeature(),
                key.getVariation().getValue(),
                key.getVariation().getIdentifier()
        );
    }

    private void setMetricsAttributes(MetricsData metricsData, String key, String value) {

        KeyValue metricsAttributes = new KeyValue();
        metricsAttributes.setKey(key);
        metricsAttributes.setValue(value);
        metricsData.addAttributesItem(metricsAttributes);
    }
}
