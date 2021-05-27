package io.harness.cfsdk.cloud.factories;

import android.content.Context;

import com.google.common.cache.Cache;

import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.NetworkInfoProvider;
import io.harness.cfsdk.cloud.TokenProvider;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.InMemoryCacheImpl;
import io.harness.cfsdk.cloud.cache.StorageCache;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.polling.ShortTermPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.repository.FeatureRepositoryImpl;
import io.harness.cfsdk.cloud.sse.SSEController;

public class CloudFactory {

    private TokenProvider tokenProvider;

    public AuthResponseDecoder getAuthResponseDecoder() {
        return new AuthResponseDecoder();
    }

    public Cloud cloud(String sseUrl, String baseUrl, String key, Target target) {

        return new Cloud(this, sseUrl, baseUrl, key, target);
    }

    public FeatureRepository getFeatureRepository(FeatureService featureService, CloudCache cloudCache) {
        return new FeatureRepositoryImpl(featureService, cloudCache);
    }

    public SSEController sseController(

            Cloud cloud,
            AuthInfo authInfo,
            Cache<String, FeatureConfig> featureCache
    ) {

        return new SSEController(cloud, authInfo, featureCache);
    }

    public EvaluationPolling evaluationPolling(int pollingInterval, TimeUnit timeUnit) {
        return new ShortTermPolling(pollingInterval, timeUnit);
    }

    public CloudCache defaultCache(Context context) {
        return new InMemoryCacheImpl(new StorageCache(context));
    }

    public NetworkInfoProvider networkInfoProvider(Context context) {
        return new NetworkInfoProvider(context);
    }

    public ApiClient apiClient(){
        return new ApiClient();
    }

    public DefaultApi defaultApi(ApiClient apiClient){
        return new DefaultApi(apiClient);
    }

    public synchronized TokenProvider tokenProvider() {

        if (tokenProvider == null) {
            tokenProvider = new TokenProvider();
        }
        return tokenProvider;
    }
}
