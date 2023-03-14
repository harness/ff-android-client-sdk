package io.harness.cfsdk.cloud.oksse.model;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.oksse.SSEAuthentication;

public class SSEConfig {
    private final String url;
    private final SSEAuthentication authentication;
    private final CfConfiguration cfConfig;

    public SSEConfig(String url, SSEAuthentication authentication, CfConfiguration cfConfig) {
        this.url = url;
        this.authentication = authentication;
        this.cfConfig = cfConfig;
    }

    public SSEAuthentication getAuthentication() {
        return authentication;
    }

    public String getUrl() {
        return url;
    }

    public boolean isValid() {
        return authentication.getAuthToken() != null && url != null && !url.isEmpty();
    }

    public CfConfiguration getCfConfig() {
        return cfConfig;
    }
}
