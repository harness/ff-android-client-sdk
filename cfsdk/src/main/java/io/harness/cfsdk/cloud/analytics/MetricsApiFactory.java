package io.harness.cfsdk.cloud.analytics;

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.harness.cfsdk.BuildConfig;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.logging.CfLog;
import io.harness.cfsdk.utils.CfUtils;

/**
 * This is a factory class to provide the API for metrics related operations.
 *
 * @author Subir.Adhikari
 * @version 1.0
 * @since 08/01/2021
 */
public class MetricsApiFactory {

    private static final String logTag;

    static {

        logTag = MetricsApiFactory.class.getSimpleName();
    }

    public static DefaultApi create(

            final String authToken,
            final CfConfiguration config
    ) {

        final DefaultApi metricsAPI = new DefaultApi();

        if (!CfUtils.Text.isEmpty(config.getEventURL())) {

            ApiClient apiClient = metricsAPI.getApiClient();
            apiClient.setBasePath(config.getEventURL());
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            apiClient.setUserAgent("android " + BuildConfig.APP_VERSION_NAME);
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
