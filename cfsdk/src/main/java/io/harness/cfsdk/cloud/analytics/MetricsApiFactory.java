package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.utils.CfUtils;

/**
 * This is a factory class to provide the API for metrics related operations.
 *
 * @author Subir.Adhikari
 * @version 1.0
 * @since 08/01/2021
 */
public class MetricsApiFactory {

    public static DefaultApi create(

            final String authToken,
            final CfConfiguration config
    ) {

        final DefaultApi metricsAPI = new DefaultApi();

        if (!CfUtils.Text.isEmpty(config.getEventURL())) {

            ApiClient apiClient = metricsAPI.getApiClient();
            apiClient.setBasePath(config.getEventURL());
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            metricsAPI.setApiClient(apiClient);
        }
        return metricsAPI;
    }
}
