package io.harness.cfsdk.cloud.analytics;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

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
    private final int sendingCount = 5;
    private final int publishingIntervalInMillis = 100;

    {

        logTag = AnalyticsManagerTest.class.getSimpleName();
    }

    @Before
    public void prepare() {

        CfLog.testModeOn();
    }

    @Test
    public void testHappyPath() {

        final ManagerWrapper wrapper = getWrapped();
        final Target target = wrapper.target;
        final BlockingQueue<Analytics> queue = wrapper.queue;
        final MockedAnalyticsManager manager = wrapper.manager;

        populate(target, queue, manager);

        final MetricsApiFactoryRecipe successFactory = new MockMetricsApiFactoryRecipe(true);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(successFactory);

        manager.resetCounters();

        waitForPublishing();

        Assert.assertTrue(queue.isEmpty());
        Assert.assertTrue(manager.getSuccessCount() >= sendingCount + 1);

        manager.destroy();

        Assert.assertTrue(queue.isEmpty());
        Assert.assertTrue(manager.getSuccessCount() >= sendingCount + 2);
    }

    @Test
    public void testFaultyPath() {

        final MetricsApiFactoryRecipe factory = new MockMetricsApiFactoryRecipe(false);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(factory);

        final ManagerWrapper wrapper = getWrapped();
        final Target target = wrapper.target;
        final BlockingQueue<Analytics> queue = wrapper.queue;
        final MockedAnalyticsManager manager = wrapper.manager;

        populate(target, queue, manager);

        manager.resetCounters();

        Assert.assertEquals(count * count, queue.size());

        waitForPublishing();

        final int failed = manager.getFailureCount();

        Assert.assertEquals(count * count, queue.size());
        Assert.assertTrue(failed >= sendingCount);

        MockMetricsApiFactoryRecipe successFactory = new MockMetricsApiFactoryRecipe(true);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(successFactory);

        manager.destroy();

        Assert.assertTrue(queue.isEmpty());
        Assert.assertTrue(manager.getSuccessCount() >= 1);
        Assert.assertEquals(failed, manager.getFailureCount());
    }

    private ManagerWrapper getWrapped() {

        CfLog.OUT.v(logTag, "Testing: " + AnalyticsManager.class.getSimpleName());

        final String test = "Test";
        final String token = UUID.randomUUID().toString();

        final Target target = new Target();
        target.identifier(test);
        target.name(test);

        int metricsCapacity = 100;
        int publishingAcceptableDurationInMillis = 500;

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

        final MockedAnalyticsManager manager = new MockedAnalyticsManager(test, token, configuration);
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

        final MetricsApiFactoryRecipe factory = new MockMetricsApiFactoryRecipe(false);
        MetricsApiFactory.setDefaultMetricsApiFactoryRecipe(factory);

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

    private void waitForPublishing() {

        try {

            Thread.sleep((sendingCount + 1) * publishingIntervalInMillis);

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }
    }

    private void waitForPopulating() {

        try {

            Thread.sleep(publishingIntervalInMillis);

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }
    }
}
