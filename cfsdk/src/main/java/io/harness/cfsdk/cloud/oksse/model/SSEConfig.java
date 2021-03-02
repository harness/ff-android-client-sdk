package io.harness.cfsdk.cloud.oksse.model;

public class SSEConfig {
    private String url;
    private String token;

    public SSEConfig(String url, String token) {
        this.url = url;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public boolean isValid() {
        return token != null && url != null && !url.isEmpty();
    }
}
