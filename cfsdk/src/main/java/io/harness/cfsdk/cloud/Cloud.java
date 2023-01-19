package io.harness.cfsdk.cloud;

import java.util.Locale;

import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.oksse.SSEAuthentication;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;
import io.harness.cfsdk.logging.CfLog;

public class Cloud implements ICloud {

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

        this.key = key;
        this.target = target;
        this.streamUrl = sseUrl;
        this.cloudFactory = cloudFactory;
        this.tokenProvider = cloudFactory.tokenProvider();
        this.authResponseDecoder = cloudFactory.getAuthResponseDecoder();

        apiClient = cloudFactory.apiClient();
        apiClient.setBasePath(baseUrl);
        apiClient.setDebugging(false);
    }

    @Override
    public ApiResponse getEvaluations(String target, String cluster) {

        try {

            return new ApiResponse(

                    200,
                    "",
                    defaultApi.getEvaluations(this.authInfo.getEnvironment(), target, cluster)
            );

        } catch (ApiException e) {

            CfLog.OUT.e(logTag, "API, Error: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ApiResponse getEvaluationForId(String identifier, String target, String cluster) {

        try {
            return new ApiResponse(

                    200,
                    "",
                    defaultApi.getEvaluationByIdentifier(

                            this.authInfo.getEnvironment(),
                            identifier,
                            target,
                            cluster
                    )
            );
        } catch (ApiException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean initialize() {

        this.authenticate();
        return this.isInitialized();
    }

    @Override
    public boolean isInitialized() {

        return this.authToken != null && this.authInfo != null &&
                this.authInfo.getEnvironmentIdentifier() != null;
    }

    @Override
    public AuthInfo getAuthInfo() {

        return authInfo;
    }

    @Override
    public String getAuthToken() {

        return tokenProvider.getToken(key);
    }

    @Override
    public SSEConfig getConfig() {

        return new SSEConfig(buildSSEUrl(), new SSEAuthentication(this.authToken, this.key));
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
            if (e.getCode() == 403) {
                String message = String.format(Locale.US,"API, Authentication denied: message=%s httpCode=%d", e.getMessage(), e.getCode());
                throw new RuntimeException(message, e);
            }
            this.authToken = this.tokenProvider.getToken(this.key);
        } finally {
            apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
            this.authInfo = authResponseDecoder.extractInfo(authToken);
        }
    }

    private String buildSSEUrl() {
        return this.streamUrl;
    }
}
