package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;

public interface MetricsApiFactoryRecipe {

    MetricsApi create(

            final String authToken,
            final CfConfiguration config
    );
}
