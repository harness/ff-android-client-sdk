package io.harness.cfsdk.cloud.events;

import io.harness.cfsdk.cloud.model.AuthInfo;

/**
 * This class will be removed in a future version of the SDK.
 * Use {@link io.harness.cfsdk.CfClient#waitForInitialization()} instead to wait for authentication
 * and flag cache loading to complete.
 */
@Deprecated
public interface AuthCallback {

    void authorizationSuccess(AuthInfo authInfo, AuthResult result);
}
