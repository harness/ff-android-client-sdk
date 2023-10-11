package io.harness.cfsdk.cloud.cache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public class DefaultCache implements CloudCache {

    private static final String KEY_ALL = "all_evaluations_v2";
    private final Map<String, Evaluation> evaluations;
    private final InternalCache internalCache;

    public interface InternalCache {
        default void init(final Context appContext) { Hawk.init(appContext).build(); }
        default void saveAll(String key, Map<String, Evaluation> evaluations) { Hawk.put(key, evaluations); }
        default Map<String, Evaluation> loadAll(String key, Map<String, Evaluation> defaultMap) { return Hawk.get(key, defaultMap); }
        default void deleteAll() { Hawk.deleteAll(); }
    }

    public DefaultCache(final Context appContext) {
        this(appContext, new InternalCache() {});
    }

    public DefaultCache(final Context appContext, final InternalCache cache) {
        internalCache = cache;
        internalCache.init(appContext);
        evaluations = load();
    }

    @Override
    @Nullable
    public Evaluation getEvaluation(final String env, final String key) {
        return evaluations.get(makeKey(env, key));
    }

    @Override
    public void saveEvaluation(final String env, final String key, final Evaluation evaluation) {
        evaluations.put(makeKey(env, key), evaluation);
        internalCache.saveAll(KEY_ALL, evaluations);
    }

    @Override
    @NonNull
    public List<Evaluation> getAllEvaluations(final String env) {
        return new ArrayList<>(evaluations.values());
    }

    @Override
    public void saveAllEvaluations(final String env, final List<Evaluation> newEvaluations) {
        for (final Evaluation eval : newEvaluations) {
            evaluations.put(makeKey(env, eval.getFlag()), eval);
        }
        internalCache.saveAll(KEY_ALL, evaluations);
    }

    @Override
    public void removeEvaluation(final String env, final String key) {
        evaluations.remove(makeKey(env, key));
        internalCache.saveAll(KEY_ALL, evaluations);
    }

    @Override
    public void clear() {
        evaluations.clear();
        internalCache.saveAll(KEY_ALL, evaluations);
    }

    private String makeKey(String env, String key) {
        return env + '_' + key;
    }

    private ConcurrentHashMap<String, Evaluation> load() {
        final ConcurrentHashMap<String, Evaluation> defaultMap = new ConcurrentHashMap<>();
        try {
            return new ConcurrentHashMap<>(internalCache.loadAll(KEY_ALL, defaultMap));
        } catch (Exception ex) {
            // Possible cache corruption, reset the cache on disk and let it rebuild
            internalCache.deleteAll();
            return new ConcurrentHashMap<>(internalCache.loadAll(KEY_ALL, defaultMap));
        }
    }
}
