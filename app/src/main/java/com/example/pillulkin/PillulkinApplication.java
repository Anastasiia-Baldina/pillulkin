package com.example.pillulkin;

import android.app.Application;
import android.net.ConnectivityManager;

import com.example.pillulkin.sync.SyncManager;

public class PillulkinApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        registerNetworkCallback();
    }

    private void registerNetworkCallback() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm == null) return;

        cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(android.net.Network network) {
                new SyncManager(getApplicationContext()).syncAll();
            }
        });
    }
}
