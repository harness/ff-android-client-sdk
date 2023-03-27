package io.harness.cfsdk.cloud.analytics;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.analytics.api.MetricsApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.utils.CfUtils;
import io.harness.cfsdk.utils.TlsUtils;

public class DefaultMetricsApiFactoryRecipe implements MetricsApiFactoryRecipe {

    private final String logTag;

    {

        logTag = DefaultMetricsApiFactoryRecipe.class.getSimpleName();
    }

    @Override
    public MetricsApi create(String authToken, CfConfiguration config, AuthInfo authInfo) {

        final DefaultApi metricsAPI = new DefaultApi();

        if (!CfUtils.Text.isEmpty(config.getEventURL())) {

            ApiClient apiClient = metricsAPI.getApiClient();
            apiClient.setBasePath(config.getEventURL());
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            apiClient.setUserAgent("android " + ANDROID_SDK_VERSION);
            TlsUtils.setupTls(apiClient, config);
            String hostname = "UnknownHost";

            try {

                hostname = InetAddress.getLocalHost().getHostName();
                CfLog.OUT.v(logTag, "Hostname: " + hostname);

            } catch (UnknownHostException e) {

                CfLog.OUT.w(logTag, "Unable to get hostname");
            }

            apiClient.addDefaultHeader("Hostname", hostname);
            apiClient.addDefaultHeader("Harness-SDK-Info", "Android " + ANDROID_SDK_VERSION + " Client");
            apiClient.addDefaultHeader("Harness-EnvironmentID", authInfo.getEnvironmentIdentifier());
            apiClient.addDefaultHeader("Harness-AccountID", authInfo.getAccountID());
            metricsAPI.setApiClient(apiClient);
        }

        return metricsAPI;
    }
}
