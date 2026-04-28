package com.example.pillulkin.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.CachedProfileDao;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PatientProfileRepositoryTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private Call<PatientProfileResponse> profileCall;
    @Mock private PillulkinDatabase db;
    @Mock private CachedProfileDao cachedProfileDao;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private PatientProfileRepository repository;
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

        when(db.cachedProfileDao()).thenReturn(cachedProfileDao);

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

    private void setupAuthenticated() throws Exception {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);

        when(api.getProfile(eq(1L))).thenReturn(profileCall);
    }

    private void setupNotAuthenticated() {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);
    }

    @Test
    public void testInitialState() throws Exception {
        setupAuthenticated();
        repository = new PatientProfileRepository(application);
        assertNull(repository.getProfile().getValue());
        assertFalse(repository.isLoading().getValue());
    }

    @Test
    public void testLoadProfileNotAuthenticatedReturnsCache() {
        setupNotAuthenticated();
        repository = new PatientProfileRepository(application);
        repository.loadProfile();
        assertNull(repository.getError().getValue());
        assertNull(repository.getProfile().getValue());
    }

    @Test
    public void testSaveProfileNotAuthenticated() {
        setupNotAuthenticated();
        repository = new PatientProfileRepository(application);
        repository.saveProfile("Ivan", 30, "none", "none", "notes");
        assertEquals("Not authenticated", repository.getError().getValue());
    }

    @Test
    public void testLogout() throws Exception {
        setupAuthenticated();
        repository = new PatientProfileRepository(application);
        repository.logout();
        verify(editor).remove("patient_id");
        verify(editor).remove("patient_token");
    }

    @Test
    public void testLoadProfileSuccess() throws Exception {
        setupAuthenticated();

        PatientProfileResponse profile = new PatientProfileResponse();
        profile.setPatientId(1L);
        profile.setName("Ivan");
        profile.setAge(30);

        doAnswer(invocation -> {
            Callback<PatientProfileResponse> callback = invocation.getArgument(0);
            callback.onResponse(profileCall, Response.success(profile));
            return null;
        }).when(profileCall).enqueue(any());

        repository = new PatientProfileRepository(application);
        repository.loadProfile();

        assertNotNull(repository.getProfile().getValue());
        assertEquals("Ivan", repository.getProfile().getValue().getName());
        assertEquals(Integer.valueOf(30), repository.getProfile().getValue().getAge());
    }

    @Test
    public void testLoadProfileServerError() throws Exception {
        setupAuthenticated();

        doAnswer(invocation -> {
            Callback<PatientProfileResponse> callback = invocation.getArgument(0);
            callback.onResponse(profileCall, Response.error(404, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "not found")));
            return null;
        }).when(profileCall).enqueue(any());

        repository = new PatientProfileRepository(application);
        repository.loadProfile();

        assertTrue(repository.getError().getValue().contains("404"));
    }

    @Test
    public void testLoadProfileNetworkFailure() throws Exception {
        setupAuthenticated();

        doAnswer(invocation -> {
            Callback<PatientProfileResponse> callback = invocation.getArgument(0);
            callback.onFailure(profileCall, new Exception("Timeout"));
            return null;
        }).when(profileCall).enqueue(any());

        repository = new PatientProfileRepository(application);
        repository.loadProfile();

        assertTrue(repository.getError().getValue().contains("Timeout"));
    }

    @Test
    public void testSaveProfileOptimisticUpdate() throws Exception {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);

        repository = new PatientProfileRepository(application);
        repository.saveProfile("Maria", 25, "penicillin", "none", "test notes");

        assertNotNull(repository.getProfile().getValue());
        assertEquals("Maria", repository.getProfile().getValue().getName());
        assertEquals(Integer.valueOf(25), repository.getProfile().getValue().getAge());
        assertEquals("penicillin", repository.getProfile().getValue().getAllergies());
    }
}
