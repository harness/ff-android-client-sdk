package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.cloud.analytics.cache.AnalyticsCache;
import io.harness.cfsdk.cloud.analytics.cache.DefaultAnalyticsCache;
import io.harness.cfsdk.logging.CfLog;

public class AnalyticsCacheFactory {

    private static final String LOG_TAG;
    public static final String DEFAULT_CACHE;

    static {

        DEFAULT_CACHE = "defaultCache";
        LOG_TAG = AnalyticsCacheFactory.class.getSimpleName();
    }

    public static AnalyticsCache create(String cacheName) {

        if (cacheName.equals(DEFAULT_CACHE)) {

            CfLog.OUT.d(LOG_TAG, "Using default cache");
            return new DefaultAnalyticsCache();
        }
        return null;
    }
}
