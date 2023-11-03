package io.harness.cfsdk.cloud.analytics;


import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.openapi.metric.api.MetricsApi;
import io.harness.cfsdk.cloud.openapi.metric.model.KeyValue;
import io.harness.cfsdk.cloud.openapi.metric.model.Metrics;
import io.harness.cfsdk.cloud.openapi.metric.model.MetricsData;
import io.harness.cfsdk.cloud.openapi.metric.ApiException;
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

    private final AuthInfo authInfo;
    private final MetricsApi metricsApi;
    private final AtomicLong metricsSent = new AtomicLong();

    public AnalyticsPublisherService(
            final CfConfiguration config,
            final AuthInfo authInfo,
            final String authToken
    ) {
        this.authInfo = authInfo;
        this.metricsApi = MetricsApiFactory.create(authToken, config, authInfo);
    }

    public AnalyticsPublisherService(
            final AuthInfo authInfo,
            final MetricsApi metricsAPI) {
        this.authInfo = authInfo;
        this.metricsApi = metricsAPI;
    }

    /**
     * This method sends the metrics data to the analytics server and resets the cache
     *
     * @param freqMap    Map that contains counters to be sent.
     * @param callback Sending results callback.
     */
    public void sendData(
            final Map<Analytics, Long> freqMap,
            final AnalyticsPublisherServiceCallback callback) {

        if (log.isDebugEnabled())
            log.debug("Preparing to send metric payload mapSize={} totalMapSum={}", freqMap.size(), sumOfValuesInMap(freqMap));

        if (freqMap.isEmpty()) {

            log.debug("freqMap is empty");

            callback.onAnalyticsSent(true);
            return;
        }

        try {
            final Metrics metrics = prepareSummaryMetricsBody(freqMap);

            if (metrics.getMetricsData() != null && !metrics.getMetricsData().isEmpty()) {
                log.debug("Posting metrics");

                if (log.isTraceEnabled())
                    log.trace("metrics payload: {}", metrics);

                final long evalSum = sumOfValuesInMap(freqMap);

                if (evalSum > 0) {
                    metricsApi.postMetrics(authInfo.getEnvironment(), authInfo.getCluster(), metrics);
                    metricsSent.addAndGet(evalSum);
                    log.debug("Successfully sent analytics data to the server");
                } else {
                    log.debug("Sum of metric evaluations is 0 - metrics post skipped");
                }

            } else {
                log.debug("No analytics data to send the server");
            }

            callback.onAnalyticsSent(true);

        } catch (ApiException e) {

            SdkCodes.warnPostMetricsFailed(e.getMessage());
            callback.onAnalyticsSent(false);
        }
    }

    private Map<SummaryMetrics, Long> rollUpMetrics(Map<Analytics, Long> detailedMetrics) {
        final Map<SummaryMetrics, Long> summaryMetricsData = new HashMap<>();

        log.debug("roll up: detailed metrics size {}", detailedMetrics.size());

        for (Map.Entry<Analytics, Long> analytic : detailedMetrics.entrySet()) {

            Long count = analytic.getValue();

            if (count == null) {
                count = 0L;
            }

            final SummaryMetrics summaryMetrics = prepareSummaryMetricsKey(analytic.getKey());
            final Long summaryCount = summaryMetricsData.get(summaryMetrics);

            if (summaryCount == null) {
                summaryMetricsData.put(summaryMetrics, count + 1);
            } else {
                summaryMetricsData.put(summaryMetrics, summaryCount + count);
            }

            if (log.isTraceEnabled())
                log.trace("Summary metrics appended: {}, {}", summaryMetrics, summaryMetricsData.get(summaryMetrics));
        }

        log.debug("roll up: summarised metrics size {}", summaryMetricsData.size());

        return summaryMetricsData;
    }


    private Metrics prepareSummaryMetricsBody(Map<Analytics, Long> data) {

        final Map<SummaryMetrics, Long> summaryMetricsData = rollUpMetrics(data);

        final Set<Map.Entry<SummaryMetrics, Long>> summaryEntrySet = summaryMetricsData.entrySet();

        final Metrics metrics = new Metrics();
        for (Map.Entry<SummaryMetrics, Long> entry : summaryEntrySet) {

            MetricsData metricsData = new MetricsData();
            metricsData.setTimestamp(System.currentTimeMillis());
            metricsData.count(entry.getValue().intValue());
            metricsData.setMetricsType(MetricsData.MetricsTypeEnum.FFMETRICS);

            setMetricsAttributes(metricsData, FEATURE_IDENTIFIER_ATTRIBUTE, entry.getKey().getFeatureName());
            setMetricsAttributes(metricsData, FEATURE_NAME_ATTRIBUTE, entry.getKey().getFeatureName());
            setMetricsAttributes(metricsData, VARIATION_IDENTIFIER_ATTRIBUTE, entry.getKey().getVariationIdentifier());
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

    private long sumOfValuesInMap(Map<?, Long> map) {
        return map.values().stream().mapToLong(l -> l).sum();
    }

    long getMetricsSent() {
        return metricsSent.get();
    }
}
