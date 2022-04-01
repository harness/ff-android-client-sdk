package io.harness.cfsdk.mock;

import java.util.concurrent.BlockingQueue;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.model.Analytics;

public class MockedAnalyticsManager extends AnalyticsManager {

    public MockedAnalyticsManager(

            String environmentID,
            String authToken,
            CfConfiguration config
    ) {

        super(environmentID, "", authToken, config);
    }

    public BlockingQueue<Analytics> getQueue() {

        return queue;
    }
}
