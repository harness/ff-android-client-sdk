package io.harness.cfsdk.cloud.analytics.cache;

import androidx.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.analytics.model.Analytics;
import io.harness.cfsdk.logging.CfLog;

/**
 * A custom class for implementing the interface methods of cache interface. It uses Guava cache as
 * the cache service provider.
 */
public class GuavaCache implements Cache {

    private final String logTag;
    private final LoadingCache<Analytics, Integer> cache;

    {

        cache =
                CacheBuilder.newBuilder()
                        .maximumSize(10000)
                        .expireAfterAccess(60, TimeUnit.SECONDS)
                        .build(
                                new CacheLoader<Analytics, Integer>() {
                                    @Override
                                    public Integer load(@NotNull Analytics analytics) {
                                        return 0;
                                    }
                                }
                        );

        logTag = GuavaCache.class.getSimpleName();
    }

    @Override
    public @Nullable
    Integer get(Analytics a) {

        try {

            return cache.get(a);
        } catch (ExecutionException e) {

            CfLog.OUT.e(logTag, e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Map<Analytics, Integer> getAll() {

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
