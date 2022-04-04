package io.harness.cfsdk.cloud.analytics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockMetricsApiFactoryRecipe;
import io.harness.cfsdk.mock.MockedAnalyticsManager;
import io.harness.cfsdk.mock.MockedCfConfiguration;

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
        final BlockingQueue<Analytics> queue = wrapper.queue;
        final MockedAnalyticsManager manager = wrapper.manager;

        populate(target, queue, manager);

        final MetricsApiFactoryRecipe successFactory =
                new MockMetricsApiFactoryRecipe(sendingLatch, true);

        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(successFactory);

        try {

            Assert.assertTrue(sendingLatch.await(1, TimeUnit.SECONDS));
            Assert.assertTrue(successLatch.await(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(1, manager.getSuccessCount());
        Assert.assertEquals(0, manager.getFailureCount());

        waitFor();
        Assert.assertTrue(queue.isEmpty());

        manager.destroy();

        Assert.assertTrue(manager.getSuccessCount() > 1);
        Assert.assertEquals(0, manager.getFailureCount());

        Assert.assertTrue(queue.isEmpty());
    }

    @Test
    public void testFaultyPath() {

        CountDownLatch sendingLatch = new CountDownLatch(1);
        final CountDownLatch successLatch = new CountDownLatch(1);

        final ManagerWrapper wrapper = getWrapped(successLatch);
        final Target target = wrapper.target;
        final BlockingQueue<Analytics> queue = wrapper.queue;
        final MockedAnalyticsManager manager = wrapper.manager;

        populate(target, queue, manager);

        final MetricsApiFactoryRecipe factory = new MockMetricsApiFactoryRecipe(sendingLatch,false);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(factory);

        try {

            Assert.assertTrue(sendingLatch.await(1, TimeUnit.SECONDS));
            Assert.assertTrue(successLatch.await(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(count * count, queue.size());

        sendingLatch = new CountDownLatch(1);
        MockMetricsApiFactoryRecipe successFactory = new MockMetricsApiFactoryRecipe(sendingLatch,true);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(successFactory);

        manager.destroy();

        try {

            Assert.assertTrue(sendingLatch.await(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        Assert.assertEquals(1, manager.getFailureCount());
        Assert.assertTrue(manager.getSuccessCount() >= 1);

        waitFor();
        Assert.assertTrue(queue.isEmpty());
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

        final BlockingQueue<Analytics> queue = manager.getQueue();

        Assert.assertEquals(

                metricsCapacity,
                queue.remainingCapacity()
        );

        return new ManagerWrapper(manager, queue, target);
    }

    private void populate(

            final Target target,
            final BlockingQueue<Analytics> queue,
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

                manager.pushToQueue(target, flag, variation);
            }
        }

        Assert.assertEquals(count * count, queue.size());
    }

    private String getFlag(int iteration) {

        return "Test_Flag_" + iteration;
    }

    private static class ManagerWrapper {

        MockedAnalyticsManager manager;
        BlockingQueue<Analytics> queue;
        Target target;

        public ManagerWrapper(

                final MockedAnalyticsManager manager,
                final BlockingQueue<Analytics> queue,
                final Target target
        ) {

            this.manager = manager;
            this.queue = queue;
            this.target = target;
        }
    }

    private void waitFor() {

        try {

            Thread.sleep(100);

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }
    }
}
