package com.example.pillulkin.ui.patient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PrescriptionsViewModelTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private Call<List<PrescriptionResponse>> prescriptionsCall;
    @Mock private PillulkinDatabase db;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private PrescriptionsViewModel viewModel;
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

        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);

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
        viewModel = new PrescriptionsViewModel(application);
        assertNull(viewModel.getPrescriptions().getValue());
        assertFalse(viewModel.isLoading().getValue());
        assertNull(viewModel.getError().getValue());
    }

    @Test
    public void testLoadPrescriptionsSuccess() {
        List<PrescriptionResponse> prescriptions = new ArrayList<>();
        PrescriptionResponse p = new PrescriptionResponse();
        try {
            Field f = PrescriptionResponse.class.getDeclaredField("medicineName");
            f.setAccessible(true); f.set(p, "Aspirin");
            f = PrescriptionResponse.class.getDeclaredField("dosage");
            f.setAccessible(true); f.set(p, "500mg");
            f = PrescriptionResponse.class.getDeclaredField("id");
            f.setAccessible(true); f.set(p, 1L);
        } catch (Exception e) { throw new RuntimeException(e); }
        prescriptions.add(p);

        when(api.getPrescriptions(eq(1L), eq("active"))).thenReturn(prescriptionsCall);

        doAnswer(invocation -> {
            Callback<List<PrescriptionResponse>> callback = invocation.getArgument(0);
            callback.onResponse(prescriptionsCall, Response.success(prescriptions));
            return null;
        }).when(prescriptionsCall).enqueue(any());

        viewModel = new PrescriptionsViewModel(application);
        viewModel.loadPrescriptions();

        assertNotNull(viewModel.getPrescriptions().getValue());
        assertEquals(1, viewModel.getPrescriptions().getValue().size());
        assertFalse(viewModel.isLoading().getValue());
    }

    @Test
    public void testLoadPrescriptionsServerError() {
        when(api.getPrescriptions(eq(1L), eq("active"))).thenReturn(prescriptionsCall);

        doAnswer(invocation -> {
            Callback<List<PrescriptionResponse>> callback = invocation.getArgument(0);
            callback.onResponse(prescriptionsCall, Response.error(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(prescriptionsCall).enqueue(any());

        viewModel = new PrescriptionsViewModel(application);
        viewModel.loadPrescriptions();

        assertTrue(viewModel.getError().getValue().contains("500"));
        assertFalse(viewModel.isLoading().getValue());
    }

    @Test
    public void testLoadPrescriptionsNetworkFailure() {
        when(api.getPrescriptions(eq(1L), eq("active"))).thenReturn(prescriptionsCall);

        doAnswer(invocation -> {
            Callback<List<PrescriptionResponse>> callback = invocation.getArgument(0);
            callback.onFailure(prescriptionsCall, new Exception("Network error"));
            return null;
        }).when(prescriptionsCall).enqueue(any());

        viewModel = new PrescriptionsViewModel(application);
        viewModel.loadPrescriptions();

        assertEquals("Ошибка сети", viewModel.getError().getValue());
        assertFalse(viewModel.isLoading().getValue());
    }

    @Test
    public void testLoadPrescriptionsSkipsWhenNotLoggedIn() {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);

        viewModel = new PrescriptionsViewModel(application);
        viewModel.loadPrescriptions();

        assertNull(viewModel.getPrescriptions().getValue());
        assertFalse(viewModel.isLoading().getValue());
    }

    @Test
    public void testMoveToCabinetSuccess() {
        Call<Void> moveCall = mock(Call.class);
        when(api.moveToCabinet(eq(1L), eq(5L))).thenReturn(moveCall);
        when(api.getPrescriptions(eq(1L), eq("active"))).thenReturn(prescriptionsCall);

        doAnswer(invocation -> {
            Callback<Void> callback = invocation.getArgument(0);
            callback.onResponse(moveCall, Response.success(null));
            return null;
        }).when(moveCall).enqueue(any());

        doAnswer(invocation -> {
            Callback<List<PrescriptionResponse>> callback = invocation.getArgument(0);
            callback.onResponse(prescriptionsCall, Response.success(new ArrayList<>()));
            return null;
        }).when(prescriptionsCall).enqueue(any());

        viewModel = new PrescriptionsViewModel(application);

        PrescriptionResponse p = new PrescriptionResponse();
        try {
            Field f = PrescriptionResponse.class.getDeclaredField("id");
            f.setAccessible(true); f.set(p, 5L);
        } catch (Exception e) { throw new RuntimeException(e); }

        viewModel.moveToCabinet(p);
    }
}
