package io.harness.cfsdk.cloud.cache;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.orhanobut.hawk.Hawk;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.harness.cfsdk.cloud.openapi.client.model.Evaluation;

public class DefaultCache implements CloudCache {

    public static final String METADATA_KEY_LAST_UPDATED = "LAST_UPDATED";
    private final Map<String, Evaluation> evaluations;
    private final InternalCache internalCache;
    private final String cacheId;

    public interface InternalCache {
        default void init(final Context appContext) { Hawk.init(appContext).build(); }
        default void saveAll(String cacheId, Map<String, Evaluation> evaluations) { Hawk.put(cacheId, evaluations); }
        default Map<String, Evaluation> loadAll(String cacheId, Map<String, Evaluation> defaultMap) { return Hawk.get(cacheId, defaultMap); }
        default void deleteAll() { Hawk.deleteAll(); }
        default void updateMetadata(String cacheId, Map<String, String> metadata) { Hawk.put(cacheId + "_METADATA", metadata); }
    }

    public DefaultCache(final Context appContext, String targetId, String apiKey) {
        this(appContext, new InternalCache() {}, targetId, apiKey);
    }

    public DefaultCache(final Context appContext, final InternalCache cache, String targetId, String apiKey) {
        internalCache = cache;
        internalCache.init(appContext);
        evaluations = load();
        cacheId = getCacheId(targetId, apiKey);
    }

    private String getCacheId(String targetId, String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(targetId.getBytes(UTF_8));
            digest.update(apiKey.getBytes(UTF_8));
            return "HARNESS_FF_CACHE_" + String.format("%032X", new BigInteger(1, digest.digest())) + getVersionSuffix();
        } catch (NoSuchAlgorithmException ex) {
            return "HARNESS_FF_CACHE_" + targetId + getVersionSuffix();
        }
    }

    private String getVersionSuffix() {
        return "_v" + ANDROID_SDK_VERSION.replace("-SNAPSHOT", "").replace(".", "");
    }

    @Override
    @Nullable
    public Evaluation getEvaluation(final String env, final String key) {
        return evaluations.get(makeKey(env, key));
    }

    @Override
    public void saveEvaluation(final String env, final String key, final Evaluation evaluation) {
        evaluations.put(makeKey(env, key), evaluation);
        internalCache.saveAll(cacheId, evaluations);
        updateMetadata(env);
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
        internalCache.saveAll(cacheId, evaluations);
        updateMetadata(env);
    }

    @Override
    public void removeEvaluation(final String env, final String key) {
        evaluations.remove(makeKey(env, key));
        internalCache.saveAll(cacheId, evaluations);
        updateMetadata(env);
    }

    @Override
    public void clear(String env) {
        evaluations.clear();
        internalCache.saveAll(cacheId, evaluations);
        updateMetadata(env);
    }

    private void updateMetadata(String env) {
        final Map<String, String> metadata = Collections.singletonMap(METADATA_KEY_LAST_UPDATED + '.' + env, Instant.now().toString());
        internalCache.updateMetadata(cacheId, metadata);
    }

    private String makeKey(String env, String key) {
        return env + '_' + key;
    }

    private ConcurrentHashMap<String, Evaluation> load() {
        final ConcurrentHashMap<String, Evaluation> defaultMap = new ConcurrentHashMap<>();
        try {
            return new ConcurrentHashMap<>(internalCache.loadAll(cacheId, defaultMap));
        } catch (Exception ex) {
            // Possible cache corruption, reset the cache on disk and let it rebuild
            internalCache.deleteAll();
            return new ConcurrentHashMap<>(internalCache.loadAll(cacheId, defaultMap));
        }
    }
}
