package io.harness.cfsdk.cloud.oksse;

public class SSEAuthentication {

    private final String authToken;
    private final String apiToken;

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
