package io.harness.cfsdk.mock;

import android.content.Context;

import io.harness.cfsdk.cloud.ICloud;
import io.harness.cfsdk.cloud.factories.CloudFactory;
import io.harness.cfsdk.cloud.model.Target;
import io.harness.cfsdk.cloud.network.NetworkInfoProviding;

public class MockedCloudFactory extends CloudFactory {

    @Override
    public ICloud cloud(String sseUrl, String baseUrl, String key, Target target) {

        return new MockedCloud();
    }

    @Override
    public NetworkInfoProviding networkInfoProvider(final Context context) {

        return new MockedNetworkInfoProvider();
    }
}
