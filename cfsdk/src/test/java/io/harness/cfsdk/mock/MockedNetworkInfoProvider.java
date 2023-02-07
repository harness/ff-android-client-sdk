package io.harness.cfsdk.mock;

import io.harness.cfsdk.cloud.network.NetworkInfoProviding;

public class MockedNetworkInfoProvider extends NetworkInfoProviding {

    private final boolean isNetworkAvailable;

    private MockedNetworkInfoProvider(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
    }

    public static NetworkInfoProviding create() {
        return new MockedNetworkInfoProvider(true);
    }

    public static NetworkInfoProviding createWithNetworkOff() {
        return new MockedNetworkInfoProvider(false);
    }

    @Override
    public boolean isNetworkAvailable() {

        return isNetworkAvailable;
    }
}
