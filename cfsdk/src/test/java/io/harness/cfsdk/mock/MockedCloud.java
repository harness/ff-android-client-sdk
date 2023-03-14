package io.harness.cfsdk.mock;


import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.SSEAuthentication;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;

public class MockedCloud implements ICloud {

    private boolean initialized;

    private final String mock;
    private final AuthInfo authInfo;
    private final SSEConfig sseConfig;

    {

        mock = "mock";
        authInfo = new AuthInfo(

                mock,
                mock,
                mock,
                mock,
                mock,
                mock,
                mock
        );

        SSEAuthentication sseAuthentication = new SSEAuthentication(mock, mock);
        sseConfig = new SSEConfig(mock, sseAuthentication, null);
    }


    @Override
    public SSEConfig getConfig() {

        return sseConfig;
    }

    @Override
    public AuthInfo getAuthInfo() {

        return authInfo;
    }

    @Override
    public String getAuthToken() {

        return mock;
    }

    @Override
    public boolean isInitialized() {

        return initialized;
    }

    @Override
    public boolean initialize() {

        initialized = true;
        return isInitialized();
    }

    @Override
    public ApiResponse getEvaluations(String target, String cluster) {

        return null;
    }

    @Override
    public ApiResponse getEvaluationForId(String identifier, String target, String cluster) {

        return null;
    }
}
