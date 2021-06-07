package io.harness.cfsdk.mock;

import io.harness.cfsdk.CfConfiguration;

public class MockedCfConfiguration extends CfConfiguration {

    public static final int MOCKED_MIN_FREQUENCY;

    static {

        MOCKED_MIN_FREQUENCY = 2;
    }

    public MockedCfConfiguration(

            String baseURL,
            String streamURL,
            boolean streamEnabled,
            boolean analyticsEnabled,
            int pollingInterval
    ) {

        super(baseURL, streamURL, streamEnabled, analyticsEnabled, pollingInterval);
    }

    @Override
    public int getFrequency() {

        return MOCKED_MIN_FREQUENCY;
    }
}
