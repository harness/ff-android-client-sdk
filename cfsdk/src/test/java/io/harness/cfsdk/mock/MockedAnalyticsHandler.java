package io.harness.cfsdk.mock;

import java.util.HashSet;
import java.util.Set;

import io.harness.cfsdk.cloud.analytics.AnalyticsEventHandler;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherService;
import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.model.Analytics;

public class MockedAnalyticsHandler extends AnalyticsEventHandler {

    private Set<MockedAnalyticsHandlerCallback> callbacks;

    {

        callbacks = new HashSet<>();
    }

    public MockedAnalyticsHandler(

            Cache analyticsCache,
            AnalyticsPublisherService analyticsPublisherService
    ) {

        super(analyticsCache, analyticsPublisherService);
    }

    public void addCallback(MockedAnalyticsHandlerCallback callback) {

        callbacks.add(callback);
    }

    public void removeCallback(MockedAnalyticsHandlerCallback callback) {

        callbacks.remove(callback);
    }

    @Override
    protected void onMetricsEvent(Analytics analytics) {

        super.onMetricsEvent(analytics);
        notifyMetrics();
    }

    @Override
    protected void onTimerEvent() {

        super.onTimerEvent();
        notifyTimer();
    }

    private void notifyMetrics() {

        for (MockedAnalyticsHandlerCallback callback : callbacks) {

            callback.onMetrics();
        }
    }

    private void notifyTimer() {

        for (MockedAnalyticsHandlerCallback callback : callbacks) {

            callback.onTimer();
        }
    }
}
