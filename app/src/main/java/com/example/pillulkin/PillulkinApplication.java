package com.example.pillulkin;

import android.app.Application;

import com.example.pillulkin.data.local.PillulkinDatabase;

public class PillulkinApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PillulkinDatabase.getDatabase(this);
    }
}
