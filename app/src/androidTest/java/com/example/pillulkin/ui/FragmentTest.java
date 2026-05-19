package com.example.pillulkin.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.remote.NetworkModule;

import org.junit.After;
import org.junit.Before;

import java.lang.reflect.Field;

import androidx.test.core.app.ApplicationProvider;

public abstract class FragmentTest {

    protected Context context;
    protected SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Before
    public void baseSetUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences("pillulkin_prefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.clear();
        editor.putBoolean("local_mode", false);
        editor.putLong("patient_id", 1L);
        editor.putString("patient_token", "test_token");
        editor.apply();
        resetNetworkModule();
        resetDatabase();
    }

    @After
    public void baseTearDown() throws Exception {
        resetNetworkModule();
        resetDatabase();
        editor.clear().apply();
    }

    protected void resetNetworkModule() throws Exception {
        Field f = NetworkModule.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    protected void resetDatabase() throws Exception {
        Field f = PillulkinDatabase.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    protected void setLocalMode(boolean local) {
        editor.putBoolean("local_mode", local);
        if (local) editor.remove("patient_token");
        editor.apply();
    }

    protected void setDoctorSession(boolean active) {
        editor.putBoolean("local_doctor_session", active);
        editor.putString("doctor_token", active ? "doctor_token" : null);
        editor.putLong("doctor_patient_id", active ? 1L : -1L);
        editor.putLong("doctor_expires", active ? System.currentTimeMillis() + 3600000L : 0L);
        editor.putString("local_doctor_code", active ? "123456" : null);
        editor.apply();
    }
}
