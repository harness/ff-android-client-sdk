package io.harness.cfsdk.cloud;

import io.harness.cfsdk.cloud.openapi.client.ApiException;
import io.harness.cfsdk.cloud.model.AuthInfo;

public interface ICloud extends FeatureService {

    AuthInfo getAuthInfo();

    String getAuthToken();

    boolean isInitialized();

    boolean initialize() throws ApiException;
}
