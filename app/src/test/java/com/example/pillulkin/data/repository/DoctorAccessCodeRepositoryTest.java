package com.example.pillulkin.data.repository;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.AuthResponse;
import com.example.pillulkin.data.remote.model.DoctorCodeResponse;
import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;

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

public class DoctorAccessCodeRepositoryTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private Call<DoctorCodeResponse> generateCodeCall;
    @Mock private Call<AuthResponse> loginCall;
    @Mock private Call<DoctorFullDataResponse> fullDataCall;
    @Mock private PillulkinDatabase db;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private DoctorAccessCodeRepository repository;
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
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);

        dbStatic = mockStatic(PillulkinDatabase.class);
        dbStatic.when(() -> PillulkinDatabase.getDatabase(any(Context.class))).thenReturn(db);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);
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

    @Test
    public void testInitialState() {
        repository = new DoctorAccessCodeRepository(application);
        assertNull(repository.getGeneratedCode().getValue());
        assertNull(repository.getPatientData().getValue());
        assertFalse(repository.isLoading().getValue());
    }

    @Test
    public void testLoadPatientDataNotAuthenticated() {
        when(prefs.getString(eq("doctor_token"), any())).thenReturn(null);
        repository = new DoctorAccessCodeRepository(application);
        repository.loadPatientFullData();
        assertEquals("Not authenticated as doctor", repository.getError().getValue());
    }

    @Test
    public void testLogout() {
        repository = new DoctorAccessCodeRepository(application);
        repository.logout();
        verify(editor).remove("doctor_token");
        verify(editor).remove("doctor_patient_id");
    }

    @Test
    public void testGenerateAccessCodeSuccess() {
        when(api.generateDoctorCode(any())).thenReturn(generateCodeCall);

        DoctorCodeResponse codeResponse = new DoctorCodeResponse();
        codeResponse.setCode("123456");
        codeResponse.setExpiresAt("2026-04-27T12:00:00");

        doAnswer(invocation -> {
            Callback<DoctorCodeResponse> callback = invocation.getArgument(0);
            callback.onResponse(generateCodeCall, Response.success(codeResponse));
            return null;
        }).when(generateCodeCall).enqueue(any());

        repository = new DoctorAccessCodeRepository(application);
        repository.generateAccessCode(1L, 60);

        assertEquals("123456", repository.getGeneratedCode().getValue());
        assertFalse(repository.isLoading().getValue());
    }

    @Test
    public void testGenerateAccessCodeServerError() {
        when(api.generateDoctorCode(any())).thenReturn(generateCodeCall);

        doAnswer(invocation -> {
            Callback<DoctorCodeResponse> callback = invocation.getArgument(0);
            callback.onResponse(generateCodeCall, Response.error(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(generateCodeCall).enqueue(any());

        repository = new DoctorAccessCodeRepository(application);
        repository.generateAccessCode(1L, 60);

        assertTrue(repository.getError().getValue().contains("500"));
    }

    @Test
    public void testGenerateAccessCodeNetworkFailure() {
        when(api.generateDoctorCode(any())).thenReturn(generateCodeCall);

        doAnswer(invocation -> {
            Callback<DoctorCodeResponse> callback = invocation.getArgument(0);
            callback.onFailure(generateCodeCall, new Exception("Connection refused"));
            return null;
        }).when(generateCodeCall).enqueue(any());

        repository = new DoctorAccessCodeRepository(application);
        repository.generateAccessCode(1L, 60);

        assertTrue(repository.getError().getValue().contains("Connection refused"));
    }

    @Test
    public void testLoginAsDoctorSuccess() {
        when(api.loginDoctor(any())).thenReturn(loginCall);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken("doc_token_123");
        authResponse.setPatientId(5L);

        doAnswer(invocation -> {
            Callback<AuthResponse> callback = invocation.getArgument(0);
            callback.onResponse(loginCall, Response.success(authResponse));
            return null;
        }).when(loginCall).enqueue(any());

        repository = new DoctorAccessCodeRepository(application);
        repository.loginAsDoctor("123456");

        assertTrue(repository.getLoginSuccess().getValue());
        verify(editor).putString("doctor_token", "doc_token_123");
        verify(editor).putLong("doctor_patient_id", 5L);
    }

    @Test
    public void testLoginAsDoctorInvalidCode() {
        when(api.loginDoctor(any())).thenReturn(loginCall);

        doAnswer(invocation -> {
            Callback<AuthResponse> callback = invocation.getArgument(0);
            callback.onResponse(loginCall, Response.error(401, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "unauthorized")));
            return null;
        }).when(loginCall).enqueue(any());

        repository = new DoctorAccessCodeRepository(application);
        repository.loginAsDoctor("wrong_code");

        assertEquals("Invalid code", repository.getError().getValue());
    }

    @Test
    public void testNotifyLoginSuccess() {
        repository = new DoctorAccessCodeRepository(application);
        repository.notifyLoginSuccess();
        assertTrue(repository.getLoginSuccess().getValue());
    }
}
