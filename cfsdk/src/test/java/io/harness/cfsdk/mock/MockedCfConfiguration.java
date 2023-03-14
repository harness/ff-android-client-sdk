package io.harness.cfsdk.mock;

import io.harness.cfsdk.CfConfiguration;

public class MockedCfConfiguration  extends CfConfiguration {

    protected MockedCfConfiguration(

            String baseURL,
            String streamURL,
            String eventURL,
            boolean streamEnabled,
            boolean analyticsEnabled,
            int pollingInterval
    ) {

        super(baseURL, streamURL, eventURL, streamEnabled, analyticsEnabled, pollingInterval, null);
    }

    public MockedCfConfiguration(final CfConfiguration.Builder builder) {

        this(

                builder.getBaseURL(),
                builder.getStreamURL(),
                builder.getEventURL(),
                builder.isStreamEnabled(),
                builder.isAnalyticsEnabled(),
                builder.getPollingInterval()
        );

        setMetricsCapacity(builder.getMetricsCapacity());
        setMetricsPublishingIntervalInMillis(builder.getMetricsPublishingIntervalInMillis());
        setMetricsServiceAcceptableDurationInMillis(builder.getMetricsPublishingAcceptableDurationInMillis());
    }

    @Override
    public long getMetricsPublishingIntervalInMillis() {

        return getMetricsServiceAcceptableDurationInMillis();
    }

    public static class Builder extends CfConfiguration.Builder {

        @Override
        public MockedCfConfiguration build() {

            return new MockedCfConfiguration(this);
        }
    }
}
