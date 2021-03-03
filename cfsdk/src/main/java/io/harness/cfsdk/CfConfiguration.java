package io.harness.cfsdk;

/**
 * Main configuration class used to tune the behaviour of {@link CfClient}. It uses builder pattern.
 */
public class CfConfiguration {
    private static final String BASE_URL = "https://config.feature-flags.uat.harness.io/api/1.0";
    private static final String STREAM_URL = BASE_URL + "/stream/environments/";

    private final String baseURL;
    private final String streamURL;
    private final boolean streamEnabled;
    private final int pollingInterval;
    private final String target;

    CfConfiguration(String baseURL, String streamURL, boolean streamEnabled, int pollingInterval, String target) {
        this.baseURL = baseURL;
        this.streamURL = streamURL;
        this.streamEnabled = streamEnabled;
        this.pollingInterval = pollingInterval;
        this.target = target;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public boolean getStreamEnabled() {
        return streamEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public String getTarget() {
        return target;
    }

    public static class Builder {

        private String baseURL;
        private String streamURL;
        private boolean streamEnabled;
        private int pollingInterval;
        private String target;

        /**
         * Sets the base API url
         */
        public Builder baseUrl(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        /**
         * Sets the stream url to be used for realtime evaluation update
         * */
        public Builder streamUrl(String streamURL) {
            this.streamURL = streamURL;
            return this;
        }

        /**
         * Configuration to explicitly enable or disable stream. If enabled, the stream url must be valid.
         * */
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

        public Builder target(String target) {
            this.target = target;
            return this;
        }

        public CfConfiguration build() {
            if (baseURL == null || baseURL.isEmpty()) {
                baseURL = BASE_URL;
            }
            if (streamEnabled && (streamURL == null || streamURL.isEmpty())) {
                streamURL = STREAM_URL;
            }
            if (target == null) throw new IllegalArgumentException("target must not be null!");
            return new CfConfiguration(baseURL, streamURL, streamEnabled, pollingInterval, target);
        }
    }
}
