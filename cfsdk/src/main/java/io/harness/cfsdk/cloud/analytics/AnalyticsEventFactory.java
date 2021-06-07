package io.harness.cfsdk.cloud.analytics;

import com.lmax.disruptor.EventFactory;

import io.harness.cfsdk.cloud.analytics.model.Analytics;

/**
 * This class implements the EventFactory interface required by the LMAX library.
 */
public class AnalyticsEventFactory implements EventFactory<Analytics> {

    @Override
    public Analytics newInstance() {

        return new AnalyticsBuilder().build();
    }
}