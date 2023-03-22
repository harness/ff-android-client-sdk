package io.harness.cfsdk.mock;

import android.content.Context;

import io.harness.cfsdk.CfConfiguration;
import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.AuthInfo;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;
import io.harness.cfsdk.cloud.sse.SSEControlling;

public class MockedCloudFactory extends CloudFactory {

    private SSEControlling controlling;

    @Override
    public ICloud cloud(String sseUrl, String baseUrl, String key, Target target, CfConfiguration config) {

        return new MockedCloud();
    }

    @Override
    public NetworkInfoProviding networkInfoProvider(final Context context) {

        return MockedNetworkInfoProvider.create();
    }

    @Override
    public SSEControlling sseController(

            ICloud cloud,
            AuthInfo authInfo
    ) {

        if (controlling == null) {

            controlling = new MockedSSEController();
        }
        return controlling;
    }
}
