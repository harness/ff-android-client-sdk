package io.harness.cfsdk.cloud.cache;

import static java.nio.charset.StandardCharsets.UTF_8;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public class DefaultCache implements CloudCache {

    public static final String METADATA_KEY_LAST_UPDATED = "LAST_UPDATED";
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
