package io.harness.cfsdk.cloud.factories;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.harness.cfsdk.cloud.AuthResponseDecoder;
import io.harness.cfsdk.cloud.FeatureService;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.TokenProvider;
import io.harness.cfsdk.cloud.cache.CloudCache;
import io.harness.cfsdk.cloud.core.api.DefaultApi;
import io.harness.cfsdk.cloud.core.client.ApiClient;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.polling.EvaluationPolling;
import io.harness.cfsdk.cloud.repository.FeatureRepository;
import io.harness.cfsdk.cloud.sse.SSEControlling;

public interface ICloudFactory {

    AuthResponseDecoder getAuthResponseDecoder();

    ICloud cloud(String sseUrl, String baseUrl, String key, Target target);

    FeatureRepository getFeatureRepository(FeatureService featureService, CloudCache cloudCache);

    SSEControlling sseController(

            ICloud cloud,
            AuthInfo authInfo
    );

    EvaluationPolling evaluationPolling(int pollingInterval, TimeUnit timeUnit);

    CloudCache defaultCache(Context context);

    NetworkInfoProviding networkInfoProvider(Context context);

    ApiClient apiClient();

    DefaultApi defaultApi(ApiClient apiClient);

    TokenProvider tokenProvider();
}
