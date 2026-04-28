package com.example.pillulkin.ui.doctor;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
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
import java.util.Arrays;
import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class DoctorViewModelsTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Application application;
    @Mock
    private Context context;
    @Mock
    private SharedPreferences prefs;
    @Mock
    private SharedPreferences.Editor editor;
    @Mock
    private PillulkinApi api;
    @Mock
    private PillulkinDatabase db;

    private MockedStatic<PillulkinDatabase> dbStatic;
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

    private NetworkModule initNetworkModule() {
        NetworkModule instance = NetworkModule.getInstance(context);
        try {
            Field apiField = NetworkModule.class.getDeclaredField("api");
            apiField.setAccessible(true);
            apiField.set(instance, api);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    @Test
    public void doctorSearchViewModel_initialState() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        DoctorSearchViewModel viewModel = new DoctorSearchViewModel(application);

        assertFalse(viewModel.isSearching().getValue());
        assertNull(viewModel.getError().getValue());
        assertNull(viewModel.getSearchResults().getValue());
    }

    @Test
    public void doctorSearchViewModel_searchByMedicineName_setsSearching() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        when(api.getReferenceMedicines(anyString())).thenReturn(mock(retrofit2.Call.class));

        DoctorSearchViewModel viewModel = new DoctorSearchViewModel(application);
        viewModel.searchByMedicineName("aspirin");

        assertTrue(viewModel.isSearching().getValue());
    }

    @Test
    public void doctorSearchViewModel_searchBySymptoms_setsSearching() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        when(api.getRecommendations(anyList())).thenReturn(mock(retrofit2.Call.class));

        DoctorSearchViewModel viewModel = new DoctorSearchViewModel(application);
        viewModel.searchBySymptoms(Arrays.asList("headache", "fever"));

        assertTrue(viewModel.isSearching().getValue());
    }

    @Test
    public void doctorCodeEntryViewModel_loginWithCode_nonLocalCode_delegatesToRepository() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("local_doctor_code"), any())).thenReturn(null);
        initNetworkModule();

        when(api.loginDoctor(any())).thenReturn(mock(retrofit2.Call.class));

        DoctorCodeEntryViewModel viewModel = new DoctorCodeEntryViewModel(application);
        viewModel.loginWithCode("remote-code");

        verify(api).loginDoctor(any());
    }

    @Test
    public void doctorCodeEntryViewModel_extractDataFromResponse_fullData() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        DoctorCodeEntryViewModel viewModel = new DoctorCodeEntryViewModel(application);

        DoctorFullDataResponse data = new DoctorFullDataResponse();
        data.setPatientId(1L);
        PatientProfileResponse profile = new PatientProfileResponse();
        profile.setName("Ivan");
        data.setProfile(profile);

        PatientMedicineResponse med = new PatientMedicineResponse();
        med.setMedicineName("Aspirin");
        data.setMedicines(Arrays.asList(med));

        PatientSymptomResponse sym = new PatientSymptomResponse();
        sym.setSymptom("Headache");
        data.setSymptoms(Arrays.asList(sym));

        viewModel.extractDataFromResponse(data);

        assertNotNull(viewModel.getPatientData().getValue());
        assertEquals(1L, viewModel.getPatientData().getValue().getPatientId().longValue());
        assertNotNull(viewModel.getMedicines().getValue());
        assertEquals(1, viewModel.getMedicines().getValue().size());
        assertEquals("Aspirin", viewModel.getMedicines().getValue().get(0).getMedicineName());
        assertNotNull(viewModel.getSymptoms().getValue());
        assertEquals(1, viewModel.getSymptoms().getValue().size());
        assertEquals("Headache", viewModel.getSymptoms().getValue().get(0).getSymptom());
    }

    @Test
    public void doctorCodeEntryViewModel_extractDataFromResponse_nullMedicinesAndSymptoms() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        DoctorCodeEntryViewModel viewModel = new DoctorCodeEntryViewModel(application);

        DoctorFullDataResponse data = new DoctorFullDataResponse();
        data.setPatientId(2L);
        data.setMedicines(null);
        data.setSymptoms(null);

        viewModel.extractDataFromResponse(data);

        assertNotNull(viewModel.getPatientData().getValue());
        assertNotNull(viewModel.getMedicines().getValue());
        assertTrue(viewModel.getMedicines().getValue().isEmpty());
        assertNotNull(viewModel.getSymptoms().getValue());
        assertTrue(viewModel.getSymptoms().getValue().isEmpty());
    }

    @Test
    public void doctorCodeEntryViewModel_extractDataFromResponse_nullData_doesNothing() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        DoctorCodeEntryViewModel viewModel = new DoctorCodeEntryViewModel(application);

        viewModel.extractDataFromResponse(null);

        assertNull(viewModel.getPatientData().getValue());
        assertNull(viewModel.getMedicines().getValue());
        assertNull(viewModel.getSymptoms().getValue());
    }

    @Test
    public void doctorCodeEntryViewModel_logout_clearsLocalSession() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        initNetworkModule();

        DoctorCodeEntryViewModel viewModel = new DoctorCodeEntryViewModel(application);
        viewModel.logout();

        verify(editor, atLeastOnce()).putBoolean(eq("local_doctor_session"), eq(false));
        verify(editor, atLeastOnce()).apply();
    }
}
