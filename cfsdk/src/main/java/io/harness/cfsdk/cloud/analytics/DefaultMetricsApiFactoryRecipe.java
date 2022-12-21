package io.harness.cfsdk.cloud.analytics;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.utils.CfUtils;

public class DefaultMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private final String logTag;

    {

        logTag = DefaultMetricsApiFactoryRecipe.class.getSimpleName();
    }

    @Override
    public MetricsApi create(String authToken, CfConfiguration config) {

        final DefaultApi metricsAPI = new DefaultApi();

        if (!CfUtils.Text.isEmpty(config.getEventURL())) {

            ApiClient apiClient = metricsAPI.getApiClient();
            apiClient.setBasePath(config.getEventURL());
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            apiClient.setUserAgent("android 1.0.16");
            String hostname = "UnknownHost";

            try {

                hostname = InetAddress.getLocalHost().getHostName();
                CfLog.OUT.v(logTag, "Hostname: " + hostname);

            } catch (UnknownHostException e) {

                CfLog.OUT.w(logTag, "Unable to get hostname");
            }

            apiClient.addDefaultHeader("Hostname", hostname);
            metricsAPI.setApiClient(apiClient);
        }

        return metricsAPI;
    }
}
