package io.harness.cfsdk.cloud.analytics;

import androidx.annotation.Nullable;

import java.util.Map;

import io.harness.cfsdk.cloud.analytics.model.Analytics;

/**
 * An interface for different cache providers for our analytics service.
 */
public interface Cache {

    @Nullable
    Integer get(Analytics a);

    Map<Analytics, Integer> getAll();

    void put(Analytics a, Integer i);

    void resetCache();

    void printCache();
}
