package com.example.pillulkin.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.CachedMedicineDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MedicineRepositoryTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private Call<List<PatientMedicineResponse>> medicinesCall;
    @Mock private Call<PatientMedicineResponse> addMedicineCall;
    @Mock private PillulkinDatabase db;
    @Mock private CachedMedicineDao cachedMedicineDao;
    @Mock private PendingOperationDao pendingOperationDao;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private MedicineRepository repository;
    private AutoCloseable mocks;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        resetNetworkModule();
        when(application.getApplicationContext()).thenReturn(context);
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(prefs.edit()).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);

        when(db.cachedMedicineDao()).thenReturn(cachedMedicineDao);
        when(db.pendingOperationDao()).thenReturn(pendingOperationDao);
        when(cachedMedicineDao.getMedicines(anyLong())).thenReturn(Collections.emptyList());

        dbStatic = mockStatic(PillulkinDatabase.class);
        dbStatic.when(() -> PillulkinDatabase.getDatabase(any(Context.class))).thenReturn(db);
    }

    @After
    public void tearDown() throws Exception {
        dbStatic.close();
        resetNetworkModule();
        mocks.close();
    }

    private void resetNetworkModule() throws Exception {
        Field f = NetworkModule.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    private void setupNotAuthenticated() {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);
    }

    private void setupAuthenticated() throws Exception {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);

        when(api.getMedicines(eq(1L))).thenReturn(medicinesCall);
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
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testAddNotAuthenticated() {
        setupNotAuthenticated();
        repository = new MedicineRepository(application);
        repository.addPatientMedicine(1L, "2026-12-01", "10");
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testDeleteNotAuthenticated() {
        setupNotAuthenticated();
        repository = new MedicineRepository(application);
        repository.deletePatientMedicine(1L);
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testLoadPatientMedicinesSuccess() throws Exception {
        setupAuthenticated();

        PatientMedicineResponse med = new PatientMedicineResponse();
        med.setId(1L);
        med.setMedicineName("Aspirin");
        med.setDosage("500mg");

        List<PatientMedicineResponse> meds = new ArrayList<>();
        meds.add(med);

        doAnswer(invocation -> {
            Callback<List<PatientMedicineResponse>> callback = invocation.getArgument(0);
            callback.onResponse(medicinesCall, Response.success(meds));
            return null;
        }).when(medicinesCall).enqueue(any());

        repository = new MedicineRepository(application);
        repository.loadPatientMedicines();

        assertNotNull(repository.getPatientMedicines().getValue());
        assertEquals(1, repository.getPatientMedicines().getValue().size());
        assertEquals("Aspirin", repository.getPatientMedicines().getValue().get(0).getMedicineName());
    }

    @Test
    public void testLoadPatientMedicinesServerError() throws Exception {
        setupAuthenticated();

        doAnswer(invocation -> {
            Callback<List<PatientMedicineResponse>> callback = invocation.getArgument(0);
            callback.onResponse(medicinesCall, Response.error(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(medicinesCall).enqueue(any());

        repository = new MedicineRepository(application);
        repository.loadPatientMedicines();

        assertTrue(repository.getError().getValue().contains("500"));
    }

    @Test
    public void testLoadPatientMedicinesNetworkFailure() throws Exception {
        setupAuthenticated();

        doAnswer(invocation -> {
            Callback<List<PatientMedicineResponse>> callback = invocation.getArgument(0);
            callback.onFailure(medicinesCall, new Exception("Network error"));
            return null;
        }).when(medicinesCall).enqueue(any());

        repository = new MedicineRepository(application);
        repository.loadPatientMedicines();

        assertTrue(repository.getError().getValue().contains("Network error"));
    }

    @Test
    public void testUpdateNotAuthenticated() {
        setupNotAuthenticated();
        repository = new MedicineRepository(application);
        repository.updatePatientMedicine(1L, "2026-12-01", "10");
        assertEquals("Not authenticated", repository.getError().getValue());
    }
}
