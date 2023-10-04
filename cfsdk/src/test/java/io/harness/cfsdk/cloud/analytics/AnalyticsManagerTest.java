package io.harness.cfsdk.cloud.analytics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.mock.MockMetricsApiFactoryRecipe;
import io.harness.cfsdk.mock.MockedAnalyticsManager;


@SuppressWarnings("BusyWait")
public class AnalyticsManagerTest {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsManagerTest.class);

    private final long timeout = 30_000L;
    private final int count = 3;

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

            Assert.assertTrue(sendingLatch.await(timeout, TimeUnit.MILLISECONDS));
            Assert.assertTrue(successLatch.await(timeout, TimeUnit.MILLISECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        long start = System.currentTimeMillis();
        while (!manager.queue.isEmpty()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= timeout) {

                    Assert.fail("Timeout after " + timeout);
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertTrue(manager.getQueue().isEmpty());

        start = System.currentTimeMillis();
        while (manager.getSuccessCount() == 0 && manager.getFailureCount() == 0) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= timeout) {

                    Assert.fail("Timeout after " + timeout);
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertTrue(manager.getSuccessCount() >= 1);
        Assert.assertEquals(0, manager.getFailureCount());

        manager.destroy();

        start = System.currentTimeMillis();
        while (!manager.queue.isEmpty()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= timeout) {

                    Assert.fail("Timeout after 3 seconds");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
        }

        Assert.assertTrue(manager.getQueue().isEmpty());
        Assert.assertTrue(manager.getSuccessCount() > 1);
        Assert.assertEquals(0, manager.getFailureCount());
    }

    @Ignore("Tracked by FFM-8570")
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

            Assert.assertTrue(sendingLatch.await(timeout, TimeUnit.MILLISECONDS));
            Assert.assertTrue(successLatch.await(timeout, TimeUnit.MILLISECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        long start = System.currentTimeMillis();
        while (count * count != manager.getQueue().size()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= timeout) {

                    Assert.fail("Timeout after 30 seconds");
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

            Assert.assertTrue(sendingLatch.await(timeout, TimeUnit.MILLISECONDS));

        } catch (InterruptedException e) {

            Assert.fail(e.getMessage());
        }

        start = System.currentTimeMillis();
        while (!manager.queue.isEmpty()) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= timeout) {

                    Assert.fail("Timeout after 3 seconds");
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

        log.debug("Testing: {}", AnalyticsManager.class.getSimpleName());

        final String test = "Test";
        final String token = UUID.randomUUID().toString();

        final Target target = new Target();
        target.identifier(test);
        target.name(test);

        int metricsCapacity = 100;
        long publishingAcceptableDurationInMillis = 500;

        long publishingIntervalInMillis = 100;

        final CfConfiguration configuration = mock(CfConfiguration.class);
        when(configuration.isAnalyticsEnabled()).thenReturn(true);
        when(configuration.getStreamEnabled()).thenReturn(false);
        when(configuration.getMetricsPublishingIntervalInMillis()).thenReturn(publishingIntervalInMillis);
        when(configuration.getMetricsCapacity()).thenReturn(metricsCapacity);
        when(configuration.getMetricsServiceAcceptableDurationInMillis()).thenReturn(publishingAcceptableDurationInMillis);

        final MockedAnalyticsManager manager =
                new MockedAnalyticsManager(Mockito.mock(AuthInfo.class), token, configuration, latch);

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

                (authInfo, authToken, config) -> (environment, cluster, metrics) ->
                       log.debug("Ignore this metrics posting")
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

        long start = System.currentTimeMillis();
        while (manager.queue.size() != count * count) {

            try {

                Thread.sleep(50);

                if (System.currentTimeMillis() - start >= timeout) {

                    Assert.fail("Timeout after 3 seconds");
                }

            } catch (InterruptedException e) {

                Assert.fail(e.getMessage());
            }
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
