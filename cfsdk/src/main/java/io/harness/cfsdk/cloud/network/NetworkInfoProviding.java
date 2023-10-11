package io.harness.cfsdk.cloud.network;

import java.util.HashSet;

public abstract class NetworkInfoProviding {

    protected volatile boolean lastState;
    protected final HashSet<NetworkListener> evaluationsObserver = new HashSet<>();

    public abstract boolean isNetworkAvailable();

    public void register(NetworkListener networkListener) {
        this.evaluationsObserver.add(networkListener);
    }

    public void unregisterAll() {
        this.evaluationsObserver.clear();
    }
}
