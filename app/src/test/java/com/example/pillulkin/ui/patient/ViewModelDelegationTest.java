package com.example.pillulkin.ui.patient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.CachedMedicineDao;
import com.example.pillulkin.data.local.dao.CachedProfileDao;
import com.example.pillulkin.data.local.dao.CachedSymptomDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
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
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ViewModelDelegationTest {

    @Rule public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock Application application;
    @Mock Context context;
    @Mock SharedPreferences prefs;
    @Mock SharedPreferences.Editor editor;
    @Mock PillulkinApi api;
    @Mock PillulkinDatabase db;
    @Mock CachedMedicineDao cachedMedicineDao;
    @Mock CachedSymptomDao cachedSymptomDao;
    @Mock CachedProfileDao cachedProfileDao;
    @Mock PendingOperationDao pendingOperationDao;
    @Mock Call<PatientProfileResponse> profileCall;
    @Mock Call<List<PatientMedicineResponse>> medicinesCall;
    @Mock Call<PatientMedicineResponse> addMedicineCall;
    @Mock Call<PatientMedicineResponse> updateMedicineCall;
    @Mock Call<Void> deleteMedicineCall;
    @Mock Call<List<ReferenceMedicineResponse>> searchCall;

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
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        dbStatic = mockStatic(PillulkinDatabase.class);
        dbStatic.when(() -> PillulkinDatabase.getDatabase(any(Context.class))).thenReturn(db);
        when(db.cachedMedicineDao()).thenReturn(cachedMedicineDao);
        when(db.cachedSymptomDao()).thenReturn(cachedSymptomDao);
        when(db.cachedProfileDao()).thenReturn(cachedProfileDao);
        when(db.pendingOperationDao()).thenReturn(pendingOperationDao);
        when(cachedMedicineDao.getMedicines(anyLong())).thenReturn(Collections.emptyList());
        when(cachedProfileDao.getProfile(anyLong())).thenReturn(null);
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
    public void profileViewModel_loadsProfile() {
        PatientProfileResponse profile = new PatientProfileResponse();
        profile.setPatientId(1L);
        profile.setName("Ivan");
        profile.setAge(30);

        when(api.getProfile(eq(1L))).thenReturn(profileCall);
        doAnswer(invocation -> {
            Callback<PatientProfileResponse> callback = invocation.getArgument(0);
            callback.onResponse(profileCall, Response.success(profile));
            return null;
        }).when(profileCall).enqueue(any());

        PatientProfileViewModel vm = new PatientProfileViewModel(application);
        vm.loadProfile();

        assertNotNull(vm.getProfile().getValue());
        assertEquals("Ivan", vm.getProfile().getValue().getName());
        assertEquals(Integer.valueOf(30), vm.getProfile().getValue().getAge());
        assertFalse(vm.isLoading().getValue());
    }

    @Test
    public void profileViewModel_savesProfile() {
        PatientProfileViewModel vm = new PatientProfileViewModel(application);
        vm.saveProfile("Maria", 25, "penicillin", "none", "notes");

        assertNotNull(vm.getProfile().getValue());
        assertEquals("Maria", vm.getProfile().getValue().getName());
        assertEquals(Integer.valueOf(25), vm.getProfile().getValue().getAge());
        assertEquals("penicillin", vm.getProfile().getValue().getAllergies());
        assertEquals("none", vm.getProfile().getValue().getContraindications());
        assertEquals("notes", vm.getProfile().getValue().getNotes());
    }

    @Test
    public void profileViewModel_logout() {
        PatientProfileViewModel vm = new PatientProfileViewModel(application);
        vm.logout();

        verify(editor).remove("patient_id");
        verify(editor).remove("patient_token");
        assertNull(vm.getProfile().getValue());
    }

    @Test
    public void profileViewModel_getLiveData() {
        PatientProfileViewModel vm = new PatientProfileViewModel(application);
        assertNull(vm.getProfile().getValue());
        assertFalse(vm.isLoading().getValue());
        assertNull(vm.getError().getValue());
    }

    @Test
    public void medicineListViewModel_loadsMedicines() {
        PatientMedicineResponse med = new PatientMedicineResponse();
        med.setId(1L);
        med.setMedicineName("Aspirin");
        med.setDosage("500mg");

        List<PatientMedicineResponse> meds = new ArrayList<>();
        meds.add(med);

        when(api.getMedicines(eq(1L))).thenReturn(medicinesCall);
        doAnswer(invocation -> {
            Callback<List<PatientMedicineResponse>> callback = invocation.getArgument(0);
            callback.onResponse(medicinesCall, Response.success(meds));
            return null;
        }).when(medicinesCall).enqueue(any());

        MedicineListViewModel vm = new MedicineListViewModel(application);
        vm.loadMedicines();

        assertNotNull(vm.getMedicines().getValue());
        assertEquals(1, vm.getMedicines().getValue().size());
        assertEquals("Aspirin", vm.getMedicines().getValue().get(0).getMedicineName());
        assertFalse(vm.isLoading().getValue());
    }

    @Test
    public void medicineListViewModel_deletesMedicine() {
        MedicineListViewModel vm = new MedicineListViewModel(application);
        vm.deleteMedicine(5L);

        verify(api, never()).deleteMedicine(anyLong(), anyLong());
        assertNull(vm.getError().getValue());
    }

    @Test
    public void medicineListViewModel_getLiveData() {
        MedicineListViewModel vm = new MedicineListViewModel(application);
        assertNull(vm.getMedicines().getValue());
        assertFalse(vm.isLoading().getValue());
        assertNull(vm.getError().getValue());
    }

    @Test
    public void addEditMedicine_addMedicine() {
        AddEditMedicineViewModel vm = new AddEditMedicineViewModel(application);
        vm.addMedicine(10L, "2026-12-01", "30");

        assertTrue(vm.getSaveSuccess().getValue());
    }

    @Test
    public void addEditMedicine_updateMedicine() {
        when(api.updateMedicine(eq(1L), eq(5L), any())).thenReturn(updateMedicineCall);

        AddEditMedicineViewModel vm = new AddEditMedicineViewModel(application);
        vm.updateMedicine(5L, "2027-01-15", "20");

        assertTrue(vm.getSaveSuccess().getValue());
    }

    @Test
    public void addEditMedicine_deleteMedicine() {
        AddEditMedicineViewModel vm = new AddEditMedicineViewModel(application);
        vm.deleteMedicine(3L);

        assertNull(vm.getError().getValue());
    }

    @Test
    public void addEditMedicine_searchMedicines() {
        ReferenceMedicineResponse ref = new ReferenceMedicineResponse();
        ref.setId(1L);
        ref.setName("Aspirin");
        ref.setDosage("500mg");

        List<ReferenceMedicineResponse> results = new ArrayList<>();
        results.add(ref);

        when(api.getReferenceMedicines(eq("asp"))).thenReturn(searchCall);
        doAnswer(invocation -> {
            Callback<List<ReferenceMedicineResponse>> callback = invocation.getArgument(0);
            callback.onResponse(searchCall, Response.success(results));
            return null;
        }).when(searchCall).enqueue(any());

        AddEditMedicineViewModel vm = new AddEditMedicineViewModel(application);
        vm.searchMedicines("asp");

        assertNotNull(vm.getSearchResults().getValue());
        assertEquals(1, vm.getSearchResults().getValue().size());
        assertEquals("Aspirin", vm.getSearchResults().getValue().get(0).getName());
        assertEquals("500mg", vm.getSearchResults().getValue().get(0).getDosage());
    }
}
