package io.harness.cfsdk.cloud;

public class ApiResponse {
    private int code;
    private String rawResponse;
    private Object body;

    public ApiResponse(int code, String rawResponse, Object body) {
        this.code = code;
        this.rawResponse = rawResponse;
        this.body = body;
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
