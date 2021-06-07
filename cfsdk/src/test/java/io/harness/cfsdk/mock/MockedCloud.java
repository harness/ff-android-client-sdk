package io.harness.cfsdk.mock;

import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.ApiResponse;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.core.client.ApiException;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
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
        sseConfig = new SSEConfig(mock, sseAuthentication);
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
    public ApiResponse getEvaluations(String target) {

        return null;
    }

    @Override
    public ApiResponse getEvaluationForId(String identifier, String target) {

        return null;
    }

    @Override
    public List<FeatureConfig> getFeatureConfig(String environmentID, String clusterID) throws ApiException {

        return new LinkedList<>();
    }

    @Override
    public FeatureConfig getFeatureConfigByIdentifier(String identifier, String environmentUUID, String clusterIdentifier) throws ApiException {

        return new FeatureConfig();
    }
}
