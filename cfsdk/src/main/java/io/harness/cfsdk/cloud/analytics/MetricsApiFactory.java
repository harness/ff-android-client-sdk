package io.harness.cfsdk.cloud.analytics;



import io.harness.cfsdk.CfClientException;
import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.analytics.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.core.model.AuthenticationResponse;
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
    private static final int AUTH_RETRY_MAX_RETRY_COUNT = 3;
    private static final long AUTH_RETRY_INTERNAL_MILLIS = 1000;

    static {

        logTag = MetricsApiFactory.class.getSimpleName();
    }

    public static DefaultApi create(String apiKey, CfConfiguration config) {

        DefaultApi metricsAPI = new DefaultApi();
        io.harness.cfsdk.cloud.core.api.DefaultApi clientAPI =
                new io.harness.cfsdk.cloud.core.api.DefaultApi();

        if (!CfUtils.Text.isEmpty(config.getStreamURL())) {

            ApiClient apiClient = metricsAPI.getApiClient();
            apiClient.setBasePath(config.getStreamURL());
            metricsAPI.setApiClient(apiClient);
        }

        int count = 0;
        while (count < AUTH_RETRY_MAX_RETRY_COUNT) {
            try {

                auth(metricsAPI, apiKey, clientAPI);
                break;
            } catch (Exception apiException) {

                count++;
                CfLog.OUT.e(logTag, "Failed to get auth token ", apiException);
                try {

                    Thread.sleep(AUTH_RETRY_INTERNAL_MILLIS);
                } catch (InterruptedException e) {

                    CfLog.OUT.e(logTag, e.getMessage(), e);
                }
            }
        }
        return metricsAPI;
    }

    public static void auth(

            DefaultApi metricsAPI,
            String apiKey,
            io.harness.cfsdk.cloud.core.api.DefaultApi clientAPI

    ) throws CfClientException {

        String authToken = getAuthToken(clientAPI, apiKey);
        ApiClient apiClient = metricsAPI.getApiClient();
        apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
        metricsAPI.setApiClient(apiClient);
    }

    public static String getAuthToken(

            io.harness.cfsdk.cloud.core.api.DefaultApi defaultApi,
            String apiKey

    ) throws CfClientException {

        AuthenticationResponse authResponse;
        try {

            final AuthenticationRequest request = new AuthenticationRequest();
            request.setApiKey(apiKey);
            authResponse = defaultApi.authenticate(request);
            return authResponse.getAuthToken();
        } catch (ApiException apiException) {

            if (apiException.getCode() == 401) {

                throw new CfClientException(String.format("Invalid apiKey %s. Exiting. ", apiKey));
            }
            CfLog.OUT.e(logTag, "Failed to get auth token", apiException);
        }
        return null;
    }
}
