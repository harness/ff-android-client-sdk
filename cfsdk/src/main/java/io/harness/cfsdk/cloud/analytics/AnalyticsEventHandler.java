package io.harness.cfsdk.cloud.analytics;

import com.lmax.disruptor.EventHandler;

import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.model.EventType;
import io.harness.cfsdk.logging.CfLog;

public class AnalyticsEventHandler implements EventHandler<Analytics> {

    private final String logTag;
    private final Cache analyticsCache;
    private final AnalyticsPublisherService analyticsPublisherService;

    {

        logTag = AnalyticsEventHandler.class.getSimpleName();
    }

    public AnalyticsEventHandler(

            Cache analyticsCache,
            AnalyticsPublisherService analyticsPublisherService
    ) {

        this.analyticsCache = analyticsCache;
        this.analyticsPublisherService = analyticsPublisherService;
    }

    @Override
    public void onEvent(Analytics analytics, long l, boolean b) {

        final EventType type = analytics.getEventType();
        CfLog.OUT.v(logTag, "onEvent: " + type.name());

        switch (type) {
            case TIMER:

                onTimerEvent();
                break;
            case METRICS:

                onMetricsEvent(analytics);
                break;
        }
    }

    protected void onTimerEvent() {

        analyticsPublisherService.sendDataAndResetCache();
    }

    protected void onMetricsEvent(Analytics analytics) {

        Integer count = analyticsCache.get(analytics);
        if (count == null) {

            analyticsCache.put(analytics, 1);
        } else {

            analyticsCache.put(analytics, count + 1);
        }

        CfLog.OUT.d(

                logTag,
                String.format(

                        "Analytics object received in queue: Target='%s', Variation name='%s', Count=%s",
                        analytics.getTarget().getIdentifier(),
                        analytics.getVariation().getName(),
                        analyticsCache.get(analytics)
                )
        );
    }

    // Uncomment the below line to print the cache for debugging purpose
    // AnalyticsManager.analyticsCache.printCache();
}
