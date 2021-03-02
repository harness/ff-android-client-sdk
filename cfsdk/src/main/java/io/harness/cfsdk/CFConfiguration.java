package io.harness.cfsdk;

/**
 * Main configuration class used to tune the behaviour of {@link CFClient}. It uses builder pattern.
 */
public class CFConfiguration {
    private final String baseURL;
    private final String streamURL;
    private final boolean streamEnabled;
    private final int pollingInterval;
    private final String target;

    CFConfiguration(String baseURL, String streamURL, boolean streamEnabled, int pollingInterval, String target) {
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

        public CFConfiguration build() {
            if (baseURL == null || baseURL.isEmpty()) throw new IllegalArgumentException("Provided base url is not valid!");
            if (streamEnabled && (streamURL == null || streamURL.isEmpty())) throw new IllegalArgumentException("Stream configuration is wrong!");
            return new CFConfiguration(baseURL, streamURL, streamEnabled, pollingInterval, target);
        }
    }
}
