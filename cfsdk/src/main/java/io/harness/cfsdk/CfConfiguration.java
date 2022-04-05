package io.harness.cfsdk;


/**
 * Main configuration class used to tune the behaviour of {@link CfClient}. It uses builder pattern.
 */
public class CfConfiguration {

    public static final int DEFAULT_METRICS_CAPACITY;
    public static final int MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS;
    public static final int DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS;

    protected static final String BASE_URL;
    protected static final String EVENT_URL;
    protected static final String STREAM_URL;

    protected final String baseURL;
    protected final String eventURL;
    protected final String streamURL;

    protected boolean analyticsEnabled;
    protected final boolean streamEnabled;

    protected int metricsCapacity;
    protected final int pollingInterval;
    protected long metricsPublishingIntervalInMillis;
    protected long metricsServiceAcceptableDurationInMillis;

    static {

        DEFAULT_METRICS_CAPACITY = 1024;
        MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS = 60;
        DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS = 10;
    }

    {

        analyticsEnabled = true;
        metricsCapacity = DEFAULT_METRICS_CAPACITY;

        metricsPublishingIntervalInMillis =
                MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS * 1000L;

        metricsServiceAcceptableDurationInMillis =
                DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS * 1000L;
    }

    static {

        BASE_URL = "https://config.ff.harness.io/api/1.0";
        STREAM_URL = BASE_URL + "/stream";
        EVENT_URL = "https://events.ff.harness.io/api/1.0";
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

    protected CfConfiguration(

            String baseURL,
            String streamURL,
            boolean streamEnabled,
            int pollingInterval
    ) {

        this.baseURL = baseURL;
        this.streamURL = streamURL;
        this.eventURL = EVENT_URL;
        this.streamEnabled = streamEnabled;
        this.pollingInterval = pollingInterval;
    }

    protected CfConfiguration(

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

    CfConfiguration(

            String baseURL,
            String streamURL,
            String eventURL,
            boolean streamEnabled,
            int pollingInterval
    ) {

        this.baseURL = baseURL;
        this.streamURL = streamURL;
        this.eventURL = eventURL;
        this.streamEnabled = streamEnabled;
        this.pollingInterval = pollingInterval;
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
        private int metricsCapacity;
        private boolean streamEnabled;
        private boolean analyticsEnabled;
        private long metricsPublishingIntervalInMillis;
        private long metricsPublishingAcceptableDurationInMillis;

        {

            analyticsEnabled = true;
            metricsCapacity = DEFAULT_METRICS_CAPACITY;
            metricsPublishingIntervalInMillis = MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS * 1000L;

            metricsPublishingAcceptableDurationInMillis =
                    DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS * 1000L;
        }

        /**
         * Sets the base API url
         *
         * @param baseURL Base url for the API.
         * @return Builder instance.
         */
        public Builder baseUrl(String baseURL) {

            this.baseURL = baseURL;
            return this;
        }

        /**
         * Sets the metrics events API url
         *
         * @param eventURL Base url for the metrics API.
         * @return Builder instance.
         */
        public Builder eventUrl(String eventURL) {

            this.eventURL = eventURL;
            return this;
        }

        /**
         * Sets the stream url to be used for realtime evaluation update
         *
         * @param streamURL Base url for the SSE API.
         * @return Builder instance.
         */
        public Builder streamUrl(String streamURL) {

            this.streamURL = streamURL;
            return this;
        }

        /**
         * Configuration to explicitly enable or disable analytics. If enabled, SDK metrics will be used.
         *
         * @param analyticsEnabled True == Analytics enabled.
         * @return Builder instance.
         */
        public Builder enableAnalytics(boolean analyticsEnabled) {

            this.analyticsEnabled = analyticsEnabled;
            return this;
        }

        /**
         * Configuration to explicitly enable or disable stream. If enabled, the stream url must be valid.
         *
         * @param streamEnabled True == SSE is enabled.
         * @return Builder instance.
         */
        public Builder enableStream(boolean streamEnabled) {

            this.streamEnabled = streamEnabled;
            return this;
        }

        /**
         * Polling interval to use when getting new evaluation data from server
         *
         * @param pollingInterval Polling interval.
         * @return Builder instance.
         */
        public Builder pollingInterval(int pollingInterval) {

            this.pollingInterval = pollingInterval;
            return this;
        }

        /**
         * Metrics service publishing acceptable duration.
         *
         * @return Acceptable duration in milliseconds.
         */
        public long getMetricsPublishingAcceptableDurationInMillis() {

            return metricsPublishingAcceptableDurationInMillis;
        }

        /**
         * @param durationInMillis Metrics service publishing acceptable duration.
         * @return This builder.
         */
        public Builder metricsPublishingAcceptableDurationInMillis(long durationInMillis) {

            this.metricsPublishingAcceptableDurationInMillis = durationInMillis;
            return this;
        }

        /**
         * @param intervalInMillis Metrics publishing interval in millis.
         * @return This builder.
         */
        public Builder metricsPublishingIntervalInMillis(long intervalInMillis) {

            metricsPublishingIntervalInMillis = intervalInMillis;
            return this;
        }

        public int getMetricsCapacity() {

            return metricsCapacity;
        }

        public Builder metricsCapacity(int metricsCapacity) {

            this.metricsCapacity = metricsCapacity;
            return this;
        }

        public String getBaseURL() {

            return baseURL;
        }

        public String getEventURL() {

            return eventURL;
        }

        public String getStreamURL() {

            return streamURL;
        }

        public int getPollingInterval() {

            return pollingInterval;
        }

        public boolean isStreamEnabled() {

            return streamEnabled;
        }

        public boolean isAnalyticsEnabled() {

            return analyticsEnabled;
        }

        public long getMetricsPublishingIntervalInMillis() {

            return metricsPublishingIntervalInMillis;
        }

        /**
         * Build the configuration instance.
         *
         * @return Configuration instance.
         */
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

            final CfConfiguration cfConfiguration = new CfConfiguration(

                    baseURL, streamURL, eventURL, streamEnabled, analyticsEnabled, pollingInterval
            );

            cfConfiguration.setMetricsCapacity(metricsCapacity);
            cfConfiguration.setMetricsPublishingIntervalInMillis(metricsPublishingIntervalInMillis);
            cfConfiguration.setMetricsServiceAcceptableDurationInMillis(metricsPublishingAcceptableDurationInMillis);

            return cfConfiguration;
        }
    }

    public long getMetricsPublishingIntervalInMillis() {

        return Math.max(

                metricsPublishingIntervalInMillis,
                MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS * 1000L
        );
    }

    public void setMetricsCapacity(final int capacity) {

        metricsCapacity = capacity;
    }

    public int getMetricsCapacity() {

        return metricsCapacity;
    }

    public void setMetricsPublishingIntervalInMillis(long intervalInMillis) {

        metricsPublishingIntervalInMillis = intervalInMillis;
    }

    public long getMetricsServiceAcceptableDurationInMillis() {

        return metricsServiceAcceptableDurationInMillis;
    }

    public void setMetricsServiceAcceptableDurationInMillis(long durationInMillis) {

        this.metricsServiceAcceptableDurationInMillis = durationInMillis;
    }
}
