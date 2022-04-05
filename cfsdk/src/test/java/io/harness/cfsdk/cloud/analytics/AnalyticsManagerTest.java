package io.harness.cfsdk.cloud.analytics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockMetricsApiFactoryRecipe;
import io.harness.cfsdk.mock.MockedAnalyticsManager;
import io.harness.cfsdk.mock.MockedCfConfiguration;

@SuppressWarnings("BusyWait")
public class AnalyticsManagerTest {

    private final String logTag;
    private final int count = 10;

    {

        logTag = AnalyticsManagerTest.class.getSimpleName();
    }

    @Before
    public void prepare() {

        CfLog.testModeOn();
    }

    @Test
    public void testHappyPath() {

        final CountDownLatch sendingLatch = new CountDownLatch(1);
        final CountDownLatch successLatch = new CountDownLatch(1);

        final ManagerWrapper wrapper = getWrapped(successLatch);
        final Target target = wrapper.target;
        final MockedAnalyticsManager manager = wrapper.manager;

        populate(target, manager);

        final MetricsApiFactoryRecipe successFactory =
                new MockMetricsApiFactoryRecipe(sendingLatch, true);

        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(successFactory);

        try {

            Assert.assertTrue(sendingLatch.await(1, TimeUnit.SECONDS));
            Assert.assertTrue(successLatch.await(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        long start = System.currentTimeMillis();
        while (manager.getSuccessCount() == 0 && manager.getFailureCount() == 0) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= 1000) {

                    Assert.fail("Timeout after 1 second");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertEquals(1, manager.getSuccessCount());
        Assert.assertEquals(0, manager.getFailureCount());

        start = System.currentTimeMillis();
        while (!manager.queue.isEmpty()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= 1000) {

                    Assert.fail("Timeout after 1 second");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertTrue(manager.getQueue().isEmpty());

        manager.destroy();

        Assert.assertTrue(manager.getSuccessCount() > 1);
        Assert.assertEquals(0, manager.getFailureCount());

        start = System.currentTimeMillis();
        while (!manager.queue.isEmpty()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= 1000) {

                    Assert.fail("Timeout after 1 second");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertTrue(manager.getQueue().isEmpty());
    }

    @Test
    public void testFaultyPath() {

        CountDownLatch sendingLatch = new CountDownLatch(1);
        final CountDownLatch successLatch = new CountDownLatch(1);

        final ManagerWrapper wrapper = getWrapped(successLatch);
        final Target target = wrapper.target;
        final MockedAnalyticsManager manager = wrapper.manager;

        populate(target, manager);

        final MetricsApiFactoryRecipe factory = new MockMetricsApiFactoryRecipe(sendingLatch, false);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(factory);

        try {

            Assert.assertTrue(sendingLatch.await(1, TimeUnit.SECONDS));
            Assert.assertTrue(successLatch.await(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        long start = System.currentTimeMillis();
        while (count * count != manager.getQueue().size()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= 1000) {

                    Assert.fail("Timeout after 1 second");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertEquals(count * count, manager.getQueue().size());

        sendingLatch = new CountDownLatch(1);
        MockMetricsApiFactoryRecipe successFactory = new MockMetricsApiFactoryRecipe(sendingLatch, true);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(successFactory);

        manager.destroy();

        try {

            Assert.assertTrue(sendingLatch.await(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        start = System.currentTimeMillis();
        while (!manager.queue.isEmpty()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= 1000) {

                    Assert.fail("Timeout after 1 second");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertTrue(manager.getQueue().isEmpty());

        Assert.assertEquals(1, manager.getFailureCount());
        Assert.assertTrue(manager.getSuccessCount() >= 1);
    }

    private ManagerWrapper getWrapped(final CountDownLatch latch) {

        CfLog.OUT.v(logTag, "Testing: " + AnalyticsManager.class.getSimpleName());

        final String test = "Test";
        final String token = UUID.randomUUID().toString();

        final Target target = new Target();
        target.identifier(test);
        target.name(test);

        int metricsCapacity = 100;
        int publishingAcceptableDurationInMillis = 500;

        int publishingIntervalInMillis = 100;
        final CfConfiguration.Builder builder = CfConfiguration.builder()
                .enableAnalytics(true)
                .enableStream(false)
                .metricsPublishingIntervalInMillis(publishingIntervalInMillis)
                .metricsPublishingAcceptableDurationInMillis(publishingAcceptableDurationInMillis)
                .metricsCapacity(metricsCapacity);

        final CfConfiguration defaults = builder.build();

        final MockedCfConfiguration configuration = new MockedCfConfiguration(builder);

        Assert.assertEquals(

                CfConfiguration.MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS * 1000L,
                defaults.getMetricsPublishingIntervalInMillis()
        );

        Assert.assertEquals(

                publishingAcceptableDurationInMillis,
                defaults.getMetricsServiceAcceptableDurationInMillis()
        );

        Assert.assertEquals(

                publishingIntervalInMillis,
                configuration.getMetricsPublishingIntervalInMillis()
        );

        Assert.assertEquals(

                publishingAcceptableDurationInMillis,
                configuration.getMetricsServiceAcceptableDurationInMillis()
        );

        Assert.assertEquals(

                metricsCapacity,
                configuration.getMetricsCapacity()
        );

        final MockedAnalyticsManager manager =
                new MockedAnalyticsManager(test, token, configuration, latch);

        Assert.assertEquals(

                metricsCapacity,
                manager.getQueue().remainingCapacity()
        );

        return new ManagerWrapper(manager, target);
    }

    private void populate(

            final Target target,
            final MockedAnalyticsManager manager
    ) {

        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(

                (authToken, config) -> (environment, cluster, metrics) ->
                        CfLog.OUT.v(logTag, "Ignore this metrics posting")
        );

        for (int x = 0; x < count; x++) {
            for (int y = 0; y < count; y++) {

                final String flag = getFlag(x);
                final boolean value = x % 2 == 0;
                final Evaluation result = new Evaluation().value(value).flag(flag);

                final Variation variation = new Variation();
                variation.setName(flag);
                variation.setValue(String.valueOf(result));
                variation.setIdentifier(result.getIdentifier());

                Assert.assertTrue(manager.pushToQueue(target, flag, variation));
            }
        }

        while (manager.queue.size() < count * count) {

            Thread.yield();
        }
        Assert.assertEquals(count * count, manager.getQueue().size());
    }

    private String getFlag(int iteration) {

        return "Test_Flag_" + iteration;
    }

    private static class ManagerWrapper {

        MockedAnalyticsManager manager;
        Target target;

        public ManagerWrapper(

                final MockedAnalyticsManager manager,
                final Target target
        ) {

            this.manager = manager;
            this.target = target;
        }
    }
}
