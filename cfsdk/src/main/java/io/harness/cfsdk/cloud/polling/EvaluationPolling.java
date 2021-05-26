package io.harness.cfsdk.cloud.polling;

public interface EvaluationPolling {

    void start(Runnable runnable);

    void stop();
}