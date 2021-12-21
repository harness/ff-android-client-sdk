package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.cache.GuavaCache;
import io.harness.cfsdk.logging.CfLog;

public class AnalyticsCacheFactory {

    private static final String LOG_TAG;
    public static final String DEFAULT_CACHE;

    static {

        DEFAULT_CACHE = "defaultCache";
        LOG_TAG = AnalyticsCacheFactory.class.getSimpleName();
    }

    public static Cache create(String cacheName) {

        if (cacheName.equals(DEFAULT_CACHE)) {

            CfLog.OUT.d(LOG_TAG, "Using default cache");
            return new GuavaCache();
        }
        return null;
    }
}
