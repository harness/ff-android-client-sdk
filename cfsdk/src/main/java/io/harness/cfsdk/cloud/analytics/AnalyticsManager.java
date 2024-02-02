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
import io.harness.cfsdk.cloud.openapi.client.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.common.SdkCodes;

public class AnalyticsManager implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsManager.class);

    private final AnalyticsPublisherService analyticsPublisherService;
    private final ScheduledExecutorService scheduledExecutorService;
    private final FrequencyMap<Analytics> frequencyMap;
    private final CfConfiguration config;
    private final Target target;

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
            final Target target,
            final AnalyticsPublisherService analyticsPublisherService) {
        this.frequencyMap = new FrequencyMap<>();
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.analyticsPublisherService = analyticsPublisherService;
        this.config = config;
        this.target = target;

        final long frequencyMs = config.getMetricsPublishingIntervalInMillis();
        scheduledExecutorService.scheduleAtFixedRate(this::postMetricsThread,frequencyMs/2, frequencyMs, TimeUnit.MILLISECONDS);
        SdkCodes.infoMetricsThreadStarted((int)frequencyMs/1000);
    }

    public void postMetricsThread() {
        log.debug("Running metrics thread iteration. frequencyMapSize={}", frequencyMap.size());
        Thread.currentThread().setName("Metrics Thread");

        try {
            long startTime = System.currentTimeMillis();
            int mapSizeBefore = frequencyMap.size();

            analyticsPublisherService.sendData(frequencyMap.drainToMap(), getSendingCallback());

            long timeTakenMs = (System.currentTimeMillis() - startTime);
            if (timeTakenMs > config.getMetricsServiceAcceptableDurationInMillis())
                log.warn("Metrics service API duration={}", timeTakenMs);

            log.debug("Metrics thread completed in {}ms, previousMapSize={} newMapSize={}", timeTakenMs, mapSizeBefore, frequencyMap.size());
        } catch (Throwable ex) {
            log.warn("Exception in metrics thread", ex);
        }
    }

    public void registerEvaluation(
            final String evaluationId,
            final Variation variation
    ) {
        final int freqMapSize = frequencyMap.size();

        if (freqMapSize > config.getMetricsCapacity()) {
            if (log.isWarnEnabled()) {
                log.warn("Metric frequency map exceeded buffer size ({} > {}), force flushing",
                        freqMapSize,
                        config.getMetricsCapacity());
            }
            // If the map is starting to grow too much then push the events now and reset the counters
            scheduledExecutorService.schedule(this::postMetricsThread, 0, TimeUnit.SECONDS);
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
        return success -> log.trace("callback result success={}", success);
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
