package io.harness.cfsdk.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.MetricsApiFactoryRecipe;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.model.AuthInfo;

public class MockMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private static final Logger log = LoggerFactory.getLogger(MockMetricsApiFactoryRecipe.class);

    private final CountDownLatch latch;
    private final boolean resultSuccess;

    public MockMetricsApiFactoryRecipe(

            final CountDownLatch latch,
            final boolean resultSuccess
    ) {

        this.latch = latch;
        this.resultSuccess = resultSuccess;
    }

    @Override
    public MetricsApi create(String authToken, CfConfiguration config, AuthInfo authInfo) {

        return (environment, cluster, metrics) -> {

            latch.countDown();

            log.debug("Post metrics mocked success: {}", resultSuccess);

            if (!resultSuccess) {

                throw new ApiException("Mocked metrics API failure");
            }
        };
    }
}
