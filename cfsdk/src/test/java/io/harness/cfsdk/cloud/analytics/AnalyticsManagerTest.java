package io.harness.cfsdk.cloud.analytics;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.core.model.Evaluation;
import io.harness.cfsdk.cloud.core.model.Variation;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.mock.MockedAnalyticsManager;
import io.harness.cfsdk.mock.MockedCfConfiguration;

public class AnalyticsManagerTest {

    private final String logTag;

    {

        logTag = AnalyticsManagerTest.class.getSimpleName();
    }

    @Test
    public void testAnalyticsManager() {

        CfLog.testModeOn();

        CfLog.OUT.v(logTag, "Testing: " + AnalyticsManager.class.getSimpleName());

        final int count = 10;
        final String test = "Test";
        final String token = UUID.randomUUID().toString();

        final Target target = new Target();
        target.identifier(test);
        target.name(test);

        final int metricsCapacity = 100;
        final int publishingIntervalInMillis = 500;
        final int publishingAcceptableDurationInMillis = 500;

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

        MockedAnalyticsManager manager = new MockedAnalyticsManager(test, token, configuration);

        Assert.assertEquals(

                metricsCapacity,
                manager.getQueue().remainingCapacity()
        );

        for (int x = 0; x < count; x++) {

            final String flag = getFlag(x);
            final boolean value = x % 2 == 0;
            final Evaluation result = new Evaluation().value(value).flag(flag);

            final Variation variation = new Variation();
            variation.setName(flag);
            variation.setValue(String.valueOf(result));
            variation.setIdentifier(result.getIdentifier());

            manager.pushToQueue(target, flag, variation);
        }

        // TODO: Assert queue

        manager.destroy();
    }

    private String getFlag(int iteration) {

        return "Test_Flag_" + iteration;
    }
}
