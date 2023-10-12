package io.harness.cfsdk.cloud.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.common.SdkCodes;

public class AnalyticsManager implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsManager.class);

    private final AnalyticsPublisherService analyticsPublisherService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final FrequencyMap<Analytics> frequencyMap;
    private final CfConfiguration config;

    private static class FrequencyMap<K> {

        private final ConcurrentHashMap<K, Long> freqMap;

        FrequencyMap() {
            freqMap = new ConcurrentHashMap<>();
        }

        void increment(K key) {
            freqMap.compute(key, (k, v) -> (v == null) ? 1L : v + 1L);
        }

        int size() {
            return freqMap.size();
        }

        long sum() {
            return freqMap.values().stream().mapToLong(Long::longValue).sum();
        }

        Map<K, Long> drainToMap() {
            // ConcurrentHashMap doesn't have a function to atomically drain an AtomicLongMap.
            // Here we need to atomically set each key to zero as we transfer it to the new map else we
            // see missed evaluations
            final ConcurrentHashMap<K, Long> snapshotMap = new ConcurrentHashMap<>();
            freqMap.forEach((k, v) -> transferValueIntoMapAtomicallyAndUpdateTo(k, snapshotMap, 0));
            snapshotMap.forEach((k, v) -> freqMap.remove(k, 0L));

            if (log.isTraceEnabled()) {
                log.trace("snapshot got {} events",
                        snapshotMap.values().stream().mapToLong(Long::longValue).sum());
            }
            return snapshotMap;
        }

        private void transferValueIntoMapAtomicallyAndUpdateTo(K key, Map<K, Long> targetMap, long newValue) {
            freqMap.computeIfPresent(key,
                    (k, v) -> {
                        targetMap.put(k, v);
                        return newValue;
                    });
        }
    }

    public AnalyticsManager(
            final CfConfiguration config,
            final AnalyticsPublisherService analyticsPublisherService) {
        this.frequencyMap = new FrequencyMap<>();
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.analyticsPublisherService = analyticsPublisherService;
        this.config = config;

        final long frequencyMs = config.getMetricsPublishingIntervalInMillis();
        scheduledExecutorService.scheduleAtFixedRate(() -> analyticsPublisherService.sendData(frequencyMap.drainToMap(), getSendingCallback()),  frequencyMs/2, frequencyMs, TimeUnit.MILLISECONDS);
        SdkCodes.infoMetricsThreadStarted((int)frequencyMs/1000);
    }

    public void registerEvaluation(
            final Target target,
            final String evaluationId,
            final Variation variation
    ) {
        final int freqMapSize = frequencyMap.size();

        if (freqMapSize > config.getMetricsCapacity()) {
            if (log.isWarnEnabled()) {
                log.warn(
                        "Metric frequency map exceeded buffer size ({} > {}), force flushing",
                        freqMapSize,
                        config.getMetricsCapacity());
            }
            // If the map is starting to grow too much then push the events now and reset the counters
            analyticsPublisherService.sendData(frequencyMap.drainToMap(), getSendingCallback());
        }

        final Analytics analytics = new AnalyticsBuilder()
                .target(target)
                .evaluationId(evaluationId)
                .variation(variation)
                .build();

        frequencyMap.increment(analytics);

        if (log.isTraceEnabled())
            log.trace("registerEvaluation: Variation={} NewMapSize={} NewTotalEvaluations={}", variation.getIdentifier(), frequencyMap.size(), frequencyMap.sum());
    }

    @Override
    public void close() {
        log.debug("destroying");

        analyticsPublisherService.sendData(frequencyMap.drainToMap(), getSendingCallback());
        scheduledExecutorService.shutdown();
        SdkCodes.infoMetricsThreadExited();
    }

    protected AnalyticsPublisherServiceCallback getSendingCallback() {
        return success -> {
            if (success) {
                log.debug("Metrics sending success");
            } else {
                log.debug("Metrics sending failure");
            }
        };
    }

    void flush() {
        analyticsPublisherService.sendData(frequencyMap.drainToMap(), getSendingCallback());
    }

    long getMetricsSent() {
        return analyticsPublisherService.getMetricsSent();
    }

    long getPendingMetricsToBeSent() {
        return frequencyMap.sum();
    }

    long getQueueSize() {
        return frequencyMap.size();
    }
}
