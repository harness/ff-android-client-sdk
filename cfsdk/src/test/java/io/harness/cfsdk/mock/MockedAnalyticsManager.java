package io.harness.cfsdk.mock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherServiceCallback;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.logging.CfLog;

public class MockedAnalyticsManager extends AnalyticsManager {

    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;

    {

        successCount = new AtomicInteger();
        failureCount = new AtomicInteger();
    }

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

    @Override
    protected AnalyticsPublisherServiceCallback getSendingCallback() {

        return success -> {

            if (success) {

                successCount.incrementAndGet();

            } else {

                failureCount.incrementAndGet();
            }
        };
    }

    public void resetCounters() {

        successCount.set(0);
        failureCount.set(0);
    }

    public int getSuccessCount() {

        return successCount.get();
    }

    public int getFailureCount() {

        return failureCount.get();
    }
}
