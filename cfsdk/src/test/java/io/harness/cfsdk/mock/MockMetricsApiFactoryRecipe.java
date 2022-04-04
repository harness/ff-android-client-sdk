package io.harness.cfsdk.mock;

import java.util.concurrent.CountDownLatch;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.MetricsApiFactoryRecipe;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.logging.CfLog;

public class MockMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private final String logTag;
    private final CountDownLatch latch;
    private final boolean resultSuccess;

    public MockMetricsApiFactoryRecipe(

            final CountDownLatch latch,
            final boolean resultSuccess
    ) {

        this.latch = latch;
        this.resultSuccess = resultSuccess;
        logTag = MockMetricsApiFactoryRecipe.class.getSimpleName() + " :: hash=" + hashCode();
    }

    @Override
    public MetricsApi create(String authToken, CfConfiguration config) {

        return (environment, cluster, metrics) -> {

            latch.countDown();

            CfLog.OUT.v(logTag, "Post metrics mocked success: " + resultSuccess);

            if (!resultSuccess) {

                throw new ApiException("Mocked metrics API failure");
            }
        };
    }
}
