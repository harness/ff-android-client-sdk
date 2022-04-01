package io.harness.cfsdk.mock;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.MetricsApiFactoryRecipe;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.core.client.ApiException;

public class MockMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private final boolean resultSuccess;

    public MockMetricsApiFactoryRecipe(final boolean resultSuccess) {

        this.resultSuccess = resultSuccess;
    }

    @Override
    public MetricsApi create(String authToken, CfConfiguration config) {

        return (environment, cluster, metrics) -> {

            if (!resultSuccess) {

                throw new ApiException("Mocked metrics API failure");
            }
        };
    }
}
