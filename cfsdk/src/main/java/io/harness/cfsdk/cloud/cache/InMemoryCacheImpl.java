package io.harness.cfsdk.cloud.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public class InMemoryCacheImpl implements CloudCache{

    private final HashMap<String, Evaluation> inMemoryCache;
    private final CloudCache backedCache;

    public InMemoryCacheImpl(CloudCache backedCache) {
        this.backedCache = backedCache;
        inMemoryCache = new HashMap<>();

    }

    @Override
    public Evaluation getEvaluation(String key) {
        try {
            if (!inMemoryCache.containsKey(key)) {
                inMemoryCache.put(key, backedCache.getEvaluation(key));
            }
            return inMemoryCache.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void saveEvaluation(String key, Evaluation evaluation) {
        inMemoryCache.put(key, evaluation);
        backedCache.saveEvaluation(key, evaluation);
    }

    @Override
    public List<Evaluation> getAllEvaluations(String key) {
        return new LinkedList<>(inMemoryCache.values());
    }

    @Override
    public void removeEvaluation(String key) {
        this.inMemoryCache.remove(key);
        this.backedCache.removeEvaluation(key);
    }

    @Override
    public void clear() {
        inMemoryCache.clear();
    }
}
