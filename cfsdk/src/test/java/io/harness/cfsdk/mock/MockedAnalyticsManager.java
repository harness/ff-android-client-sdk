package io.harness.cfsdk.mock;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;

public class MockedAnalyticsManager extends AnalyticsManager {

    public MockedAnalyticsManager(

            String environmentID,
            String authToken,
            CfConfiguration config
    ) {

        super(environmentID, "", authToken, config);
    }
}
