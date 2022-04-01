package io.harness.cfsdk.mock;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.MetricsApiFactoryRecipe;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.logging.CfLog;

public class MockMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private final String logTag;
    private final boolean resultSuccess;

    public MockMetricsApiFactoryRecipe(final boolean resultSuccess) {

        this.resultSuccess = resultSuccess;
        logTag = MockMetricsApiFactoryRecipe.class.getSimpleName() + " :: hash=" + hashCode();
    }

    @Override
    public MetricsApi create(String authToken, CfConfiguration config) {

        return new MetricsApi() {

            @Override
            public void postMetrics(

                    final String environment,
                    final String cluster,
                    final Metrics metrics

            ) throws ApiException {

                CfLog.OUT.v(logTag, "Post metrics mocked success: " + resultSuccess);

                if (!resultSuccess) {

                    throw new ApiException("Mocked metrics API failure");
                }
            }
        };
    }
}
