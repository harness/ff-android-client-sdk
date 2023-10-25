package io.harness.cfsdk.cloud.factories;

import static java.util.concurrent.ThreadLocalRandom.current;
import static io.harness.cfsdk.AndroidSdkVersion.ANDROID_SDK_VERSION;

import android.content.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.Cloud;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.TokenProvider;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.cache.DefaultCache;
import io.harness.cfsdk.cloud.network.NewRetryInterceptor;
import io.harness.cfsdk.cloud.openapi.client.api.ClientApi;
import io.harness.cfsdk.cloud.openapi.client.ApiClient;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProvider;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.polling.ShortTermPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.repository.FeatureRepositoryImpl;
import okhttp3.OkHttpClient;

public class CloudFactory implements ICloudFactory {

    private static final Logger log = LoggerFactory.getLogger(CloudFactory.class);

    private TokenProvider tokenProvider;

    @Override
    public AuthResponseDecoder getAuthResponseDecoder() {

        return new AuthResponseDecoder();
    }

    @Override
    public ICloud cloud(String sseUrl, String baseUrl, String key, Target target, CfConfiguration config) {

        return new Cloud(this, sseUrl, baseUrl, key, target, config);
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

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(new NewRetryInterceptor(current().nextInt(2000, 5000)));

        final ApiClient apiClient = new ApiClient(builder.build());
        apiClient.setUserAgent("android " + ANDROID_SDK_VERSION);
        apiClient.addDefaultHeader("Hostname", getHostname());
        apiClient.addDefaultHeader("Harness-SDK-Info", "Android " + ANDROID_SDK_VERSION + " Client");

        return apiClient;
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.warn("Unable to get hostname", e);
            return "UnknownHost";
        }
    }

    @Override
    public ClientApi defaultApi(ApiClient apiClient) {
        return new ClientApi(apiClient);
    }

    @Override
    public synchronized TokenProvider tokenProvider() {

        if (tokenProvider == null) {
            tokenProvider = new TokenProvider();
        }
        return tokenProvider;
    }
}
