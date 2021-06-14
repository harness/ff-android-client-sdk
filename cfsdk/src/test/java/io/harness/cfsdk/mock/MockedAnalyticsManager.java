package io.harness.cfsdk.mock;

import org.jetbrains.annotations.NotNull;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsEventHandler;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherService;

public class MockedAnalyticsManager extends AnalyticsManager {

    private MockedAnalyticsHandler analyticsEventHandler;

    public MockedAnalyticsManager(

            String environmentID,
            String authToken,
            CfConfiguration config
    ) {

        super(environmentID, "", authToken, config);
    }

    @NotNull
    @Override
    protected AnalyticsEventHandler getAnalyticsEventHandler(

            AnalyticsPublisherService analyticsPublisherService
    ) {

        if (analyticsEventHandler == null) {

            analyticsEventHandler = new MockedAnalyticsHandler(

                    analyticsCache,
                    analyticsPublisherService
            );
        }
        return analyticsEventHandler;
    }

    public void addCallback(MockedAnalyticsHandlerCallback callback) throws IllegalStateException {

        if (analyticsEventHandler == null) {

            throw new IllegalStateException("Analytics event handler not yet instantiated");
        }
        analyticsEventHandler.addCallback(callback);
    }

    public void removeCallback(MockedAnalyticsHandlerCallback callback) throws IllegalStateException {

        if (analyticsEventHandler == null) {

            throw new IllegalStateException("Analytics event handler not yet instantiated");
        }
        analyticsEventHandler.removeCallback(callback);
    }
}
