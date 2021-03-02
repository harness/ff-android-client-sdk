package io.harness.cfsdk.cloud;

import io.harness.cfsdk.BuildConfig;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;

public class Cloud implements FeatureService{

    private final CloudFactory cloudFactory;
    private final String key;
    private final AuthResponseDecoder authResponseDecoder;
    private final String streamUrl;
    private AuthInfo authInfo;

    private final ApiClient apiClient;
    private String authToken;
    private DefaultApi defaultApi;

    private final TokenProvider tokenProvider;

    public Cloud(CloudFactory cloudFactory, String sseUrl, String baseUrl, String key) {
        this.cloudFactory = cloudFactory;
        this.key = key;
        this.authResponseDecoder = cloudFactory.getAuthResponseDecoder();
        this.streamUrl = sseUrl;
        this.tokenProvider = cloudFactory.tokenProvider();
        apiClient = cloudFactory.apiClient();
        apiClient.setDebugging(true);
        apiClient.setBasePath(baseUrl);
    }

    public AuthInfo getAuthInfo() {
        return authInfo;
    }


    private void authenticate() {
        defaultApi = cloudFactory.defaultApi(apiClient);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.apiKey(this.key);
        try {
            authToken = defaultApi.authenticate(authenticationRequest).getAuthToken();
            this.tokenProvider.addToken(this.key, authToken);
        } catch (ApiException e) {
            this.authToken = this.tokenProvider.getToken(this.key);

        } finally {
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            this.authInfo = authResponseDecoder.extractInfo(authToken);
        }

    }

    public SSEConfig getConfig() {
        return new SSEConfig(this.streamUrl + this.authInfo.getEnvironment(), this.authToken);
    }

    public boolean isInitialized() {
        return this.authToken != null && this.authInfo != null &&
                this.authInfo.getEnvironmentIdentifier() != null;
    }

    public boolean initialize() {
        this.authenticate();
        return this.isInitialized();
    }

    public ApiResponse getEvaluations(String target) {
        try {
            return new ApiResponse(200, "", defaultApi.getEvaluations(this.authInfo.getEnvironment(), target));
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse getEvaluationForId(String identifier, String target) {

        try {
            return new ApiResponse(200,"",defaultApi.getEvaluationByIdentifier(this.authInfo.getEnvironment(),
                    identifier, target));
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return null;
    }


}
