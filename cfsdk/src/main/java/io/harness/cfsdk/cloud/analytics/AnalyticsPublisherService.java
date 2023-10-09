package io.harness.cfsdk.cloud.analytics;


import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
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
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.common.SdkCodes;

/**
 * This class prepares the message body for metrics and posts it to the server
 */
public class AnalyticsPublisherService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsPublisherService.class);

    private static final String CLIENT = "client";
    private static final String SDK_TYPE = "SDK_TYPE";
    private static final String SDK_VERSION = "SDK_VERSION";
    private static final String SDK_LANGUAGE = "SDK_LANGUAGE";
    private static final String TARGET_ATTRIBUTE = "target";
    private static final String FEATURE_IDENTIFIER_ATTRIBUTE = "featureIdentifier";
    private static final String FEATURE_NAME_ATTRIBUTE = "featureName";
    private static final String VARIATION_IDENTIFIER_ATTRIBUTE = "variationIdentifier";
    private static final String VARIATION_VALUE_ATTRIBUTE = "variationValue";

    private final String authToken;
    private final CfConfiguration config;
    private final AuthInfo authInfo;

    public AnalyticsPublisherService(
            final String authToken,
            final CfConfiguration config,
            final AuthInfo authInfo
    ) {
        this.config = config;
        this.authToken = authToken;
        this.authInfo = authInfo;
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
        log.debug("Reading from queue and building cache");
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

                log.debug("Preparing metrics: metric='{}', count={}", analytics.getEvaluationId(), count);
            }
        }

        if (all.isEmpty()) {
            log.debug("Cache is empty");
            callback.onAnalyticsSent(true);

        } else {
            log.debug("Cache contains the metrics data, size={}", all.size());

            try {

                final Metrics metrics = prepareSummaryMetricsBody(all);
                if (metrics.getMetricsData() != null && !metrics.getMetricsData().isEmpty()) {
                    long startTime = System.currentTimeMillis();

                    log.debug("Sending metrics");

                    final MetricsApi metricsAPI = MetricsApiFactory.create(authToken, config, authInfo);
                    metricsAPI.postMetrics(authInfo.getEnvironment(), authInfo.getCluster(), metrics);

                    long endTime = System.currentTimeMillis();

                    if ((endTime - startTime) > config.getMetricsServiceAcceptableDurationInMillis()) {
                        log.debug("Metrics service API duration={}", endTime - startTime);
                    }
                    log.debug("Successfully sent analytics data to the server");

                } else {
                    log.debug("No analytics data to send the server");
                }

                boolean queueCleared = true;
                for (final Analytics analytics : all.keySet()) {

                    while (queue.contains(analytics)) {

                        if (queue.remove(analytics)) {
                            log.debug("Metrics item was removed from the queue: {}", analytics.getEvaluationId());
                        } else {
                            log.debug("Metrics item was not removed from the queue: {}", analytics.getEvaluationId());
                            queueCleared = false;
                        }
                    }
                }

                if (queueCleared) {
                    log.debug("Queue is cleared, size={}", queue.size());
                }

                callback.onAnalyticsSent(true);

            } catch (ApiException e) {

                SdkCodes.warnPostMetricsFailed(e.getMessage());
                callback.onAnalyticsSent(false);
            }
        }
    }


    private Metrics prepareSummaryMetricsBody(Map<Analytics, Integer> data) {

        log.debug("Data size: {}", data.size());

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

            log.debug("Summary metrics appended: {}, {}", summaryMetrics, summaryMetricsData.get(summaryMetrics));
        }

        log.debug("Summary metrics size: {}", summaryMetricsData.size());

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
            setMetricsAttributes(metricsData, SDK_VERSION, ANDROID_SDK_VERSION);

            metrics.addMetricsDataItem(metricsData);
        }

        final List<MetricsData> mData = metrics.getMetricsData();

        if (mData != null) {
            log.debug("Metrics data size: {}", mData.size());
        } else {
            log.warn("Metrics data size: no data");
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
