package io.harness.cfsdk.cloud.oksse.model;

import io.harness.cfsdk.cloud.oksse.SSEAuthentication;

public class SSEConfig {
    private String url;
    private SSEAuthentication authentication;

    public SSEConfig(String url, SSEAuthentication authentication) {
        this.url = url;
        this.authentication = authentication;
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
}
