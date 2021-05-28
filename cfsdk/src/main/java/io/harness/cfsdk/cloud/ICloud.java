package io.harness.cfsdk.cloud;

import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.oksse.model.SSEConfig;

public interface ICloud extends FeatureService {

    SSEConfig getConfig();

    AuthInfo getAuthInfo();

    String getAuthToken();

    boolean isInitialized();

    boolean initialize();
}
