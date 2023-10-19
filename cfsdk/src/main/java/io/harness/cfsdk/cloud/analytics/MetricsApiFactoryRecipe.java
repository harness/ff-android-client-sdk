package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.openapi.metric.api.MetricsApi;

public interface MetricsApiFactoryRecipe {

    MetricsApi create(

            final String authToken,
            final CfConfiguration config,
            final AuthInfo authInfo
    );
}
