package io.harness.cfsdk.cloud.analytics.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.harness.cfsdk.cloud.analytics.model.Metrics;
import io.harness.cfsdk.cloud.core.client.ApiCallback;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.client.ApiResponse;
import io.harness.cfsdk.cloud.core.client.Configuration;
import io.harness.cfsdk.cloud.core.client.Pair;
import io.harness.cfsdk.logging.CfLog;
import okhttp3.Call;

public class DefaultApi {

    private final String logTag;
    private ApiClient localVarApiClient;

    {

        logTag = DefaultApi.class.getSimpleName();
    }

    public DefaultApi() {

        this(Configuration.getDefaultApiClient());
    }

    public DefaultApi(ApiClient apiClient) {

        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {

        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    /**
     * Build call for postMetrics
     *
     * @param environment environment parameter in query. (required)
     * @param cluster     Cluster identifier.
     * @param metrics     (optional)
     * @param _callback   Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     */
    public okhttp3.Call postMetricsCall(

            String environment,
            String cluster,
            Metrics metrics,
            final ApiCallback _callback

    ) throws ApiException {

        Object localVarPostBody = metrics;

        // create path and map variables
        String localVarPath = "/metrics/{environment}"
                .replaceAll("\\{" + "environment" + "\\}", localVarApiClient.escapeString(environment.toString()));

        List<Pair> localVarQueryParams = new ArrayList<>(localVarApiClient.parameterToPair("cluster", cluster));
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();
        final String[] localVarAccepts = {

                "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
                "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"BearerAuth"};

        final Call call = localVarApiClient.buildCall(

                localVarPath,
                "POST",
                localVarQueryParams,
                localVarCollectionQueryParams,
                localVarPostBody,
                localVarHeaderParams,
                localVarCookieParams,
                localVarFormParams,
                localVarAuthNames,
                _callback
        );

        CfLog.OUT.v(logTag, "Metrics API, url: " + call.request().url().url());

        return call;
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call postMetricsValidateBeforeCall(

            String environment,
            String cluster,
            Metrics metrics,
            final ApiCallback _callback

    ) throws ApiException {

        // verify the required parameter 'environment' is set
        if (environment == null) {

            throw new ApiException("Missing the required parameter 'environment' when calling postMetrics(Async)");
        }
        return postMetricsCall(environment, cluster, metrics, _callback);
    }

    /**
     * Send metrics to the Analytics server.
     * Send metrics to Analytics server
     *
     * @param environment environment parameter in query. (required)
     * @param cluster     Cluster identifier.
     * @param metrics     (optional)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public void postMetrics(

            String environment,
            String cluster,
            Metrics metrics

    ) throws ApiException {

        final ApiResponse<Void> response = postMetricsWithHttpInfo(environment, cluster, metrics);

        if (response != null) {

            CfLog.OUT.v(

                    logTag,
                    String.format("Metrics API, response code: %s", response.getStatusCode())
            );

        } else {

            CfLog.OUT.e(logTag, "Metrics API, got null response");
        }
    }

    /**
     * Send metrics to the Analytics server.
     * Send metrics to Analytics server
     *
     * @param environment Environment parameter in query. (required)
     * @param cluster     Cluster identifier.
     * @param metrics     (optional)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    public ApiResponse<Void> postMetricsWithHttpInfo(

            String environment,
            String cluster,
            Metrics metrics

    ) throws ApiException {

        okhttp3.Call localVarCall = postMetricsValidateBeforeCall(

                environment, cluster, metrics, null
        );
        return localVarApiClient.execute(localVarCall);
    }

    /**
     * Send metrics to the Analytics server. (asynchronously)
     * Send metrics to Analytics server
     *
     * @param environment environment parameter in query. (required)
     * @param cluster     Cluster identifier.
     * @param metrics     (optional)
     * @param _callback   The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     */
    public okhttp3.Call postMetricsAsync(

            String environment,
            String cluster,
            Metrics metrics,
            final ApiCallback<Void> _callback

    ) throws ApiException {

        okhttp3.Call localVarCall = postMetricsValidateBeforeCall(

                environment, cluster, metrics, _callback
        );
        localVarApiClient.executeAsync(localVarCall, _callback);
        return localVarCall;
    }
}
