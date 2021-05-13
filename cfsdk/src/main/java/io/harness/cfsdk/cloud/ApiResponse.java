package io.harness.cfsdk.cloud;

public class ApiResponse {

    private final int code;
    private final Object body;
    private final String rawResponse;

    public ApiResponse(int code, String rawResponse, Object body) {

        this.code = code;
        this.body = body;
        this.rawResponse = rawResponse;
    }

    public boolean isSuccess(){
        return code >= 200 && code < 300;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public <T> T body() {
        return (T) this.body;
    }

    public int getCode() {
        return code;
    }
}
