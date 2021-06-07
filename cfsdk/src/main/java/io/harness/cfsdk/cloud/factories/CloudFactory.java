package io.harness.cfsdk.cloud.factories;

import android.content.Context;

import com.google.common.cache.Cache;

import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.TokenProvider;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.InMemoryCacheImpl;
import io.harness.cfsdk.cloud.cache.StorageCache;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProvider;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.polling.ShortTermPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.repository.FeatureRepositoryImpl;
import io.harness.cfsdk.cloud.sse.SSEController;
import io.harness.cfsdk.cloud.sse.SSEControlling;

public class CloudFactory implements ICloudFactory {

    private TokenProvider tokenProvider;

    @Override
    public AuthResponseDecoder getAuthResponseDecoder() {

        return new AuthResponseDecoder();
    }

    @Override
    public ICloud cloud(String sseUrl, String baseUrl, String key, Target target) {

        return new Cloud(this, sseUrl, baseUrl, key, target);
    }

    @Override
    public FeatureRepository getFeatureRepository(FeatureService featureService, CloudCache cloudCache) {

        return new FeatureRepositoryImpl(featureService, cloudCache);
    }

    @Override
    public SSEControlling sseController(

            ICloud cloud,
            AuthInfo authInfo,
            Cache<String, FeatureConfig> featureCache
    ) {

        return new SSEController(cloud, authInfo, featureCache);
    }

    @Override
    public EvaluationPolling evaluationPolling(int pollingInterval, TimeUnit timeUnit) {

        return new ShortTermPolling(pollingInterval, timeUnit);
    }

    @Override
    public CloudCache defaultCache(Context context) {

        return new InMemoryCacheImpl(new StorageCache(context));
    }

    @Override
    public NetworkInfoProviding networkInfoProvider(Context context) {

        return new NetworkInfoProvider(context);
    }

    @Override
    public ApiClient apiClient(){
        return new ApiClient();
    }

    @Override
    public DefaultApi defaultApi(ApiClient apiClient){
        return new DefaultApi(apiClient);
    }

    @Override
    public synchronized TokenProvider tokenProvider() {

        if (tokenProvider == null) {
            tokenProvider = new TokenProvider();
        }
        return tokenProvider;
    }
}
