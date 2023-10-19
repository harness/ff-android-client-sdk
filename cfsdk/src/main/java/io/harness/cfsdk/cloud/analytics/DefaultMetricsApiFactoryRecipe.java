package io.harness.cfsdk.cloud.analytics;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.openapi.metric.ApiClient;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.openapi.metric.api.MetricsApi;
import io.harness.cfsdk.utils.CfUtils;
import io.harness.cfsdk.utils.TlsUtils;

public class DefaultMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private static final Logger log = LoggerFactory.getLogger(DefaultMetricsApiFactoryRecipe.class);

    @Override
    public MetricsApi create(String authToken, CfConfiguration config, AuthInfo authInfo) {

        final MetricsApi metricsAPI = new MetricsApi();

        if (!CfUtils.Text.isEmpty(config.getEventURL())) {

            ApiClient apiClient = metricsAPI.getApiClient();
            apiClient.setBasePath(config.getEventURL());
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            apiClient.setUserAgent("android " + ANDROID_SDK_VERSION);
            TlsUtils.setupTls(apiClient, config);
            String hostname = "UnknownHost";

            try {
                hostname = InetAddress.getLocalHost().getHostName();
                log.debug("Hostname: {}", hostname);

            } catch (UnknownHostException e) {

                log.warn("Unable to get hostname", e);
            }

            apiClient.addDefaultHeader("Hostname", hostname);
            apiClient.addDefaultHeader("Harness-SDK-Info", "Android " + ANDROID_SDK_VERSION + " Client");
            apiClient.addDefaultHeader("Harness-EnvironmentID", authInfo.getEnvironmentTrackingHeader());
            // Relay Proxy does not include the accountID
            if (authInfo.getAccountID() != null) {
                apiClient.addDefaultHeader("Harness-AccountID", authInfo.getAccountID());
            }
            metricsAPI.setApiClient(apiClient);
        }

        return metricsAPI;
    }
}
