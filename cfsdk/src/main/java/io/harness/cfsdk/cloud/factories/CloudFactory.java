package io.harness.cfsdk.cloud.factories;

import android.content.Context;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.TokenProvider;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.DefaultCache;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
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
import io.harness.cfsdk.logging.CfLog;

public class CloudFactory implements ICloudFactory {

    private final String logTag;
    private TokenProvider tokenProvider;

    {

        logTag = CloudFactory.class.getSimpleName();
    }

    @Override
    public AuthResponseDecoder getAuthResponseDecoder() {

        return new AuthResponseDecoder();
    }

    @Override
    public ICloud cloud(String sseUrl, String baseUrl, String key, Target target) {

        return new Cloud(this, sseUrl, baseUrl, key, target);
    }

    @Override
    public FeatureRepository getFeatureRepository(

            FeatureService featureService,
            CloudCache cloudCache,
            NetworkInfoProviding networkInfoProvider
    ) {

        return new FeatureRepositoryImpl(featureService, cloudCache, networkInfoProvider);
    }

    @Override
    public SSEControlling sseController(

            ICloud cloud,
            AuthInfo authInfo
    ) {

        return new SSEController(authInfo);
    }

    @Override
    public EvaluationPolling evaluationPolling(int pollingInterval, TimeUnit timeUnit) {

        return new ShortTermPolling(pollingInterval, timeUnit);
    }

    @Override
    public CloudCache defaultCache(Context context) {

        return new DefaultCache(context);
    }

    @Override
    public NetworkInfoProviding networkInfoProvider(Context context) {

        return new NetworkInfoProvider(context);
    }

    @Override
    public ApiClient apiClient() {

        final ApiClient apiClient = new ApiClient();
        apiClient.setUserAgent("android 1.0.10");
        String hostname = "UnknownHost";
        try {

            hostname = InetAddress.getLocalHost().getHostName();
            CfLog.OUT.v(logTag, "Hostname: " + hostname);
        } catch (UnknownHostException e) {

            CfLog.OUT.w(logTag, "Unable to get hostname");
        }
        apiClient.addDefaultHeader("Hostname", hostname);
        return apiClient;
    }

    @Override
    public DefaultApi defaultApi(ApiClient apiClient) {

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
