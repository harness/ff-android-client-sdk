package io.harness.cfsdk.mock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherServiceCallback;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.logging.CfLog;

public class MockedAnalyticsManager extends AnalyticsManager {

    private CountDownLatch latch;
    private int successCountValue;
    private int failureCountValue;

    private final String logTag;
    private final AtomicInteger successCount;
    private final AtomicInteger failureCount;

    {

        successCount = new AtomicInteger();
        failureCount = new AtomicInteger();
        logTag = MockedAnalyticsManager.class.getSimpleName();
    }

    public MockedAnalyticsManager(

            final String environmentID,
            final String authToken,
            final CfConfiguration config,
            final CountDownLatch latch
    ) {

        super(environmentID, "", authToken, config);
        this.latch = latch;
    }

    public MockedAnalyticsManager(

            final String environmentID,
            final String authToken,
            final CfConfiguration config
    ) {

        super(environmentID, "", authToken, config);
    }

    public BlockingQueue<Analytics> getQueue() {

        return queue;
    }

    @Override
    protected AnalyticsPublisherServiceCallback getSendingCallback() {

        return success -> {

            CfLog.OUT.v(logTag, "Sending result: " + success);

            if (success) {

                successCountValue = successCount.incrementAndGet();

            } else {

                failureCountValue = failureCount.incrementAndGet();
            }

            if (latch != null) {

                latch.countDown();
            }
        };
    }

    public int getSuccessCount() {

        return successCountValue;
    }

    public int getFailureCount() {

        return failureCountValue;
    }
}
