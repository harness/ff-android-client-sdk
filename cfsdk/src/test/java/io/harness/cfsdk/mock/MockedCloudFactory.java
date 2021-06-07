package io.harness.cfsdk.mock;

import android.content.Context;

import com.google.common.cache.Cache;

import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.model.FeatureConfig;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.sse.SSEControlling;

public class MockedCloudFactory extends CloudFactory {

    private SSEControlling controlling;

    @Override
    public ICloud cloud(String sseUrl, String baseUrl, String key, Target target) {

        return new MockedCloud();
    }

    @Override
    public NetworkInfoProviding networkInfoProvider(final Context context) {

        return new MockedNetworkInfoProvider();
    }

    @Override
    public SSEControlling sseController(

            ICloud cloud,
            AuthInfo authInfo,
            Cache<String, FeatureConfig> featureCache
    ) {

        if(controlling == null){

            controlling = new MockedSSEController(featureCache);
        }
        return controlling;
    }

    @Override
    public FeatureRepository getFeatureRepository(FeatureService featureService, CloudCache cloudCache) {

        return new MockedFeatureRepository();
    }
}
