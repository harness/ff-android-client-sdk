package io.harness.cfsdk;


import java.security.cert.X509Certificate;
import java.util.List;

import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.DefaultCache;

/**
 * Main configuration class used to tune the behaviour of {@link CfClient}. It uses builder pattern.
 */
public class CfConfiguration {

    public static final int DEFAULT_METRICS_CAPACITY = 1024;
    public static final int MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS = 60;
    public static final int DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS = 10;

    private static final String BASE_URL = "https://config.ff.harness.io/api/1.0";
    private static final String EVENT_URL = "https://events.ff.harness.io/api/1.0";

    private final String baseURL;
    private final String eventURL;
    private final String streamURL;

    private final boolean analyticsEnabled;
    private final boolean streamEnabled;

    private int metricsCapacity;
    private final int pollingInterval;
    private final List<X509Certificate> tlsTrustedCerts;

    private long metricsPublishingIntervalInMillis;
    private long metricsServiceAcceptableDurationInMillis;
    private boolean debugEnabled;

    private CloudCache cache;

    protected CfConfiguration(

            String baseURL,
            String streamURL,
            String eventURL,
            boolean streamEnabled,
            boolean analyticsEnabled,
            int pollingInterval,
            List<X509Certificate> tlsTrustedCerts

    ) {

        this.baseURL = baseURL;
        this.streamURL = streamURL;
        this.eventURL = eventURL;
        this.streamEnabled = streamEnabled;
        this.pollingInterval = pollingInterval;
        this.analyticsEnabled = analyticsEnabled;
        this.tlsTrustedCerts = tlsTrustedCerts;
        this.metricsCapacity = DEFAULT_METRICS_CAPACITY;
        this.metricsPublishingIntervalInMillis = MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS * 1000L;
        this.metricsServiceAcceptableDurationInMillis = DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS * 1000L;
        this.cache = null;
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

    public boolean isStreamEnabled() {

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

    public List<X509Certificate> getTlsTrustedCAs() {
        return tlsTrustedCerts;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public CloudCache getCache() {
        return cache;
    }

    public static class Builder {

        private String baseURL;
        private String eventURL;
        private String streamURL;
        private int pollingInterval;
        private int metricsCapacity = DEFAULT_METRICS_CAPACITY;
        private boolean streamEnabled = true;
        private boolean analyticsEnabled = true;
        private long metricsPublishingIntervalInMillis = MIN_METRICS_PUBLISHING_INTERVAL_IN_SECONDS * 1000L;
        private long metricsPublishingAcceptableDurationInMillis = DEFAULT_METRICS_PUBLISHING_ACCEPTABLE_DURATION_IN_SECONDS * 1000L;
        private List<X509Certificate> tlsTrustedCerts;
        private boolean debugEnabled = false;

        private CloudCache cache = null;

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
         * @deprecated If streamUrl is not given then stream URL will now be automatically derived from baseUrl
         */
        @Deprecated
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

        /**
         * @param tlsTrustedCerts list of trusted CAs - for when the given config/event URLs are
         *                        signed with a private CA. You should include intermediate CAs too
         *                        to allow the HTTP client to build the full trust chain.
         * @return This builder.
         */
        public Builder tlsTrustedCAs(List<X509Certificate> tlsTrustedCerts) {
            this.tlsTrustedCerts = tlsTrustedCerts;
            return this;
        }

        public Builder debug(boolean enabled) {
            this.debugEnabled = enabled;
            return this;
        }

        public Builder cache(CloudCache cache) {
            this.cache = cache;
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

        public List<X509Certificate> getTlsTrustedCAs() {
            return tlsTrustedCerts;
        }

        public boolean isDebugEnabled() {
            return debugEnabled;
        }

        public CloudCache getCache() {
            return cache;
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

                streamURL = baseURL + "/stream";
            }

            final CfConfiguration cfConfiguration = new CfConfiguration(

                    baseURL, streamURL, eventURL, streamEnabled, analyticsEnabled, pollingInterval, tlsTrustedCerts
            );

            cfConfiguration.setMetricsCapacity(metricsCapacity);
            cfConfiguration.setMetricsPublishingIntervalInMillis(metricsPublishingIntervalInMillis);
            cfConfiguration.setMetricsServiceAcceptableDurationInMillis(metricsPublishingAcceptableDurationInMillis);
            cfConfiguration.setDebugEnabled(debugEnabled);
            cfConfiguration.setCache(cache);
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

    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    private void setCache(CloudCache cache) {
        this.cache = cache;
    }
}
