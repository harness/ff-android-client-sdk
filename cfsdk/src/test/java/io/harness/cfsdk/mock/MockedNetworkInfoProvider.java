package io.harness.cfsdk.mock;

import io.harness.cfsdk.cloud.network.NetworkInfoProviding;

public class MockedNetworkInfoProvider extends NetworkInfoProviding {

    public MockedNetworkInfoProvider() {
    }

    @Override
    public boolean isNetworkAvailable() {

        return true;
    }
}
