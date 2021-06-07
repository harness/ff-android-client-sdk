package io.harness.cfsdk;

public class CfClientException extends Exception {
    public CfClientException(String errorMessage) {
        super(errorMessage);
    }
}