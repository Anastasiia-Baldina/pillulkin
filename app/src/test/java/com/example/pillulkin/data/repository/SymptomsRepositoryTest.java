package com.example.pillulkin.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.CachedSymptomDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;

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

public class SymptomsRepositoryTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private Call<List<PatientSymptomResponse>> symptomsCall;
    @Mock private Call<PatientSymptomResponse> addSymptomCall;
    @Mock private Call<PatientSymptomResponse> renewSymptomCall;
    @Mock private PillulkinDatabase db;
    @Mock private CachedSymptomDao cachedSymptomDao;
    @Mock private PendingOperationDao pendingOperationDao;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private SymptomsRepository repository;
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

        when(db.cachedSymptomDao()).thenReturn(cachedSymptomDao);
        when(db.pendingOperationDao()).thenReturn(pendingOperationDao);
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

        when(api.getSymptoms(eq(1L))).thenReturn(symptomsCall);
    }

    @Test
    public void testInitialState() {
        repository = new SymptomsRepository(application);
        assertNull(repository.getAllSymptoms().getValue());
        assertFalse(repository.isLoading().getValue());
    }

    @Test
    public void testLoadSymptomsNotAuthenticated() {
        setupNotAuthenticated();
        repository = new SymptomsRepository(application);
        repository.loadSymptoms();
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testAddSymptomNotAuthenticated() {
        setupNotAuthenticated();
        repository = new SymptomsRepository(application);
        repository.addSymptom("headache");
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testDeleteSymptomNotAuthenticated() {
        setupNotAuthenticated();
        repository = new SymptomsRepository(application);
        repository.deleteSymptom(1L);
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testLoadSymptomsSuccess() throws Exception {
        setupAuthenticated();

        PatientSymptomResponse symptom = new PatientSymptomResponse();
        symptom.setId(1L);
        symptom.setSymptom("headache");
        symptom.setTimestamp("2026-04-15T10:00:00");

        List<PatientSymptomResponse> symptoms = new ArrayList<>();
        symptoms.add(symptom);

        doAnswer(invocation -> {
            Callback<List<PatientSymptomResponse>> callback = invocation.getArgument(0);
            callback.onResponse(symptomsCall, Response.success(symptoms));
            return null;
        }).when(symptomsCall).enqueue(any());

        repository = new SymptomsRepository(application);
        repository.loadSymptoms();

        assertNotNull(repository.getAllSymptoms().getValue());
        assertEquals(1, repository.getAllSymptoms().getValue().size());
        assertEquals("headache", repository.getAllSymptoms().getValue().get(0).getSymptom());
    }

    @Test
    public void testLoadSymptomsServerError() throws Exception {
        setupAuthenticated();

        doAnswer(invocation -> {
            Callback<List<PatientSymptomResponse>> callback = invocation.getArgument(0);
            callback.onResponse(symptomsCall, Response.error(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(symptomsCall).enqueue(any());

        repository = new SymptomsRepository(application);
        repository.loadSymptoms();

        assertTrue(repository.getError().getValue().contains("500"));
    }

    @Test
    public void testLoadSymptomsNetworkFailure() throws Exception {
        setupAuthenticated();

        doAnswer(invocation -> {
            Callback<List<PatientSymptomResponse>> callback = invocation.getArgument(0);
            callback.onFailure(symptomsCall, new Exception("Connection refused"));
            return null;
        }).when(symptomsCall).enqueue(any());

        repository = new SymptomsRepository(application);
        repository.loadSymptoms();

        assertTrue(repository.getError().getValue().contains("Connection refused"));
    }

    @Test
    public void testRenewSymptomNotAuthenticated() {
        setupNotAuthenticated();
        repository = new SymptomsRepository(application);
        repository.renewSymptom(1L);
        assertEquals("Not authenticated", repository.getError().getValue());
    }
}
