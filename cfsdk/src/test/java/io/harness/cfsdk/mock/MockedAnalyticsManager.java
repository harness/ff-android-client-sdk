package io.harness.cfsdk.mock;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherServiceCallback;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.logging.CfLog;

public class MockedAnalyticsManager extends AnalyticsManager {

    private final CountDownLatch latch;
    private int successCountValue;
    private int failureCountValue;

    private final String logTag = MockedAnalyticsManager.class.getSimpleName();
    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger failureCount = new AtomicInteger();

    public MockedAnalyticsManager(

            final AuthInfo authInfo,
            final String authToken,
            final CfConfiguration config,
            final CountDownLatch latch
    ) {

        super(authInfo, authToken, config);
        this.latch = latch;
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
