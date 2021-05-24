package io.harness.cfsdk.cloud;

import java.util.List;

import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.oksse.SSEAuthentication;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.logging.CfLog;

public class Cloud implements FeatureService {

    private final String key;
    private String authToken;
    private AuthInfo authInfo;
    private final String logTag;
    private final Target target;
    private DefaultApi defaultApi;
    private final String streamUrl;
    private final ApiClient apiClient;
    private final CloudFactory cloudFactory;
    private final TokenProvider tokenProvider;
    private final AuthResponseDecoder authResponseDecoder;

    {

        logTag = Cloud.class.getSimpleName();
    }

    public Cloud(

            CloudFactory cloudFactory,
            String sseUrl,
            String baseUrl,
            String key,
            Target target
    ) {

        this.cloudFactory = cloudFactory;
        this.key = key;
        this.authResponseDecoder = cloudFactory.getAuthResponseDecoder();
        this.streamUrl = sseUrl;
        this.tokenProvider = cloudFactory.tokenProvider();
        apiClient = cloudFactory.apiClient();
        this.target = target;
        apiClient.setDebugging(false);
        apiClient.setBasePath(baseUrl);
    }

    public AuthInfo getAuthInfo() {

        return authInfo;
    }

    public List<FeatureConfig> getFeatureConfig(final String environmentID) throws ApiException {

        return defaultApi.getFeatureConfig(environmentID);
    }

    private void authenticate() {
        defaultApi = cloudFactory.defaultApi(apiClient);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.apiKey(this.key);
        authenticationRequest.setTarget(this.target);
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

    private String buildSSEUrl() {
        return this.streamUrl;
    }

    public SSEConfig getConfig() {
        return new SSEConfig(buildSSEUrl(), new SSEAuthentication(this.authToken, this.key));
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

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
        return null;
    }

    public ApiResponse getEvaluationForId(String identifier, String target) {

        try {
            return new ApiResponse(

                    200,
                    "",
                    defaultApi.getEvaluationByIdentifier(
                            this.authInfo.getEnvironment(),
                            identifier,
                            target
                    )
            );
        } catch (ApiException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
        return null;
    }
}
