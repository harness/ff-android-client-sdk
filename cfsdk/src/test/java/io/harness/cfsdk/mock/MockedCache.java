package io.harness.cfsdk.mock;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.Evaluation;

public class MockedCache implements CloudCache {

    private final Executor executor;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Evaluation>> evaluations;

    public MockedCache() {

        executor = Executors.newSingleThreadExecutor();

        ConcurrentHashMap<String, ConcurrentHashMap<String, Evaluation>> evaluationsTemp;

        evaluations =  new ConcurrentHashMap<>();
    }

    @Override
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

        };

        executor.execute(action);
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

        final Runnable action = () -> {

            final ConcurrentHashMap<String, Evaluation> items = new ConcurrentHashMap<>();
            for (final Evaluation item : newEvaluations) {

                items.put(item.getIdentifier(), item);
            }

            evaluations.put(env, items);
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

        };

        executor.execute(action);
    }

    @Override
    public void clear() {

        final Runnable action = () -> {

            evaluations.clear();

        };

        executor.execute(action);
    }
}
