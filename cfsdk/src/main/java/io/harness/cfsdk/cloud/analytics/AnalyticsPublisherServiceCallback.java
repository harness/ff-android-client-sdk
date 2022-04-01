package io.harness.cfsdk.cloud.analytics;

/**
 * Analytics Publisher Service Callback
 */
public interface AnalyticsPublisherServiceCallback {

    /**
     * On analytics sent results callback method.
     *
     * @param success True == Sending was successful.
     */
    void onAnalyticsSent(boolean success);
}
