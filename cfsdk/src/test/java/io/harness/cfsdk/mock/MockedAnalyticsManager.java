package io.harness.cfsdk.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.AnalyticsManager;
import io.harness.cfsdk.cloud.analytics.AnalyticsPublisherServiceCallback;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.model.AuthInfo;

public class MockedAnalyticsManager extends AnalyticsManager {

    private static final Logger log = LoggerFactory.getLogger(MockedAnalyticsManager.class);
    private final CountDownLatch latch;
    private int successCountValue;
    private int failureCountValue;

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

            log.debug("Sending result: {}", success);

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
