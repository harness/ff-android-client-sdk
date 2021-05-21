package io.harness.cfsdk.cloud.analytics.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.ConcurrentMap;

import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.logging.CfLog;

/**
 * A custom class for implementing the interface methods of cache interface. It uses Caffein cache
 * as the cache service provider.
 */
public class CaffeineCache implements Cache {

    private final String logTag;
    private final com.github.benmanes.caffeine.cache.Cache<Analytics, Integer> cache;

    {

        logTag = CaffeineCache.class.getSimpleName();
        cache = Caffeine.newBuilder().maximumSize(10000).build();
    }

    @Override
    public @Nullable Integer get(Analytics result) {

        return cache.getIfPresent(result);
    }

    @Override
    public @NonNull ConcurrentMap<@NonNull Analytics, @NonNull Integer> getAll() {

        return cache.asMap();
    }

    @Override
    public void put(Analytics a, Integer i) {

        cache.put(a, i);
    }

    @Override
    public void resetCache() {

        cache.invalidateAll();
    }

    @Override
    public void printCache() {

        CfLog.OUT.i(logTag, toString());
    }

    @Override
    public String toString() {

        return cache.asMap().toString();
    }
}
