package com.example.pillulkin.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.CachedMedicineDao;
import com.example.pillulkin.data.local.dao.CachedProfileDao;
import com.example.pillulkin.data.local.dao.CachedSymptomDao;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.local.entity.CachedMedicine;
import com.example.pillulkin.data.local.entity.CachedProfile;
import com.example.pillulkin.data.local.entity.CachedSymptom;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SyncExtendedTest {

    @Mock Context context;
    @Mock SharedPreferences prefs;
    @Mock SharedPreferences.Editor editor;
    @Mock PillulkinApi api;
    @Mock PillulkinDatabase db;
    @Mock PendingOperationDao pendingOperationDao;
    @Mock CachedMedicineDao cachedMedicineDao;
    @Mock CachedSymptomDao cachedSymptomDao;
    @Mock CachedProfileDao cachedProfileDao;
    @Mock ConnectivityManager connectivityManager;
    @Mock NetworkInfo networkInfo;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private AutoCloseable mocks;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        resetNetworkModule();

        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(prefs.edit()).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);

        when(db.pendingOperationDao()).thenReturn(pendingOperationDao);
        when(db.cachedMedicineDao()).thenReturn(cachedMedicineDao);
        when(db.cachedSymptomDao()).thenReturn(cachedSymptomDao);
        when(db.cachedProfileDao()).thenReturn(cachedProfileDao);
        when(pendingOperationDao.getPending()).thenReturn(Collections.emptyList());
        when(pendingOperationDao.getPendingCount()).thenReturn(0);

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
    public void syncAll_skipsWhenSyncing() throws Exception {
        SyncManager sm = new SyncManager(context);
        Field field = SyncManager.class.getDeclaredField("isSyncing");
        field.setAccessible(true);
        field.set(sm, true);
        sm.syncAll();
        assertTrue(sm.isSyncing());
    }

    @Test
    public void syncAll_skipsWhenOffline() throws Exception {
        when(networkInfo.isConnectedOrConnecting()).thenReturn(false);
        SyncManager sm = new SyncManager(context);
        sm.syncAll();
        assertFalse(sm.isSyncing());
    }

    @Test
    public void syncAll_skipsWhenNotLoggedIn() throws Exception {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);
        SyncManager sm = new SyncManager(context);
        sm.syncAll();
        assertFalse(sm.isSyncing());
    }

    @Test
    public void executeOperation_exceedsMaxRetries_marksFailed() throws Exception {
        SyncManager sm = new SyncManager(context);
        PendingOperation op = new PendingOperation();
        op.setId(1L);
        op.setRetryCount(3);
        Method method = SyncManager.class.getDeclaredMethod("executeOperation", PendingOperation.class);
        method.setAccessible(true);
        method.invoke(sm, op);
        assertEquals(PendingOperation.STATUS_FAILED, op.getStatus());
        verify(pendingOperationDao).update(op);
    }

    @Test
    public void executeByType_profileUpdate_success() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<PatientProfileResponse> call = mock(Call.class);
        when(api.updateProfile(eq(1L), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new PatientProfileResponse()));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_PROFILE_UPDATE);
        op.setPayload("Ivan|30|none|none|notes");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_profileUpdate_failure() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<PatientProfileResponse> call = mock(Call.class);
        when(api.updateProfile(eq(1L), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "error")));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_PROFILE_UPDATE);
        op.setPayload("name|25");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_symptomAdd_success() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<PatientSymptomResponse> call = mock(Call.class);
        when(api.addSymptom(eq(1L), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new PatientSymptomResponse()));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_SYMPTOM_ADD);
        op.setPayload("Headache");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_symptomAdd_failure() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<PatientSymptomResponse> call = mock(Call.class);
        when(api.addSymptom(eq(1L), any())).thenReturn(call);
        when(call.execute()).thenThrow(new RuntimeException("Network error"));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_SYMPTOM_ADD);
        op.setPayload("Headache");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_symptomDelete_success() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<Void> call = mock(Call.class);
        when(api.deleteSymptom(eq(1L), eq(42L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(null));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_SYMPTOM_DELETE);
        op.setPayload("42");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_symptomDelete_404returnsTrue() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<Void> call = mock(Call.class);
        when(api.deleteSymptom(eq(1L), eq(5L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "not found")));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_SYMPTOM_DELETE);
        op.setPayload("5");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_symptomDelete_failure() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<Void> call = mock(Call.class);
        when(api.deleteSymptom(eq(1L), eq(7L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "error")));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_SYMPTOM_DELETE);
        op.setPayload("7");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_medicineAdd_success() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<PatientMedicineResponse> call = mock(Call.class);
        when(api.addMedicine(eq(1L), any())).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(new PatientMedicineResponse()));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_MEDICINE_ADD);
        op.setPayload("100");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_medicineAdd_failure() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<PatientMedicineResponse> call = mock(Call.class);
        when(api.addMedicine(eq(1L), any())).thenReturn(call);
        when(call.execute()).thenThrow(new RuntimeException("fail"));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_MEDICINE_ADD);
        op.setPayload("100");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_medicineDelete_success() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<Void> call = mock(Call.class);
        when(api.deleteMedicine(eq(1L), eq(10L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.success(null));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_MEDICINE_DELETE);
        op.setPayload("10");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_medicineDelete_404returnsTrue() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<Void> call = mock(Call.class);
        when(api.deleteMedicine(eq(1L), eq(10L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "not found")));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_MEDICINE_DELETE);
        op.setPayload("10");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertTrue((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_medicineDelete_failure() throws Exception {
        SyncManager sm = new SyncManager(context);
        Call<Void> call = mock(Call.class);
        when(api.deleteMedicine(eq(1L), eq(10L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(500, ResponseBody.create(MediaType.parse("text/plain"), "error")));
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_MEDICINE_DELETE);
        op.setPayload("10");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        assertFalse((boolean) method.invoke(sm, op));
    }

    @Test
    public void executeByType_unknownType_deletesOperation() throws Exception {
        SyncManager sm = new SyncManager(context);
        PendingOperation op = new PendingOperation();
        op.setId(99L);
        op.setType("UNKNOWN_TYPE");
        Method method = SyncManager.class.getDeclaredMethod("executeByType", PendingOperation.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(sm, op);
        assertTrue(result);
        verify(pendingOperationDao).deleteById(99L);
    }

    @Test
    public void nullIfEmpty_emptyString_returnsNull() throws Exception {
        SyncManager sm = new SyncManager(context);
        Method method = SyncManager.class.getDeclaredMethod("nullIfEmpty", String.class);
        method.setAccessible(true);
        assertNull(method.invoke(sm, ""));
    }

    @Test
    public void nullIfEmpty_nonEmpty_returnsSame() throws Exception {
        SyncManager sm = new SyncManager(context);
        Method method = SyncManager.class.getDeclaredMethod("nullIfEmpty", String.class);
        method.setAccessible(true);
        assertEquals("test", method.invoke(sm, "test"));
    }

    @Test
    public void nullIfEmpty_null_returnsNull() throws Exception {
        SyncManager sm = new SyncManager(context);
        Method method = SyncManager.class.getDeclaredMethod("nullIfEmpty", String.class);
        method.setAccessible(true);
        assertNull(method.invoke(sm, (String) null));
    }

    @Test
    public void getPendingCount_returnsCorrectValue() throws Exception {
        when(pendingOperationDao.getPendingCount()).thenReturn(7);
        SyncManager sm = new SyncManager(context);
        assertEquals(7, sm.getPendingCount());
    }

    @Test
    public void isSyncing_initiallyFalse() throws Exception {
        SyncManager sm = new SyncManager(context);
        assertFalse(sm.isSyncing());
    }

    @Test
    public void syncLocalToServer_syncsMedicinesSymptomsProfile() throws Exception {
        CachedMedicine med = new CachedMedicine();
        med.setMedicineId(100L);
        med.setExpirationDate("2025-12-31");
        med.setQuantity("5");

        CachedSymptom sym = new CachedSymptom();
        sym.setSymptom("Headache");

        CachedProfile profile = new CachedProfile();
        profile.setName("Ivan");
        profile.setAge(30);
        profile.setAllergies("none");
        profile.setContraindications("none");
        profile.setNotes("notes");

        when(cachedMedicineDao.getMedicines(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(Arrays.asList(med));
        when(cachedSymptomDao.getSymptoms(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(Arrays.asList(sym));
        when(cachedProfileDao.getProfile(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(profile);

        Call medCall = mock(Call.class);
        when(api.addMedicine(eq(42L), any())).thenReturn(medCall);
        when(medCall.execute()).thenReturn(Response.success(new PatientMedicineResponse()));

        Call symCall = mock(Call.class);
        when(api.addSymptom(eq(42L), any())).thenReturn(symCall);
        when(symCall.execute()).thenReturn(Response.success(new PatientSymptomResponse()));

        Call profileCall = mock(Call.class);
        when(api.updateProfile(eq(42L), any())).thenReturn(profileCall);
        when(profileCall.execute()).thenReturn(Response.success(new PatientProfileResponse()));

        LocalSyncHelper.syncLocalToServer(context, 42L);

        verify(api, timeout(5000)).addMedicine(eq(42L), any());
        verify(api, timeout(5000)).addSymptom(eq(42L), any());
        verify(api, timeout(5000)).updateProfile(eq(42L), any());
        verify(cachedMedicineDao, timeout(5000)).deleteByPatientId(NetworkModule.LOCAL_PATIENT_ID);
        verify(cachedSymptomDao, timeout(5000)).deleteByPatientId(NetworkModule.LOCAL_PATIENT_ID);
        verify(cachedProfileDao, timeout(5000)).insert(profile);
    }

    @Test
    public void syncLocalToServer_handlesNullMedicineId_skips() throws Exception {
        CachedMedicine med = new CachedMedicine();
        med.setMedicineId(null);

        when(cachedMedicineDao.getMedicines(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(Arrays.asList(med));
        when(cachedSymptomDao.getSymptoms(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(Collections.emptyList());
        when(cachedProfileDao.getProfile(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(null);

        LocalSyncHelper.syncLocalToServer(context, 42L);

        verify(cachedMedicineDao, timeout(5000)).deleteByPatientId(NetworkModule.LOCAL_PATIENT_ID);
        verify(api, never()).addMedicine(anyLong(), any());
    }

    @Test
    public void syncLocalToServer_handlesNullProfile_skipsProfileSync() throws Exception {
        when(cachedMedicineDao.getMedicines(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(Collections.emptyList());
        when(cachedSymptomDao.getSymptoms(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(Collections.emptyList());
        when(cachedProfileDao.getProfile(NetworkModule.LOCAL_PATIENT_ID)).thenReturn(null);

        LocalSyncHelper.syncLocalToServer(context, 42L);

        verify(cachedSymptomDao, timeout(5000)).deleteByPatientId(NetworkModule.LOCAL_PATIENT_ID);
        verify(api, never()).updateProfile(anyLong(), any());
        verify(cachedProfileDao, never()).insert(any(CachedProfile.class));
    }

    @Test
    public void syncLocalToServer_handlesExceptionsGracefully() throws Exception {
        when(cachedMedicineDao.getMedicines(anyLong())).thenThrow(new RuntimeException("DB error"));
        LocalSyncHelper.syncLocalToServer(context, 42L);
        verify(cachedMedicineDao, timeout(5000)).getMedicines(anyLong());
    }
}
