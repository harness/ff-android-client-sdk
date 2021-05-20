/*
 * Harness feature flag service client apis
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 1.0.0
 * Contact: cf@harness.io
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.harness.cfsdk.cloud.core.api;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
import io.harness.cfsdk.cloud.core.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.core.model.AuthenticationResponse;
import io.harness.cfsdk.cloud.core.model.Evaluation;

public class DefaultApi {

    private ApiClient localVarApiClient;

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
     * Build call for authenticate
     *
     * @param authenticationRequest (optional)
     * @param _callback             Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      <tr><td> 401 </td><td> Unauthenticated </td><td>  -  </td></tr>
     *                      <tr><td> 403 </td><td> Unauthorized </td><td>  -  </td></tr>
     *                      <tr><td> 404 </td><td> The specified resource was not found </td><td>  -  </td></tr>
     *                      <tr><td> 500 </td><td> Internal server error </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call authenticateCall(

            final AuthenticationRequest authenticationRequest,
            final ApiCallback _callback
    ) throws ApiException {

        Object localVarPostBody = authenticationRequest;

        // create path and map variables
        String localVarPath = "/client/auth";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
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

        String[] localVarAuthNames = new String[]{};
        return localVarApiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call authenticateValidateBeforeCall(AuthenticationRequest authenticationRequest, final ApiCallback _callback) throws ApiException {

        okhttp3.Call localVarCall = authenticateCall(authenticationRequest, _callback);
        return localVarCall;

    }

    /**
     * Authenticate with the admin server.
     * Used to retrieve all target segments for certain account id.
     *
     * @param authenticationRequest (optional)
     * @return AuthenticationResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      <tr><td> 401 </td><td> Unauthenticated </td><td>  -  </td></tr>
     *                      <tr><td> 403 </td><td> Unauthorized </td><td>  -  </td></tr>
     *                      <tr><td> 404 </td><td> The specified resource was not found </td><td>  -  </td></tr>
     *                      <tr><td> 500 </td><td> Internal server error </td><td>  -  </td></tr>
     *                      </table>
     */
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) throws ApiException {

        ApiResponse<AuthenticationResponse> localVarResp = authenticateWithHttpInfo(authenticationRequest);
        return localVarResp.getData();
    }

    /**
     * Authenticate with the admin server.
     * Used to retrieve all target segments for certain account id.
     *
     * @param authenticationRequest (optional)
     * @return ApiResponse&lt;AuthenticationResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      <tr><td> 401 </td><td> Unauthenticated </td><td>  -  </td></tr>
     *                      <tr><td> 403 </td><td> Unauthorized </td><td>  -  </td></tr>
     *                      <tr><td> 404 </td><td> The specified resource was not found </td><td>  -  </td></tr>
     *                      <tr><td> 500 </td><td> Internal server error </td><td>  -  </td></tr>
     *                      </table>
     */
    public ApiResponse<AuthenticationResponse> authenticateWithHttpInfo(AuthenticationRequest authenticationRequest) throws ApiException {

        okhttp3.Call localVarCall = authenticateValidateBeforeCall(authenticationRequest, null);
        Type localVarReturnType = new TypeToken<AuthenticationResponse>() {
        }.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Authenticate with the admin server. (asynchronously)
     * Used to retrieve all target segments for certain account id.
     *
     * @param authenticationRequest (optional)
     * @param _callback             The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      <tr><td> 401 </td><td> Unauthenticated </td><td>  -  </td></tr>
     *                      <tr><td> 403 </td><td> Unauthorized </td><td>  -  </td></tr>
     *                      <tr><td> 404 </td><td> The specified resource was not found </td><td>  -  </td></tr>
     *                      <tr><td> 500 </td><td> Internal server error </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call authenticateAsync(AuthenticationRequest authenticationRequest, final ApiCallback<AuthenticationResponse> _callback) throws ApiException {

        okhttp3.Call localVarCall = authenticateValidateBeforeCall(authenticationRequest, _callback);
        Type localVarReturnType = new TypeToken<AuthenticationResponse>() {
        }.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }

    /**
     * Build call for getEvaluationByIdentifier
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param feature         Unique identifier for the flag object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @param _callback       Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call getEvaluationByIdentifierCall(String environmentUUID, String feature, String target, final ApiCallback _callback) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/client/env/{environmentUUID}/target/{target}/evaluations/{feature}"
                .replaceAll("\\{" + "environmentUUID" + "\\}", localVarApiClient.escapeString(environmentUUID.toString()))
                .replaceAll("\\{" + "feature" + "\\}", localVarApiClient.escapeString(feature.toString()))
                .replaceAll("\\{" + "target" + "\\}", localVarApiClient.escapeString(target.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
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

        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{};
        return localVarApiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getEvaluationByIdentifierValidateBeforeCall(String environmentUUID, String feature, String target, final ApiCallback _callback) throws ApiException {

        // verify the required parameter 'environmentUUID' is set
        if (environmentUUID == null) {
            throw new ApiException("Missing the required parameter 'environmentUUID' when calling getEvaluationByIdentifier(Async)");
        }

        // verify the required parameter 'feature' is set
        if (feature == null) {
            throw new ApiException("Missing the required parameter 'feature' when calling getEvaluationByIdentifier(Async)");
        }

        // verify the required parameter 'target' is set
        if (target == null) {
            throw new ApiException("Missing the required parameter 'target' when calling getEvaluationByIdentifier(Async)");
        }


        okhttp3.Call localVarCall = getEvaluationByIdentifierCall(environmentUUID, feature, target, _callback);
        return localVarCall;

    }

    /**
     * Get feature evaluations for target
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param feature         Unique identifier for the flag object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @return Evaluation
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public Evaluation getEvaluationByIdentifier(String environmentUUID, String feature, String target) throws ApiException {
        ApiResponse<Evaluation> localVarResp = getEvaluationByIdentifierWithHttpInfo(environmentUUID, feature, target);
        return localVarResp.getData();
    }

    /**
     * Get feature evaluations for target
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param feature         Unique identifier for the flag object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @return ApiResponse&lt;Evaluation&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public ApiResponse<Evaluation> getEvaluationByIdentifierWithHttpInfo(String environmentUUID, String feature, String target) throws ApiException {
        okhttp3.Call localVarCall = getEvaluationByIdentifierValidateBeforeCall(environmentUUID, feature, target, null);
        Type localVarReturnType = new TypeToken<Evaluation>() {
        }.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get feature evaluations for target (asynchronously)
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param feature         Unique identifier for the flag object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @param _callback       The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call getEvaluationByIdentifierAsync(String environmentUUID, String feature, String target, final ApiCallback<Evaluation> _callback) throws ApiException {

        okhttp3.Call localVarCall = getEvaluationByIdentifierValidateBeforeCall(environmentUUID, feature, target, _callback);
        Type localVarReturnType = new TypeToken<Evaluation>() {
        }.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }

    /**
     * Build call for getEvaluations
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @param _callback       Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call getEvaluationsCall(String environmentUUID, String target, final ApiCallback _callback) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/client/env/{environmentUUID}/target/{target}/evaluations"
                .replaceAll("\\{" + "environmentUUID" + "\\}", localVarApiClient.escapeString(environmentUUID.toString()))
                .replaceAll("\\{" + "target" + "\\}", localVarApiClient.escapeString(target.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
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

        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{};
        return localVarApiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call getEvaluationsValidateBeforeCall(String environmentUUID, String target, final ApiCallback _callback) throws ApiException {

        // verify the required parameter 'environmentUUID' is set
        if (environmentUUID == null) {
            throw new ApiException("Missing the required parameter 'environmentUUID' when calling getEvaluations(Async)");
        }

        // verify the required parameter 'target' is set
        if (target == null) {
            throw new ApiException("Missing the required parameter 'target' when calling getEvaluations(Async)");
        }


        okhttp3.Call localVarCall = getEvaluationsCall(environmentUUID, target, _callback);
        return localVarCall;

    }

    /**
     * Get feature evaluations for target
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @return List&lt;Evaluation&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public List<Evaluation> getEvaluations(String environmentUUID, String target) throws ApiException {
        ApiResponse<List<Evaluation>> localVarResp = getEvaluationsWithHttpInfo(environmentUUID, target);
        return localVarResp.getData();
    }

    /**
     * Get feature evaluations for target
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @return ApiResponse&lt;List&lt;Evaluation&gt;&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public ApiResponse<List<Evaluation>> getEvaluationsWithHttpInfo(String environmentUUID, String target) throws ApiException {
        okhttp3.Call localVarCall = getEvaluationsValidateBeforeCall(environmentUUID, target, null);
        Type localVarReturnType = new TypeToken<List<Evaluation>>() {
        }.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Get feature evaluations for target (asynchronously)
     *
     * @param environmentUUID Unique identifier for the environment object in the API. (required)
     * @param target          Unique identifier for the target object in the API. (required)
     * @param _callback       The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call getEvaluationsAsync(String environmentUUID, String target, final ApiCallback<List<Evaluation>> _callback) throws ApiException {

        okhttp3.Call localVarCall = getEvaluationsValidateBeforeCall(environmentUUID, target, _callback);
        Type localVarReturnType = new TypeToken<List<Evaluation>>() {
        }.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }

    /**
     * Build call for stream
     *
     * @param environmentId Unique UUID for the environemnt object in the API. (required)
     * @param _callback     Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  * Content-Type -  <br>  * Cache-Control -  <br>  * Connection -  <br>  * Access-Control-Allow-Origin -  <br>  </td></tr>
     *                      <tr><td> 503 </td><td> Service Unavailable </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call streamCall(String environmentId, final ApiCallback _callback) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/stream/environments/{environmentId}"
                .replaceAll("\\{" + "environmentId" + "\\}", localVarApiClient.escapeString(environmentId.toString()));

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();
        final String[] localVarAccepts = {

        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[]{"BearerAuth"};
        return localVarApiClient.buildCall(localVarPath, "GET", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call streamValidateBeforeCall(String environmentId, final ApiCallback _callback) throws ApiException {

        // verify the required parameter 'environmentId' is set
        if (environmentId == null) {
            throw new ApiException("Missing the required parameter 'environmentId' when calling stream(Async)");
        }


        okhttp3.Call localVarCall = streamCall(environmentId, _callback);
        return localVarCall;

    }

    /**
     * Stream endpoint.
     *
     * @param environmentId Unique UUID for the environemnt object in the API. (required)
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  * Content-Type -  <br>  * Cache-Control -  <br>  * Connection -  <br>  * Access-Control-Allow-Origin -  <br>  </td></tr>
     *                      <tr><td> 503 </td><td> Service Unavailable </td><td>  -  </td></tr>
     *                      </table>
     */
    public void stream(String environmentId) throws ApiException {

        streamWithHttpInfo(environmentId);
    }

    /**
     * Stream endpoint.
     *
     * @param environmentId Unique UUID for the environemnt object in the API. (required)
     * @return ApiResponse&lt;Void&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  * Content-Type -  <br>  * Cache-Control -  <br>  * Connection -  <br>  * Access-Control-Allow-Origin -  <br>  </td></tr>
     *                      <tr><td> 503 </td><td> Service Unavailable </td><td>  -  </td></tr>
     *                      </table>
     */
    public ApiResponse<Void> streamWithHttpInfo(String environmentId) throws ApiException {

        okhttp3.Call localVarCall = streamValidateBeforeCall(environmentId, null);
        return localVarApiClient.execute(localVarCall);
    }

    /**
     * Stream endpoint. (asynchronously)
     *
     * @param environmentId Unique UUID for the environemnt object in the API. (required)
     * @param _callback     The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     *
     *                      <table summary="Response Details" border="1">
     *                      <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
     *                      <tr><td> 200 </td><td> OK </td><td>  * Content-Type -  <br>  * Cache-Control -  <br>  * Connection -  <br>  * Access-Control-Allow-Origin -  <br>  </td></tr>
     *                      <tr><td> 503 </td><td> Service Unavailable </td><td>  -  </td></tr>
     *                      </table>
     */
    public okhttp3.Call streamAsync(String environmentId, final ApiCallback<Void> _callback) throws ApiException {

        okhttp3.Call localVarCall = streamValidateBeforeCall(environmentId, _callback);
        localVarApiClient.executeAsync(localVarCall, _callback);
        return localVarCall;
    }
}
