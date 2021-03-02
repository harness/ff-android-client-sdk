package io.harness.cfsdk.cloud.polling;

public interface EvaluationPolling {

    public void start(Runnable runnable);

    public void stop();

}