package io.harness.cfsdk.cloud.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NetworkInfoProviding {

    protected volatile boolean lastState;
    protected final Set<NetworkListener> evaluationsObserver = ConcurrentHashMap.newKeySet();

    public abstract boolean isNetworkAvailable();

    public void register(NetworkListener networkListener) {
        this.evaluationsObserver.add(networkListener);
    }

    public void unregisterAll() {
        this.evaluationsObserver.clear();
    }
}
