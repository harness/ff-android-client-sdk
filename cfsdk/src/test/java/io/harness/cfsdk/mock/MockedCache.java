package io.harness.cfsdk.mock;

import com.google.common.util.concurrent.AtomicLongMap;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public class MockedCache implements CloudCache {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Evaluation>> evaluations;
    private final AtomicLongMap<String> cacheHitsFreqMap = AtomicLongMap.create();
    private final AtomicLongMap<String> cacheSavedFreqMap = AtomicLongMap.create();

    public MockedCache() {
        evaluations =  new ConcurrentHashMap<>();
    }

    @Override
    public Evaluation getEvaluation(final String env, final String key) {

        final ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {
            Evaluation eval = items.get(key);
            if (eval != null) {
                cacheHitsFreqMap.incrementAndGet(key);
            }
            return eval;
        }
        return null;
    }

    @Override
    public void saveEvaluation(final String env, final String key, final Evaluation evaluation) {

        cacheSavedFreqMap.incrementAndGet(key);

        ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
        if (items == null) {

            items = new ConcurrentHashMap<>();
            evaluations.put(env, items);
        }
        items.put(key, evaluation);
    }

    @Override
    public List<Evaluation> getAllEvaluations(final String env) {

        final ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            return new LinkedList<>(items.values());
        }
        return new LinkedList<>();
    }

    @Override
    public void saveAllEvaluations(final String env, final List<Evaluation> newEvaluations) {

        final ConcurrentHashMap<String, Evaluation> items = new ConcurrentHashMap<>();
        for (final Evaluation item : newEvaluations) {

            items.put(item.getIdentifier(), item);
        }

        evaluations.put(env, items);
    }

    @Override
    public void removeEvaluation(final String env, final String key) {

        final ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            items.remove(key);
        }
    }

    @Override
    public void clear() {
        evaluations.clear();
    }

    public int getCacheHitCountForEvaluation(String id) {
        return (int) cacheHitsFreqMap.get(id);
    }

    public int getCacheSavedCountForEvaluation(String id) {
        return (int) cacheSavedFreqMap.get(id);
    }
}
