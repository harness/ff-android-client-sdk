package io.harness.cfsdk.cloud.oksse;

public class SSEAuthentication {
    private String authToken;
    private String apiToken;

    public SSEAuthentication(String authToken, String apiToken) {
        this.authToken = authToken;
        this.apiToken = apiToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getApiToken() {
        return apiToken;
    }
}
