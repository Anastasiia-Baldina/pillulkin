package com.example.pillulkin.ui.patient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.DiagnosisResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SymptomsViewModelTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private Call<DiagnosisResponse> diagnoseCall;
    @Mock private PillulkinDatabase db;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private SymptomsViewModel viewModel;
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
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);

        dbStatic = mockStatic(PillulkinDatabase.class);
        dbStatic.when(() -> PillulkinDatabase.getDatabase(any(Context.class))).thenReturn(db);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);

        when(api.diagnose(any())).thenReturn(diagnoseCall);
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

    private DiagnosisResponse buildResponse(List<String> questions, String diagnosis, double confidence) {
        DiagnosisResponse response = new DiagnosisResponse();
        try {
            Field sqField = DiagnosisResponse.class.getDeclaredField("suggestedQuestions");
            sqField.setAccessible(true);
            sqField.set(response, questions);

            Field dField = DiagnosisResponse.class.getDeclaredField("diagnosis");
            dField.setAccessible(true);
            dField.set(response, diagnosis);

            Field cField = DiagnosisResponse.class.getDeclaredField("confidence");
            cField.setAccessible(true);
            cField.set(response, confidence);

            Field fField = DiagnosisResponse.class.getDeclaredField("isFinal");
            fField.setAccessible(true);
            fField.set(response, diagnosis != null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @Test
    public void testInitialState() {
        viewModel = new SymptomsViewModel(application);
        assertNull(viewModel.getDiagnosisResult().getValue());
        assertNull(viewModel.getSuggestedQuestions().getValue());
        assertFalse(viewModel.isLoadingDiagnosis().getValue());
    }

    @Test
    public void testDiagnoseInitialWithQuestions() {
        DiagnosisResponse response = buildResponse(
                Arrays.asList("Есть ли температура?", "Сколько дней?"), null, 0.0);

        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onResponse(diagnoseCall, Response.success(response));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.diagnoseInitial(Arrays.asList("головная боль", "кашель"));

        assertNotNull(viewModel.getSuggestedQuestions().getValue());
        assertEquals(2, viewModel.getSuggestedQuestions().getValue().size());
        assertEquals("Есть ли температура?", viewModel.getSuggestedQuestions().getValue().get(0));
    }

    @Test
    public void testDiagnoseInitialDirectResult() {
        DiagnosisResponse response = buildResponse(Collections.emptyList(), "ОРВИ", 0.85);

        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onResponse(diagnoseCall, Response.success(response));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.diagnoseInitial(Arrays.asList("насморк"));

        assertNotNull(viewModel.getDiagnosisResult().getValue());
        assertTrue(viewModel.getDiagnosisResult().getValue().contains("ОРВИ"));
        assertTrue(viewModel.getDiagnosisResult().getValue().contains("85%"));
    }

    @Test
    public void testDiagnoseInitialServerError() {
        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onResponse(diagnoseCall, Response.error(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.diagnoseInitial(Arrays.asList("головная боль"));

        assertEquals("Ошибка диагностики", viewModel.getDiagnosisResult().getValue());
        assertFalse(viewModel.isLoadingDiagnosis().getValue());
    }

    @Test
    public void testDiagnoseInitialNetworkFailure() {
        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onFailure(diagnoseCall, new Exception("Connection refused"));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.diagnoseInitial(Arrays.asList("головная боль"));

        assertTrue(viewModel.getDiagnosisResult().getValue().contains("Connection refused"));
        assertFalse(viewModel.isLoadingDiagnosis().getValue());
    }

    @Test
    public void testRequestFinalDiagnosisSuccess() {
        DiagnosisResponse response = buildResponse(Collections.emptyList(), "Грипп", 0.92);

        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onResponse(diagnoseCall, Response.success(response));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.requestFinalDiagnosis(Arrays.asList("yes", "3 days"));

        assertNotNull(viewModel.getDiagnosisResult().getValue());
        assertTrue(viewModel.getDiagnosisResult().getValue().contains("Грипп"));
        assertTrue(viewModel.getDiagnosisResult().getValue().contains("92%"));
    }

    @Test
    public void testRequestFinalDiagnosisServerError() {
        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onResponse(diagnoseCall, Response.error(500, okhttp3.ResponseBody.create(okhttp3.MediaType.parse("text/plain"), "error")));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.requestFinalDiagnosis(Arrays.asList("yes"));

        assertEquals("Ошибка диагностики", viewModel.getDiagnosisResult().getValue());
    }

    @Test
    public void testRequestFinalDiagnosisNetworkFailure() {
        doAnswer(invocation -> {
            Callback<DiagnosisResponse> callback = invocation.getArgument(0);
            callback.onFailure(diagnoseCall, new Exception("Timeout"));
            return null;
        }).when(diagnoseCall).enqueue(any());

        viewModel = new SymptomsViewModel(application);
        viewModel.requestFinalDiagnosis(Arrays.asList("yes"));

        assertTrue(viewModel.getDiagnosisResult().getValue().contains("Timeout"));
    }
}
