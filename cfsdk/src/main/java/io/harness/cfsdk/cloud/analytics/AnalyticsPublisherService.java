package io.harness.cfsdk.cloud.analytics;


import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
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
    private static final String FEATURE_IDENTIFIER_ATTRIBUTE;
    private static final String FEATURE_NAME_ATTRIBUTE;
    private static final String VARIATION_IDENTIFIER_ATTRIBUTE;
    private static final String VARIATION_VALUE_ATTRIBUTE;

    static {

        CLIENT = "client";
        SDK_TYPE = "SDK_TYPE";
        TARGET_ATTRIBUTE = "target";
        SDK_VERSION = "SDK_VERSION";
        SDK_LANGUAGE = "SDK_LANGUAGE";
        GLOBAL_TARGET = "__global__cf_target";
        FEATURE_IDENTIFIER_ATTRIBUTE = "featureIdentifier";
        FEATURE_NAME_ATTRIBUTE = "featureName";
        VARIATION_IDENTIFIER_ATTRIBUTE = "variationIdentifier";
        VARIATION_VALUE_ATTRIBUTE = "variationValue";
    }

    private final String logTag;
    private final String cluster;
    private final String authToken;
    private final String environmentID;
    private final CfConfiguration config;

    {

        logTag = AnalyticsPublisherService.class.getSimpleName();
    }

    public AnalyticsPublisherService(

            final String authToken,
            final CfConfiguration config,
            final String environmentID,
            final String cluster
    ) {

        this.config = config;
        this.cluster = cluster;
        this.authToken = authToken;
        this.environmentID = environmentID;
    }

    /**
     * This method sends the metrics data to the analytics server and resets the cache
     *
     * @param queue    Queue that contains data to be sent.
     * @param callback Sending results callback.
     */
    public void sendDataAndResetQueue(

            final BlockingQueue<Analytics> queue,
            final AnalyticsPublisherServiceCallback callback
    ) {

        CfLog.OUT.d(logTag, "Reading from queue and building cache");

        final Map<Analytics, Integer> all = new HashMap<>();

        for (Analytics analytics : queue) {

            if (analytics != null) {

                Integer count = all.get(analytics);
                if (count == null) {

                    count = 0;
                    all.put(analytics, count);

                } else {

                    count++;
                    all.put(analytics, count);
                }

                CfLog.OUT.v(

                        logTag,
                        String.format(

                                Locale.getDefault(),
                                "Preparing metrics: metric='%s', count=%d",
                                analytics.getEvaluationId(),
                                count
                        )
                );
            }
        }

        if (all.isEmpty()) {

            CfLog.OUT.d(logTag, "Cache is empty");
            callback.onAnalyticsSent(true);

        } else {

            CfLog.OUT.d(

                    logTag,
                    String.format(

                            Locale.getDefault(),
                            "Cache contains the metrics data, size=%d",
                            all.size()
                    )
            );

            try {

                final Metrics metrics = prepareSummaryMetricsBody(all);
                if (metrics.getMetricsData() != null && !metrics.getMetricsData().isEmpty()) {

                    long startTime = System.currentTimeMillis();

                    CfLog.OUT.v(logTag, "Sending metrics");

                    final MetricsApi metricsAPI = MetricsApiFactory.create(authToken, config);
                    metricsAPI.postMetrics(environmentID, cluster, metrics);

                    long endTime = System.currentTimeMillis();

                    if ((endTime - startTime) > config.getMetricsServiceAcceptableDurationInMillis()) {

                        CfLog.OUT.w(logTag, "Metrics service API duration=" + (endTime - startTime));
                    }

                    CfLog.OUT.v(logTag, "Successfully sent analytics data to the server");

                } else {

                    CfLog.OUT.v(logTag, "No analytics data to send the server");
                }

                boolean queueCleared = true;
                for (final Analytics analytics : all.keySet()) {

                    while (queue.contains(analytics)) {

                        if (queue.remove(analytics)) {

                            CfLog.OUT.v(

                                    logTag,
                                    "Metrics item was removed from the queue: "
                                            + analytics.getEvaluationId()
                            );

                        } else {

                            CfLog.OUT.e(

                                    logTag,
                                    "Metrics item was not removed from the queue: "
                                            + analytics.getEvaluationId()
                            );

                            queueCleared = false;
                        }
                    }
                }

                if (queueCleared) {

                    CfLog.OUT.v(logTag, "Queue is cleared, size=" + queue.size());
                }

                callback.onAnalyticsSent(true);

            } catch (ApiException e) {

                CfLog.OUT.e(logTag, "Error sending metrics", e);
                callback.onAnalyticsSent(false);
            }
        }
    }


    private Metrics prepareSummaryMetricsBody(Map<Analytics, Integer> data) {

        CfLog.OUT.v(logTag, "Data size: " + data.size());

        final Metrics metrics = new Metrics();
        final Map<SummaryMetrics, Integer> summaryMetricsData = new HashMap<>();

        for (Analytics analytics : data.keySet()) {

            Integer count = data.get(analytics);

            if (count == null) {

                count = 0;
            }

            final SummaryMetrics summaryMetrics = prepareSummaryMetricsKey(analytics);
            final Integer summaryCount = summaryMetricsData.get(summaryMetrics);

            if (summaryCount == null) {

                summaryMetricsData.put(summaryMetrics, count + 1);
            } else {

                summaryMetricsData.put(summaryMetrics, summaryCount + count);
            }

            CfLog.OUT.v(

                    logTag,
                    String.format(

                            "Summary metrics appended: %s, %s",
                            summaryMetrics,
                            summaryMetricsData.get(summaryMetrics)
                    )
            );
        }

        CfLog.OUT.v(logTag, "Summary metrics size: " + summaryMetricsData.size());

        final Set<Map.Entry<SummaryMetrics, Integer>> summaryEntrySet = summaryMetricsData.entrySet();

        for (Map.Entry<SummaryMetrics, Integer> entry : summaryEntrySet) {

            MetricsData metricsData = new MetricsData();
            metricsData.setTimestamp(System.currentTimeMillis());
            metricsData.count(entry.getValue());
            metricsData.setMetricsType(MetricsData.MetricsTypeEnum.FFMETRICS);

            setMetricsAttributes(metricsData, FEATURE_IDENTIFIER_ATTRIBUTE, entry.getKey().getFeatureName());
            setMetricsAttributes(metricsData, FEATURE_NAME_ATTRIBUTE, entry.getKey().getFeatureName());
            setMetricsAttributes(metricsData, VARIATION_IDENTIFIER_ATTRIBUTE, entry.getKey().getVariationIdentifier());
            setMetricsAttributes(metricsData, VARIATION_VALUE_ATTRIBUTE, entry.getKey().getVariationValue());
            setMetricsAttributes(metricsData, TARGET_ATTRIBUTE, entry.getKey().getTarget());
            setMetricsAttributes(metricsData, SDK_TYPE, CLIENT);
            setMetricsAttributes(metricsData, SDK_LANGUAGE, "android");
            setMetricsAttributes(metricsData, SDK_VERSION, "1.0.15");

            metrics.addMetricsDataItem(metricsData);
        }

        final List<MetricsData> mData = metrics.getMetricsData();

        if (mData != null) {

            CfLog.OUT.v(logTag, "Metrics data size: " + mData.size());

        } else {

            CfLog.OUT.w(logTag, "Metrics data size: no data");
        }

        return metrics;
    }

    private SummaryMetrics prepareSummaryMetricsKey(Analytics key) {

        return new SummaryMetrics(

                key.getVariation().getName(),
                key.getVariation().getValue(),
                key.getVariation().getIdentifier(),
                key.getTarget().getIdentifier()
        );
    }

    private void setMetricsAttributes(

            final MetricsData metricsData,
            final String key,
            final String value
    ) {

        KeyValue metricsAttributes = new KeyValue();
        metricsAttributes.setKey(key);
        metricsAttributes.setValue(value);
        metricsData.addAttributesItem(metricsAttributes);
    }
}
