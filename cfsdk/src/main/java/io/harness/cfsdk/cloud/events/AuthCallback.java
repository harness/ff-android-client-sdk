package io.harness.cfsdk.cloud.events;

import io.harness.cfsdk.cloud.model.AuthInfo;

public interface AuthCallback {

    void authorizationSuccess(AuthInfo authInfo, AuthResult result);
}
