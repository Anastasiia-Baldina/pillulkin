package com.example.pillulkin.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.CachedMedicineDao;
import com.example.pillulkin.data.local.dao.CachedSymptomDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.local.entity.CachedMedicine;
import com.example.pillulkin.data.local.entity.CachedSymptom;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientMedicineRequest;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomRequest;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class RepositoryCallbackTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    Application application;
    @Mock
    Context context;
    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    PillulkinApi api;
    @Mock
    PillulkinDatabase db;
    @Mock
    CachedMedicineDao cachedMedicineDao;
    @Mock
    CachedSymptomDao cachedSymptomDao;
    @Mock
    PendingOperationDao pendingOperationDao;
    @Mock
    ConnectivityManager connectivityManager;
    @Mock
    NetworkInfo networkInfo;

    @Mock
    Call<List<PatientMedicineResponse>> medicinesCall;
    @Mock
    Call<PatientMedicineResponse> addMedicineCall;
    @Mock
    Call<Void> deleteMedicineCall;
    @Mock
    Call<PatientMedicineResponse> updateMedicineCall;
    @Mock
    Call<List<ReferenceMedicineResponse>> searchCall;
    @Mock
    Call<List<ReferenceMedicineResponse>> recommendationsCall;

    @Mock
    Call<List<PatientSymptomResponse>> symptomsCall;
    @Mock
    Call<PatientSymptomResponse> addSymptomCall;
    @Mock
    Call<Void> deleteSymptomCall;
    @Mock
    Call<PatientSymptomResponse> renewSymptomCall;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private AutoCloseable mocks;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        resetNetworkModule();
        replaceExecutorWithSynchronous();

        when(application.getApplicationContext()).thenReturn(context);
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        when(prefs.edit()).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);

        when(db.cachedMedicineDao()).thenReturn(cachedMedicineDao);
        when(db.cachedSymptomDao()).thenReturn(cachedSymptomDao);
        when(db.pendingOperationDao()).thenReturn(pendingOperationDao);
        when(cachedMedicineDao.getMedicines(anyLong())).thenReturn(Collections.emptyList());
        when(cachedSymptomDao.getSymptoms(anyLong())).thenReturn(Collections.emptyList());

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

    private void replaceExecutorWithSynchronous() throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field uf = unsafeClass.getDeclaredField("theUnsafe");
        uf.setAccessible(true);
        Object unsafe = uf.get(null);
        Field ef = PillulkinDatabase.class.getDeclaredField("databaseWriteExecutor");
        long offset = (long) unsafeClass.getMethod("staticFieldOffset", Field.class).invoke(unsafe, ef);
        ExecutorService syncExecutor = new AbstractExecutorService() {
            public void execute(Runnable r) { r.run(); }
            public void shutdown() {}
            public List<Runnable> shutdownNow() { return Collections.emptyList(); }
            public boolean isShutdown() { return false; }
            public boolean isTerminated() { return false; }
            public boolean awaitTermination(long t, TimeUnit u) { return true; }
        };
        unsafeClass.getMethod("putObject", Object.class, long.class, Object.class)
                .invoke(unsafe, PillulkinDatabase.class, offset, syncExecutor);
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
        when(api.getSymptoms(eq(1L))).thenReturn(symptomsCall);
    }

    private void setupLocalMode() throws Exception {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);
    }

    private void setOffline() {
        when(networkInfo.isConnectedOrConnecting()).thenReturn(false);
    }

    // ==================== MedicineRepository ====================

    @Test
    public void testLoadPatientMedicinesLocalMode() throws Exception {
        setupLocalMode();

        CachedMedicine cached = new CachedMedicine();
        cached.setId(1L);
        cached.setPatientId(-1L);
        cached.setMedicineName("Aspirin");
        when(cachedMedicineDao.getMedicines(eq(-1L))).thenReturn(Collections.singletonList(cached));

        MedicineRepository repo = new MedicineRepository(application);
        repo.loadPatientMedicines();

        assertNotNull(repo.getPatientMedicines().getValue());
        assertEquals(1, repo.getPatientMedicines().getValue().size());
        assertEquals("Aspirin", repo.getPatientMedicines().getValue().get(0).getMedicineName());
        assertNull(repo.getError().getValue());
        verify(api, never()).getMedicines(anyLong());
    }

    @Test
    public void testLoadPatientMedicinesApiSuccessSortsAndSaves() throws Exception {
        setupAuthenticated();

        PatientMedicineResponse med1 = new PatientMedicineResponse();
        med1.setId(1L);
        med1.setMedicineName("Ibuprofen");
        PatientMedicineResponse med2 = new PatientMedicineResponse();
        med2.setId(2L);
        med2.setMedicineName("Aspirin");

        List<PatientMedicineResponse> meds = new ArrayList<>();
        meds.add(med1);
        meds.add(med2);

        doAnswer(inv -> {
            Callback<List<PatientMedicineResponse>> cb = inv.getArgument(0);
            cb.onResponse(medicinesCall, Response.success(meds));
            return null;
        }).when(medicinesCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.loadPatientMedicines();

        assertNotNull(repo.getPatientMedicines().getValue());
        assertEquals(2, repo.getPatientMedicines().getValue().size());
        assertEquals("Aspirin", repo.getPatientMedicines().getValue().get(0).getMedicineName());
        assertEquals("Ibuprofen", repo.getPatientMedicines().getValue().get(1).getMedicineName());
        verify(cachedMedicineDao).deleteByPatientId(1L);
        verify(cachedMedicineDao).insertAll(anyList());
    }

    @Test
    public void testLoadPatientMedicinesApiFailureWithExistingData() throws Exception {
        setupAuthenticated();

        CachedMedicine cached = new CachedMedicine();
        cached.setId(1L);
        cached.setPatientId(1L);
        cached.setMedicineName("Aspirin");
        when(cachedMedicineDao.getMedicines(eq(1L))).thenReturn(Collections.singletonList(cached));

        doAnswer(inv -> {
            Callback<List<PatientMedicineResponse>> cb = inv.getArgument(0);
            cb.onFailure(medicinesCall, new Exception("timeout"));
            return null;
        }).when(medicinesCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.loadPatientMedicines();

        assertNotNull(repo.getPatientMedicines().getValue());
        assertEquals(1, repo.getPatientMedicines().getValue().size());
        assertNull(repo.getError().getValue());
    }

    @Test
    public void testAddPatientMedicineLocalMode() throws Exception {
        setupLocalMode();

        ReferenceMedicineResponse ref = new ReferenceMedicineResponse();
        ref.setId(5L);
        ref.setName("Ibuprofen");
        ref.setDosage("200mg");
        ref.setForm("tablet");

        when(api.getReferenceMedicines(anyString())).thenReturn(searchCall);
        when(searchCall.execute()).thenReturn(Response.success(Collections.singletonList(ref)));

        List<CachedMedicine> existing = new ArrayList<>();
        when(cachedMedicineDao.getMedicines(eq(-1L))).thenReturn(existing);

        MedicineRepository repo = new MedicineRepository(application);
        repo.addPatientMedicine(5L, "2027-01-01", "30");

        verify(cachedMedicineDao).insertAll(anyList());
        assertNotNull(repo.getPatientMedicines().getValue());
        assertEquals(1, repo.getPatientMedicines().getValue().size());
        assertEquals("Ibuprofen", repo.getPatientMedicines().getValue().get(0).getMedicineName());
    }

    @Test
    public void testAddPatientMedicineOnlineSuccess() throws Exception {
        setupAuthenticated();

        when(api.addMedicine(eq(1L), any(PatientMedicineRequest.class))).thenReturn(addMedicineCall);

        doAnswer(inv -> {
            Callback<PatientMedicineResponse> cb = inv.getArgument(0);
            cb.onResponse(addMedicineCall, Response.success(new PatientMedicineResponse()));
            return null;
        }).when(addMedicineCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.addPatientMedicine(5L, "2027-01-01", "30");

        verify(api).addMedicine(eq(1L), any(PatientMedicineRequest.class));
        assertNull(repo.getError().getValue());
    }

    @Test
    public void testAddPatientMedicineOnlineApiFailure() throws Exception {
        setupAuthenticated();

        when(api.addMedicine(eq(1L), any(PatientMedicineRequest.class))).thenReturn(addMedicineCall);

        doAnswer(inv -> {
            Callback<PatientMedicineResponse> cb = inv.getArgument(0);
            cb.onFailure(addMedicineCall, new Exception("fail"));
            return null;
        }).when(addMedicineCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.addPatientMedicine(5L, "2027-01-01", "30");

        verify(pendingOperationDao).insert(any());
    }

    @Test
    public void testAddPatientMedicineOffline() throws Exception {
        setupAuthenticated();
        setOffline();

        when(api.addMedicine(eq(1L), any(PatientMedicineRequest.class))).thenReturn(addMedicineCall);

        MedicineRepository repo = new MedicineRepository(application);
        repo.addPatientMedicine(5L, "2027-01-01", "30");

        verify(pendingOperationDao).insert(any());
        verify(addMedicineCall, never()).enqueue(any());
    }

    @Test
    public void testDeletePatientMedicineLocalMode() throws Exception {
        setupLocalMode();

        CachedMedicine med1 = new CachedMedicine();
        med1.setId(1L);
        med1.setPatientId(-1L);
        med1.setMedicineName("Aspirin");
        CachedMedicine med2 = new CachedMedicine();
        med2.setId(2L);
        med2.setPatientId(-1L);
        med2.setMedicineName("Ibuprofen");

        when(cachedMedicineDao.getMedicines(eq(-1L))).thenReturn(new ArrayList<>(Arrays.asList(med1, med2)));

        MedicineRepository repo = new MedicineRepository(application);
        repo.deletePatientMedicine(1L);

        verify(cachedMedicineDao).deleteByPatientId(-1L);
        verify(cachedMedicineDao).insertAll(anyList());
        assertNotNull(repo.getPatientMedicines().getValue());
        assertEquals(1, repo.getPatientMedicines().getValue().size());
        assertEquals("Ibuprofen", repo.getPatientMedicines().getValue().get(0).getMedicineName());
    }

    @Test
    public void testDeletePatientMedicineOffline() throws Exception {
        setupAuthenticated();
        setOffline();

        PatientMedicineResponse med = new PatientMedicineResponse();
        med.setId(5L);
        med.setMedicineName("Aspirin");
        List<PatientMedicineResponse> meds = new ArrayList<>();
        meds.add(med);

        doAnswer(inv -> {
            Callback<List<PatientMedicineResponse>> cb = inv.getArgument(0);
            cb.onResponse(medicinesCall, Response.success(meds));
            return null;
        }).when(medicinesCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.loadPatientMedicines();

        repo.deletePatientMedicine(5L);

        verify(pendingOperationDao).insert(any());
        assertNotNull(repo.getPatientMedicines().getValue());
        assertTrue(repo.getPatientMedicines().getValue().isEmpty());
    }

    @Test
    public void testUpdatePatientMedicineLocalMode() throws Exception {
        setupLocalMode();

        CachedMedicine med = new CachedMedicine();
        med.setId(1L);
        med.setPatientId(-1L);
        med.setMedicineName("Aspirin");
        med.setExpirationDate("2026-01-01");
        med.setQuantity("10");

        when(cachedMedicineDao.getMedicines(eq(-1L))).thenReturn(new ArrayList<>(Collections.singletonList(med)));

        MedicineRepository repo = new MedicineRepository(application);
        repo.updatePatientMedicine(1L, "2027-06-01", "20");

        verify(cachedMedicineDao).deleteByPatientId(-1L);
        verify(cachedMedicineDao).insertAll(anyList());
        assertNotNull(repo.getPatientMedicines().getValue());
        assertEquals("2027-06-01", repo.getPatientMedicines().getValue().get(0).getExpirationDate());
        assertEquals("20", repo.getPatientMedicines().getValue().get(0).getQuantity());
    }

    @Test
    public void testUpdatePatientMedicineApiSuccess() throws Exception {
        setupAuthenticated();

        when(api.updateMedicine(eq(1L), eq(10L), any(PatientMedicineRequest.class))).thenReturn(updateMedicineCall);

        doAnswer(inv -> {
            Callback<PatientMedicineResponse> cb = inv.getArgument(0);
            cb.onResponse(updateMedicineCall, Response.success(new PatientMedicineResponse()));
            return null;
        }).when(updateMedicineCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.updatePatientMedicine(10L, "2027-06-01", "20");

        assertNull(repo.getError().getValue());
    }

    @Test
    public void testUpdatePatientMedicineApiFailure() throws Exception {
        setupAuthenticated();

        when(api.updateMedicine(eq(1L), eq(10L), any(PatientMedicineRequest.class))).thenReturn(updateMedicineCall);

        doAnswer(inv -> {
            Callback<PatientMedicineResponse> cb = inv.getArgument(0);
            cb.onFailure(updateMedicineCall, new Exception("connection lost"));
            return null;
        }).when(updateMedicineCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.updatePatientMedicine(10L, "2027-06-01", "20");

        assertTrue(repo.getError().getValue().contains("connection lost"));
    }

    @Test
    public void testSearchMedicinesSuccess() throws Exception {
        setupAuthenticated();

        ReferenceMedicineResponse ref = new ReferenceMedicineResponse();
        ref.setId(1L);
        ref.setName("Aspirin");

        when(api.getReferenceMedicines(anyString())).thenReturn(searchCall);

        doAnswer(inv -> {
            Callback<List<ReferenceMedicineResponse>> cb = inv.getArgument(0);
            cb.onResponse(searchCall, Response.success(Collections.singletonList(ref)));
            return null;
        }).when(searchCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.searchMedicines("asp");

        assertNotNull(repo.getSearchResults().getValue());
        assertEquals(1, repo.getSearchResults().getValue().size());
        assertEquals("Aspirin", repo.getSearchResults().getValue().get(0).getName());
    }

    @Test
    public void testSearchMedicinesFailure() throws Exception {
        setupAuthenticated();

        when(api.getReferenceMedicines(anyString())).thenReturn(searchCall);

        doAnswer(inv -> {
            Callback<List<ReferenceMedicineResponse>> cb = inv.getArgument(0);
            cb.onFailure(searchCall, new Exception("timeout"));
            return null;
        }).when(searchCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.searchMedicines("asp");

        assertTrue(repo.getError().getValue().contains("timeout"));
    }

    @Test
    public void testGetRecommendationsSuccess() throws Exception {
        setupAuthenticated();

        ReferenceMedicineResponse ref = new ReferenceMedicineResponse();
        ref.setId(1L);
        ref.setName("Ibuprofen");

        when(api.getRecommendations(anyList())).thenReturn(recommendationsCall);

        doAnswer(inv -> {
            Callback<List<ReferenceMedicineResponse>> cb = inv.getArgument(0);
            cb.onResponse(recommendationsCall, Response.success(Collections.singletonList(ref)));
            return null;
        }).when(recommendationsCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.getRecommendations(Arrays.asList("headache", "fever"));

        assertNotNull(repo.getSearchResults().getValue());
        assertEquals(1, repo.getSearchResults().getValue().size());
        assertEquals("Ibuprofen", repo.getSearchResults().getValue().get(0).getName());
    }

    @Test
    public void testGetRecommendationsFailure() throws Exception {
        setupAuthenticated();

        when(api.getRecommendations(anyList())).thenReturn(recommendationsCall);

        doAnswer(inv -> {
            Callback<List<ReferenceMedicineResponse>> cb = inv.getArgument(0);
            cb.onResponse(recommendationsCall, Response.error(500,
                    okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(recommendationsCall).enqueue(any());

        MedicineRepository repo = new MedicineRepository(application);
        repo.getRecommendations(Arrays.asList("headache"));

        assertTrue(repo.getError().getValue().contains("500"));
    }

    // ==================== SymptomsRepository ====================

    @Test
    public void testLoadSymptomsLocalMode() throws Exception {
        setupLocalMode();

        CachedSymptom cached = new CachedSymptom();
        cached.setId(1L);
        cached.setPatientId(-1L);
        cached.setSymptom("headache");
        when(cachedSymptomDao.getSymptoms(eq(-1L))).thenReturn(Collections.singletonList(cached));

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.loadSymptoms();

        assertNotNull(repo.getAllSymptoms().getValue());
        assertEquals(1, repo.getAllSymptoms().getValue().size());
        assertEquals("headache", repo.getAllSymptoms().getValue().get(0).getSymptom());
        assertNull(repo.getError().getValue());
        verify(api, never()).getSymptoms(anyLong());
    }

    @Test
    public void testAddSymptomLocalMode() throws Exception {
        setupLocalMode();

        List<CachedSymptom> existing = new ArrayList<>();
        when(cachedSymptomDao.getSymptoms(eq(-1L))).thenReturn(existing);

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.addSymptom("fever");

        verify(cachedSymptomDao).insertAll(anyList());
        assertNotNull(repo.getAllSymptoms().getValue());
        assertEquals(1, repo.getAllSymptoms().getValue().size());
        assertEquals("fever", repo.getAllSymptoms().getValue().get(0).getSymptom());
    }

    @Test
    public void testAddSymptomOnlineSuccess() throws Exception {
        setupAuthenticated();

        when(api.addSymptom(eq(1L), any(PatientSymptomRequest.class))).thenReturn(addSymptomCall);

        doAnswer(inv -> {
            Callback<PatientSymptomResponse> cb = inv.getArgument(0);
            cb.onResponse(addSymptomCall, Response.success(new PatientSymptomResponse()));
            return null;
        }).when(addSymptomCall).enqueue(any());

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.addSymptom("headache");

        verify(api).addSymptom(eq(1L), any(PatientSymptomRequest.class));
        assertNull(repo.getError().getValue());
    }

    @Test
    public void testAddSymptomOnlineFailure() throws Exception {
        setupAuthenticated();

        when(api.addSymptom(eq(1L), any(PatientSymptomRequest.class))).thenReturn(addSymptomCall);

        doAnswer(inv -> {
            Callback<PatientSymptomResponse> cb = inv.getArgument(0);
            cb.onFailure(addSymptomCall, new Exception("fail"));
            return null;
        }).when(addSymptomCall).enqueue(any());

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.addSymptom("headache");

        verify(pendingOperationDao).insert(any());
    }

    @Test
    public void testAddSymptomOffline() throws Exception {
        setupAuthenticated();
        setOffline();

        when(api.addSymptom(eq(1L), any(PatientSymptomRequest.class))).thenReturn(addSymptomCall);

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.addSymptom("headache");

        verify(pendingOperationDao).insert(any());
        verify(addSymptomCall, never()).enqueue(any());
        assertNotNull(repo.getAllSymptoms().getValue());
        assertEquals(1, repo.getAllSymptoms().getValue().size());
        assertEquals("headache", repo.getAllSymptoms().getValue().get(0).getSymptom());
    }

    @Test
    public void testDeleteSymptomLocalMode() throws Exception {
        setupLocalMode();

        CachedSymptom s1 = new CachedSymptom();
        s1.setId(1L);
        s1.setPatientId(-1L);
        s1.setSymptom("headache");
        CachedSymptom s2 = new CachedSymptom();
        s2.setId(2L);
        s2.setPatientId(-1L);
        s2.setSymptom("fever");

        when(cachedSymptomDao.getSymptoms(eq(-1L))).thenReturn(new ArrayList<>(Arrays.asList(s1, s2)));

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.deleteSymptom(1L);

        verify(cachedSymptomDao).deleteByPatientId(-1L);
        verify(cachedSymptomDao).insertAll(anyList());
        assertNotNull(repo.getAllSymptoms().getValue());
        assertEquals(1, repo.getAllSymptoms().getValue().size());
        assertEquals("fever", repo.getAllSymptoms().getValue().get(0).getSymptom());
    }

    @Test
    public void testDeleteSymptomOffline() throws Exception {
        setupAuthenticated();
        setOffline();

        PatientSymptomResponse s = new PatientSymptomResponse();
        s.setId(5L);
        s.setSymptom("headache");

        doAnswer(inv -> {
            Callback<List<PatientSymptomResponse>> cb = inv.getArgument(0);
            cb.onResponse(symptomsCall, Response.success(Collections.singletonList(s)));
            return null;
        }).when(symptomsCall).enqueue(any());

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.loadSymptoms();

        repo.deleteSymptom(5L);

        verify(pendingOperationDao).insert(any());
        assertNotNull(repo.getAllSymptoms().getValue());
        assertTrue(repo.getAllSymptoms().getValue().isEmpty());
    }

    @Test
    public void testRenewSymptomLocalMode() throws Exception {
        setupLocalMode();

        CachedSymptom s = new CachedSymptom();
        s.setId(1L);
        s.setPatientId(-1L);
        s.setSymptom("headache");
        s.setTimestamp("2026-01-01T00:00:00");

        when(cachedSymptomDao.getSymptoms(eq(-1L))).thenReturn(new ArrayList<>(Collections.singletonList(s)));

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.renewSymptom(1L);

        verify(cachedSymptomDao).deleteByPatientId(-1L);
        verify(cachedSymptomDao).insertAll(anyList());
        assertNotNull(repo.getAllSymptoms().getValue());
        assertNotEquals("2026-01-01T00:00:00", repo.getAllSymptoms().getValue().get(0).getTimestamp());
    }

    @Test
    public void testRenewSymptomApiSuccess() throws Exception {
        setupAuthenticated();

        when(api.renewSymptom(eq(1L), eq(10L))).thenReturn(renewSymptomCall);

        doAnswer(inv -> {
            Callback<PatientSymptomResponse> cb = inv.getArgument(0);
            cb.onResponse(renewSymptomCall, Response.success(new PatientSymptomResponse()));
            return null;
        }).when(renewSymptomCall).enqueue(any());

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.renewSymptom(10L);

        assertNull(repo.getError().getValue());
    }

    @Test
    public void testRenewSymptomApiFailure() throws Exception {
        setupAuthenticated();

        when(api.renewSymptom(eq(1L), eq(10L))).thenReturn(renewSymptomCall);

        doAnswer(inv -> {
            Callback<PatientSymptomResponse> cb = inv.getArgument(0);
            cb.onFailure(renewSymptomCall, new Exception("connection lost"));
            return null;
        }).when(renewSymptomCall).enqueue(any());

        SymptomsRepository repo = new SymptomsRepository(application);
        repo.renewSymptom(10L);

        assertTrue(repo.getError().getValue().contains("connection lost"));
    }
}
