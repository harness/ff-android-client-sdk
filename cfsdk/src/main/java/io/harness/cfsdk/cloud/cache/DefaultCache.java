package io.harness.cfsdk.cloud.cache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public class DefaultCache implements CloudCache {

    private final Map<String, Evaluation> evaluations;


    public DefaultCache() {
        evaluations = new ConcurrentHashMap<>();
    }

    @Override
    @Nullable
    public Evaluation getEvaluation(final String env, final String key) {
        return evaluations.get(makeKey(env, key));
    }

    @Override
    public void saveEvaluation(final String env, final String key, final Evaluation evaluation) {
        evaluations.put(makeKey(env, key), evaluation);
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
    }

    @Override
    public void removeEvaluation(final String env, final String key) {
        evaluations.remove(makeKey(env, key));
    }

    @Override
    public void clear(String env) {
        evaluations.clear();
    }


    private String makeKey(String env, String key) {
        return env + '_' + key;
    }


}
