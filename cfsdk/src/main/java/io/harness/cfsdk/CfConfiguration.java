package io.harness.cfsdk;

import io.harness.cfsdk.cloud.analytics.AnalyticsCacheFactory;

/**
 * Main configuration class used to tune the behaviour of {@link CfClient}. It uses builder pattern.
 */
public class CfConfiguration {

    public static final int MIN_FREQUENCY;

    private static final String BASE_URL;
    private static final String EVENT_URL;
    private static final String STREAM_URL;

    private final int frequency;
    private final int bufferSize;
    private final int pollingInterval;

    private final String baseURL;
    private final String eventURL;
    private final String streamURL;

    private final boolean streamEnabled;
    private final boolean analyticsEnabled;

    private String analyticsCacheType;

    static {

        MIN_FREQUENCY = 60;
    }

    {

        bufferSize = 1024;
        frequency = MIN_FREQUENCY; // unit: second
        analyticsCacheType = AnalyticsCacheFactory.GUAVA_CACHE;
    }

    static {

        BASE_URL = "https://config.feature-flags.uat.harness.io/api/1.0";
        STREAM_URL = BASE_URL + "/stream";
        EVENT_URL = "https://event.feature-flags.uat.harness.io/api/1.0";
    }

    protected CfConfiguration(

            String baseURL,
            String streamURL,
            boolean streamEnabled,
            boolean analyticsEnabled,
            int pollingInterval
    ) {

        this.baseURL = baseURL;
        this.streamURL = streamURL;
        this.eventURL = EVENT_URL;
        this.streamEnabled = streamEnabled;
        this.pollingInterval = pollingInterval;
        this.analyticsEnabled = analyticsEnabled;
    }

    CfConfiguration(

            String baseURL,
            String streamURL,
            String eventURL,
            boolean streamEnabled,
            boolean analyticsEnabled,
            int pollingInterval
    ) {

        this.baseURL = baseURL;
        this.streamURL = streamURL;
        this.eventURL = eventURL;
        this.streamEnabled = streamEnabled;
        this.pollingInterval = pollingInterval;
        this.analyticsEnabled = analyticsEnabled;
    }

    public String getBaseURL() {

        return baseURL;
    }

    public String getStreamURL() {

        return streamURL;
    }

    public String getEventURL() {
        return eventURL;
    }

    public boolean getStreamEnabled() {
        return streamEnabled;
    }

    /**
     * Are analytics enabled?
     *
     * @return True == Using SDK metrics is enabled.
     */
    public boolean isAnalyticsEnabled() {

        return analyticsEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public static class Builder {

        private String baseURL;
        private String eventURL;
        private String streamURL;
        private int pollingInterval;
        private boolean streamEnabled;
        private boolean analyticsEnabled;

        /**
         * Sets the base API url
         */
        public Builder baseUrl(String baseURL) {

            this.baseURL = baseURL;
            return this;
        }

        /**
         * Sets the metrics events API url
         */
        public Builder eventUrl(String eventURL) {

            this.eventURL = eventURL;
            return this;
        }

        /**
         * Sets the stream url to be used for realtime evaluation update
         */
        public Builder streamUrl(String streamURL) {

            this.streamURL = streamURL;
            return this;
        }

        /**
         * Configuration to explicitly enable or disable analytics. If enabled, SDK metrics will be used.
         */
        public Builder enableAnalytics(boolean analyticsEnabled) {

            this.analyticsEnabled = analyticsEnabled;
            return this;
        }

        /**
         * Configuration to explicitly enable or disable stream. If enabled, the stream url must be valid.
         */
        public Builder enableStream(boolean streamEnabled) {

            this.streamEnabled = streamEnabled;
            return this;
        }

        /**
         * Polling interval to use when getting new evaluation data from server
         */
        public Builder pollingInterval(int pollingInterval) {

            this.pollingInterval = pollingInterval;
            return this;
        }

        public CfConfiguration build() {

            if (baseURL == null || baseURL.isEmpty()) {

                baseURL = BASE_URL;
            }
            if (eventURL == null || eventURL.isEmpty()) {

                eventURL = EVENT_URL;
            }
            if (streamEnabled && (streamURL == null || streamURL.isEmpty())) {

                streamURL = STREAM_URL;
            }
            return new CfConfiguration(

                    baseURL, streamURL, streamEnabled, analyticsEnabled, pollingInterval
            );
        }
    }

    public int getFrequency() {

        return Math.max(frequency, MIN_FREQUENCY);
    }

    /*
     BufferSize must be a power of 2 for LMAX to work. This function vaidates
     that. Source: https://stackoverflow.com/a/600306/1493480
    */
    public int getBufferSize() {

        return bufferSize;
    }

    public String getAnalyticsCacheType() {

        return analyticsCacheType;
    }

    public void setAnalyticsCacheType(String analyticsCacheType) {

        this.analyticsCacheType = analyticsCacheType;
    }
}
