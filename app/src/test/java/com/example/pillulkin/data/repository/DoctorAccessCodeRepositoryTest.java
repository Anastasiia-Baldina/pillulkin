package com.example.pillulkin.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.remote.NetworkModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class DoctorAccessCodeRepositoryTest {

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    private DoctorAccessCodeRepository repository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        resetNetworkModule();
        when(application.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(prefs.edit()).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
    }

    @After
    public void tearDown() throws Exception { resetNetworkModule(); }

    private void resetNetworkModule() throws Exception {
        Field f = NetworkModule.class.getDeclaredField("instance"); f.setAccessible(true); f.set(null, null);
    }

    @Test
    public void testInitialState() {
        repository = new DoctorAccessCodeRepository(application);
        assertNull(repository.getGeneratedCode().getValue());
        assertNull(repository.getPatientData().getValue());
        assertFalse(repository.isLoading().getValue());
    }

    @Test
    public void testLoadPatientDataNotAuthenticated() {
        repository = new DoctorAccessCodeRepository(application);
        repository.loadPatientFullData();
        ShadowLooper.idleMainLooper();
        assertEquals("Not authenticated as doctor", repository.getError().getValue());
    }

    @Test
    public void testLogout() {
        repository = new DoctorAccessCodeRepository(application);
        repository.logout();
        verify(editor).remove("doctor_token");
        verify(editor).remove("doctor_patient_id");
    }
}
