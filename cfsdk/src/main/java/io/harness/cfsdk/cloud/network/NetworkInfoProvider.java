package io.harness.cfsdk.cloud.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;

import java.util.HashSet;

public class NetworkInfoProvider extends NetworkInfoProviding {

    private final ConnectivityManager connectivityManager;

    public NetworkInfoProvider(Context context) {

        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        lastState = isNetworkAvailable();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    super.onCapabilitiesChanged(network, networkCapabilities);
                    boolean connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    if (connected == lastState) return;

                    lastState = connected;
                    for (NetworkListener networkListener : evaluationsObserver) {
                        networkListener.onChange((connected) ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    if (!lastState) return;
                    lastState = false;
                    for (NetworkListener networkListener : evaluationsObserver) {
                        networkListener.onChange(NetworkStatus.DISCONNECTED);
                    }

                }
            });

        } else {

            BroadcastReceiver networkBroadcast = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean lastConnected = lastState;
                    lastState = isNetworkAvailable();
                    if (lastConnected == lastState) return;
                    for (NetworkListener networkListener : evaluationsObserver) {
                        networkListener.onChange((lastState) ? NetworkStatus.CONNECTED : NetworkStatus.DISCONNECTED);
                    }
                }
            };
            context.registerReceiver(networkBroadcast, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }

    @Override
    public boolean isNetworkAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (connectivityManager != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                lastState = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                return lastState;
            }
        } else {
            if (connectivityManager != null) {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                lastState = networkInfo != null && networkInfo.isConnected();
                return lastState;
            }
        }
        return false;
    }
}