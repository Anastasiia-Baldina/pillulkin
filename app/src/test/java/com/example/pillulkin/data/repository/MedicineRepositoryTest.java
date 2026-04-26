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
public class MedicineRepositoryTest {

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    private MedicineRepository repository;

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

    private void setupNotAuthenticated() {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);
    }

    @Test
    public void testInitialState() {
        repository = new MedicineRepository(application);
        assertNull(repository.getPatientMedicines().getValue());
        assertNull(repository.getSearchResults().getValue());
        assertFalse(repository.isLoading().getValue());
    }

    @Test
    public void testLoadNotAuthenticated() {
        setupNotAuthenticated();
        repository = new MedicineRepository(application);
        repository.loadPatientMedicines();
        ShadowLooper.idleMainLooper();
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testAddNotAuthenticated() {
        setupNotAuthenticated();
        repository = new MedicineRepository(application);
        repository.addPatientMedicine(1L);
        ShadowLooper.idleMainLooper();
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testDeleteNotAuthenticated() {
        setupNotAuthenticated();
        repository = new MedicineRepository(application);
        repository.deletePatientMedicine(1L);
        ShadowLooper.idleMainLooper();
        assertEquals("Not authenticated", repository.getError().getValue());
    }
}
