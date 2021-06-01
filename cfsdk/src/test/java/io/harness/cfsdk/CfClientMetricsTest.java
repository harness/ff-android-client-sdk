package io.harness.cfsdk;

import android.content.Context;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockedAnalyticsHandlerCallback;
import io.harness.cfsdk.mock.MockedCfClient;
import io.harness.cfsdk.mock.MockedCfConfiguration;
import io.harness.cfsdk.mock.MockedCloudFactory;
import io.harness.cfsdk.mock.MockedFeatureRepository;

public class CfClientMetricsTest {

    @Mock
    Context context;

    private final String logTag;
    private final CloudFactory cloudFactory;

    {

        cloudFactory = new MockedCloudFactory();
        logTag = CfClientInitTest.class.getSimpleName();
    }

    @Test
    public void testMetrics() {

        CfLog.testModeOn();

        final String mock = "mock";
        final MockedCfClient cfClient = new MockedCfClient(cloudFactory);
        final String apiKey = String.valueOf(System.currentTimeMillis());

        final MockedCfConfiguration cfConfiguration = new MockedCfConfiguration(

                mock,
                mock,
                true,
                true,
                10
        );

        final AtomicBoolean initOk = new AtomicBoolean();
        final CountDownLatch latch = new CountDownLatch(1);

        cfClient.initialize(

                context,
                apiKey,
                cfConfiguration,
                new Target().identifier("target"),

                (info, result) -> {

                    Assert.assertNotNull(info);
                    Assert.assertNotNull(result);
                    Assert.assertTrue(result.isSuccess());
                    initOk.set(result.isSuccess());
                    latch.countDown();
                }
        );

        try {

            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }

        Assert.assertTrue(initOk.get());

        final AtomicInteger timerEventsCount = new AtomicInteger();
        final AtomicInteger metricsEventsCount = new AtomicInteger();

        final MockedAnalyticsHandlerCallback metricsCallback = new MockedAnalyticsHandlerCallback() {

            @Override
            public void onTimer() {

                int count = timerEventsCount.incrementAndGet();
                CfLog.OUT.v(logTag, "Timer events count: " + count);
            }

            @Override
            public void onMetrics() {

                int count = metricsEventsCount.incrementAndGet();
                CfLog.OUT.v(logTag, "Metric events count: " + count);
            }
        };

        try {

            cfClient.addCallback(metricsCallback);
        } catch (IllegalStateException e) {

            Assert.fail(e.getMessage());
        }

        final int evaluationsCount = 10;

        for (int x = 0; x < evaluationsCount; x++) {

            cfClient.boolVariation(MockedFeatureRepository.MOCK_BOOL, false);
        }

        while (metricsEventsCount.get() < evaluationsCount) {

            Thread.yield();
        }

        Assert.assertEquals(evaluationsCount, metricsEventsCount.get());

        try {

            Thread.sleep((MockedCfConfiguration.MOCKED_MIN_FREQUENCY + 1) * 1000);
        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        Assert.assertTrue(timerEventsCount.get() > 0);

        try {

            cfClient.removeCallback(metricsCallback);
        } catch (IllegalStateException e) {

            Assert.fail(e.getMessage());
        }

        cfClient.destroy();
    }
}
