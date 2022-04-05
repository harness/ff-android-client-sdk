package io.harness.cfsdk.cloud.analytics.api;

import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.core.client.ApiException;

public interface MetricsApi {

    /**
     * Send metrics to the Analytics server.
     * Send metrics to Analytics server
     *
     * @param environment environment parameter in query. (required)
     * @param cluster     Cluster identifier.
     * @param metrics     (optional)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    void postMetrics(

            String environment,
            String cluster,
            Metrics metrics

    ) throws ApiException;
}
