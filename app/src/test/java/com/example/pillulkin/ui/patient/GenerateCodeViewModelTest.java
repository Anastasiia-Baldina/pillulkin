package com.example.pillulkin.ui.patient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class GenerateCodeViewModelTest {

    @Rule public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private PillulkinDatabase db;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private GenerateCodeViewModel viewModel;
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
    public void testInitialState_localMode() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);

        NetworkModule instance = NetworkModule.getInstance(context);
        try {
            Field apiField = NetworkModule.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(instance, api);
        } catch (Exception e) { throw new RuntimeException(e); }

        viewModel = new GenerateCodeViewModel(application);
        assertTrue(viewModel.isLocalMode().getValue());
        assertFalse(viewModel.isCodeGenerated().getValue());
    }

    @Test
    public void testGenerateCodeLocalMode() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);

        NetworkModule instance = NetworkModule.getInstance(context);
        try {
            Field apiField = NetworkModule.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(instance, api);
        } catch (Exception e) { throw new RuntimeException(e); }

        viewModel = new GenerateCodeViewModel(application);
        viewModel.generateCode(60);

        assertTrue(viewModel.isCodeGenerated().getValue());
        assertNotNull(viewModel.getLocalCode().getValue());
        assertEquals(6, viewModel.getLocalCode().getValue().length());
    }

    @Test
    public void testGenerateCodeRemoteMode() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");

        NetworkModule instance = NetworkModule.getInstance(context);
        try {
            Field apiField = NetworkModule.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(instance, api);
        } catch (Exception e) { throw new RuntimeException(e); }

        when(api.generateDoctorCode(any())).thenReturn(mock(retrofit2.Call.class));

        viewModel = new GenerateCodeViewModel(application);
        viewModel.generateCode(60);

        assertTrue(viewModel.isCodeGenerated().getValue());
    }
}
