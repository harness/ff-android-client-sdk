package io.harness.cfsdk.cloud.cache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.hawk.Hawk;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public class DefaultCache implements CloudCache {

    private final String key_all;
    private final Executor executor;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Evaluation>> evaluations;

    public DefaultCache(final Context appContext) {

        Hawk.init(appContext).build();

        key_all = "all_evaluations";
        executor = Executors.newSingleThreadExecutor();

        ConcurrentHashMap<String, ConcurrentHashMap<String, Evaluation>> evaluationsTemp;

        try {
            evaluationsTemp = new ConcurrentHashMap<>(Hawk.get(key_all, new ConcurrentHashMap<>()));
        } catch (Exception ex) {
            // Possible cache corruption, reset the cache on disk and let it rebuild
            Hawk.deleteAll();
            evaluationsTemp = new ConcurrentHashMap<>(Hawk.get(key_all, new ConcurrentHashMap<>()));
        }

        evaluations = evaluationsTemp;
    }

    @Override
    @Nullable
    public Evaluation getEvaluation(final String env, final String key) {

        final ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            return items.get(key);
        }
        return null;
    }

    @Override
    public void saveEvaluation(final String env, final String key, final Evaluation evaluation) {

        final Runnable action = () -> {

            ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
            if (items == null) {

                items = new ConcurrentHashMap<>();
                evaluations.put(env, items);
            }
            items.put(key, evaluation);

            Hawk.put(key_all, evaluations);
        };

        executor.execute(action);
    }

    @Override
    @NonNull
    public List<Evaluation> getAllEvaluations(final String env) {

        final ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            return new LinkedList<>(items.values());
        }
        return new LinkedList<>();
    }

    @Override
    public void saveAllEvaluations(final String env, final List<Evaluation> newEvaluations) {

        final Runnable action = () -> {

            final ConcurrentHashMap<String, Evaluation> items = new ConcurrentHashMap<>();
            for (final Evaluation item : newEvaluations) {

                items.put(item.getIdentifier(), item);
            }

            evaluations.put(env, items);

            Hawk.put(key_all, evaluations);
        };

        executor.execute(action);
    }

    @Override
    public void removeEvaluation(final String env, final String key) {

        final Runnable action = () -> {

            final ConcurrentHashMap<String, Evaluation> items = evaluations.get(env);
            if (items != null) {

                items.remove(key);
            }

            Hawk.put(key_all, evaluations);
        };

        executor.execute(action);
    }

    @Override
    public void clear() {

        final Runnable action = () -> {

            evaluations.clear();
            Hawk.put(key_all, evaluations);
        };

        executor.execute(action);
    }
}
