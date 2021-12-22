package io.harness.cfsdk.cloud.cache;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.hawk.Hawk;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import io.harness.cfsdk.cloud.core.model.Evaluation;

public class DefaultCache implements CloudCache {

    private final String key_all;
    private final HashMap<String, HashMap<String, Evaluation>> evaluations;

    public DefaultCache(final Context appContext) {

        Hawk.init(appContext).build();

        key_all = "all_evaluations";
        evaluations = Hawk.get(key_all, new HashMap<>());
    }

    @Override
    @Nullable
    public Evaluation getEvaluation(final String env, final String key) {

        final HashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            return items.get(key);
        }
        return null;
    }

    @Override
    public void saveEvaluation(final String env, final String key, final Evaluation evaluation) {

        HashMap<String, Evaluation> items = evaluations.get(env);
        if (items == null) {

            items = new HashMap<>();
            evaluations.put(env, items);
        }
        items.put(key, evaluation);

        Hawk.put(key_all, evaluations);
    }

    @Override
    @NonNull
    public List<Evaluation> getAllEvaluations(final String env) {

        final HashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            return new LinkedList<>(items.values());
        }
        return new LinkedList<>();
    }

    @Override
    public void saveAllEvaluations(final String env, final List<Evaluation> newEvaluations) {

        final HashMap<String, Evaluation> items = new HashMap<>();
        for (final Evaluation item : newEvaluations) {

            items.put(item.getIdentifier(), item);
        }

        evaluations.put(env, items);

        Hawk.put(key_all, evaluations);
    }

    @Override
    public void removeEvaluation(final String env, final String key) {

        final HashMap<String, Evaluation> items = evaluations.get(env);
        if (items != null) {

            items.remove(key);
        }

        Hawk.put(key_all, evaluations);
    }

    @Override
    public void clear() {

        evaluations.clear();
        Hawk.put(key_all, evaluations);
    }
}
