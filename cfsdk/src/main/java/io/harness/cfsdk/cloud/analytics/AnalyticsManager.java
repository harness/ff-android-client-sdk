package io.harness.cfsdk.cloud.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.common.SdkCodes;

public class AnalyticsManager implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsManager.class);

    private final AnalyticsPublisherService analyticsPublisherService;
    private final ScheduledExecutorService scheduledExecutorService;
    protected final BlockingQueue<Analytics> queue;

    public AnalyticsManager(
            final AuthInfo authInfo,
            final String authToken,
            final CfConfiguration config
    ) {
        queue = new LinkedBlockingQueue<>(config.getMetricsCapacity());
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        analyticsPublisherService = new AnalyticsPublisherService(authToken, config, authInfo);

        final long frequencyMs = config.getMetricsPublishingIntervalInMillis();

        scheduledExecutorService.scheduleAtFixedRate(() -> analyticsPublisherService.sendDataAndResetQueue(queue, getSendingCallback()),  frequencyMs/2, frequencyMs, TimeUnit.MILLISECONDS);
        SdkCodes.infoMetricsThreadStarted((int)frequencyMs/1000);
    }

    public boolean pushToQueue(
            final Target target,
            final String evaluationId,
            final Variation variation
    ) {

        if (queue.remainingCapacity() == 0) {
            analyticsPublisherService.sendDataAndResetQueue(queue, getSendingCallback());
        }

        log.debug("pushToQueue: Variation={}", variation);

        final Analytics analytics = new AnalyticsBuilder()
                .target(target)
                .evaluationId(evaluationId)
                .variation(variation)
                .build();

        try {
            queue.put(analytics);
            return queue.contains(analytics);

        } catch (final InterruptedException e) {
            log.warn("metrics queue error", e);
        }

        return false;
    }

    @Override
    public void close() {
        log.debug("destroying");

        analyticsPublisherService.sendDataAndResetQueue(queue, getSendingCallback());
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
}
