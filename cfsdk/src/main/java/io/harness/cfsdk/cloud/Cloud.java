package io.harness.cfsdk.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.AuthenticationRequest;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.utils.TlsUtils;

public class Cloud implements ICloud {

    private static final Logger log = LoggerFactory.getLogger(Cloud.class);

    private final String key;
    private String authToken;
    private AuthInfo authInfo;
    private final Target target;
    private DefaultApi defaultApi;
    private final String streamUrl;
    private final ApiClient apiClient;
    private final CloudFactory cloudFactory;
    private final TokenProvider tokenProvider;
    private final AuthResponseDecoder authResponseDecoder;
    private final CfConfiguration config;

    public Cloud(

            CloudFactory cloudFactory,
            String sseUrl,
            String baseUrl,
            String key,
            Target target,
            CfConfiguration config
    ) {

        this.key = key;
        this.target = target;
        this.streamUrl = sseUrl;
        this.cloudFactory = cloudFactory;
        this.tokenProvider = cloudFactory.tokenProvider();
        this.authResponseDecoder = cloudFactory.getAuthResponseDecoder();
        this.config = config;

        apiClient = cloudFactory.apiClient();
        apiClient.setBasePath(baseUrl);
        apiClient.setDebugging(false);
        TlsUtils.setupTls(apiClient, config);
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
            log.warn("API, Error: {}", e.getMessage(), e);
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

            log.warn("{} - httpCode: {}", e.getMessage(),  e.getCode(), e);
        }
        return null;
    }

    @Override
    public boolean initialize() throws ApiException {

        this.authenticate();
        return this.isInitialized();
    }

    @Override
    public boolean isInitialized() {

        return this.authToken != null && this.authInfo != null &&
                this.authInfo.getEnvironment() != null;
    }

    @Override
    public AuthInfo getAuthInfo() {

        return authInfo;
    }

    @Override
    public String getAuthToken() {

        return tokenProvider.getToken(key);
    }

    private void authenticate() throws ApiException {

        defaultApi = cloudFactory.defaultApi(apiClient);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.apiKey(this.key);
        authenticationRequest.setTarget(this.target);
        authToken = defaultApi.authenticate(authenticationRequest).getAuthToken();
        this.tokenProvider.addToken(this.key, authToken);
        this.authToken = this.tokenProvider.getToken(this.key);
        apiClient.addDefaultHeader("Authorization", "Bearer " + authToken);
        this.authInfo = authResponseDecoder.extractInfo(authToken);

        if (authInfo != null) {
            String environmentHeader = authInfo.getEnvironmentTrackingHeader();

            apiClient.addDefaultHeader("Harness-EnvironmentID", environmentHeader);

            String accountID = authInfo.getAccountID();
            if (accountID != null) {
                apiClient.addDefaultHeader("Harness-AccountID", accountID);
            }
        }
    }

    private String buildSSEUrl() {
        return this.streamUrl;
    }
}
