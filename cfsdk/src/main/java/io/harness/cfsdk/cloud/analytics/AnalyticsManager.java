package io.harness.cfsdk.cloud.analytics;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.common.Destroyable;
import io.harness.cfsdk.logging.CfLog;

public class AnalyticsManager implements Destroyable {

    protected final BlockingQueue<Analytics> queue;

    private final Timer timer;
    private final String logTag;
    private final AnalyticsPublisherService analyticsPublisherService;

    {

        timer = new Timer();
        logTag = AnalyticsManager.class.getSimpleName();
    }

    public AnalyticsManager(

            final String environmentID,
            final String cluster,
            final String authToken,
            final CfConfiguration config
    ) {

        queue = new LinkedBlockingQueue<>(config.getMetricsCapacity());

        analyticsPublisherService = new AnalyticsPublisherService(

                authToken, config, environmentID, cluster
        );

        final long frequency = config.getMetricsPublishingIntervalInMillis();

        timer.schedule(

                new TimerTask() {

                    @Override
                    public void run() {

                        analyticsPublisherService.sendDataAndResetQueue(queue, getSendingCallback());
                    }
                },

                0L,
                frequency
        );

        final String msg = String.format(

                "Metrics sending scheduled with frequency of: %s", frequency
        );

        CfLog.OUT.v(logTag, msg);
    }

    public void pushToQueue(

            final Target target,
            final String evaluationId,
            final Variation variation
    ) {

        if (queue.remainingCapacity() == 0) {

            analyticsPublisherService.sendDataAndResetQueue(queue, getSendingCallback());
        }

        CfLog.OUT.v(logTag, "pushToQueue: Variation=" + variation);

        final Analytics analytics = new AnalyticsBuilder()
                .target(target)
                .evaluationId(evaluationId)
                .variation(variation)
                .build();

        try {

            queue.put(analytics);

        } catch (final InterruptedException e) {

            CfLog.OUT.e(logTag, "Error adding into the metrics queue", e);
        }
    }

    @Override
    public void destroy() {

        CfLog.OUT.v(logTag, "destroying");

        analyticsPublisherService.sendDataAndResetQueue(queue, getSendingCallback());
        timer.cancel();
        timer.purge();
    }

    protected AnalyticsPublisherServiceCallback getSendingCallback() {

        return success -> {

            if (success) {

                CfLog.OUT.v(logTag, "Metrics sending success");

            } else {

                CfLog.OUT.w(logTag, "Metrics sending failure");
            }
        };
    }
}
