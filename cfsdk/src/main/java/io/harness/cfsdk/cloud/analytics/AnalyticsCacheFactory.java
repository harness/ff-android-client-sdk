package io.harness.cfsdk.cloud.analytics;

import io.harness.cfsdk.cloud.analytics.cache.Cache;
import io.harness.cfsdk.cloud.analytics.cache.GuavaCache;
import io.harness.cfsdk.logging.CfLog;

public class AnalyticsCacheFactory {

    public static final String GUAVA_CACHE;

    private static final String LOG_TAG;

    static {

        GUAVA_CACHE = "guavaCache";
        LOG_TAG = AnalyticsCacheFactory.class.getSimpleName();
    }

    public static Cache create(String cacheName) {

        if (cacheName.equals(GUAVA_CACHE)) {

            CfLog.OUT.i(LOG_TAG, "Using Guava cache");
            return new GuavaCache();
        }
        return null;
    }
}
