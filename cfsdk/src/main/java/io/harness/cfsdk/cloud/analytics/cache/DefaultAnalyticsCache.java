package io.harness.cfsdk.cloud.analytics.cache;

import androidx.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.logging.CfLog;

public class DefaultAnalyticsCache implements AnalyticsCache {

    private final String logTag;
    private final ConcurrentHashMap<Analytics, Integer> cache;

    {

        cache = new ConcurrentHashMap<>();
        logTag = DefaultAnalyticsCache.class.getSimpleName();
    }

    @Nullable
    @Override
    public Integer get(Analytics a) {

        return cache.get(a);
    }

    @Override
    public Map<Analytics, Integer> getAll() {

        return cache;
    }

    @Override
    public void put(Analytics a, Integer i) {

        cache.put(a, i);
    }

    @Override
    public void resetCache() {

        cache.clear();
    }

    @Override
    public void printCache() {

        CfLog.OUT.v(logTag, "Cache: " + cache);
    }
}
