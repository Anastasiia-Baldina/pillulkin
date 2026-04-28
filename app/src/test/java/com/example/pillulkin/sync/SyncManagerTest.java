package com.example.pillulkin.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.PendingOperationDao;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SyncManagerTest {

    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private ConnectivityManager connectivityManager;
    @Mock private NetworkInfo networkInfo;
    @Mock private PillulkinDatabase db;
    @Mock private PendingOperationDao pendingOperationDao;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private SyncManager syncManager;
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

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(networkInfo);
        when(networkInfo.isConnectedOrConnecting()).thenReturn(true);

        when(db.pendingOperationDao()).thenReturn(pendingOperationDao);
        when(pendingOperationDao.getPending()).thenReturn(new java.util.ArrayList<>());
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
    public void testIsOnlineReturnsTrueWhenConnected() {
        syncManager = new SyncManager(context);
        assertTrue(syncManager.isOnline());
    }

    @Test
    public void testIsOnlineReturnsFalseWhenNoConnectivity() {
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(null);
        syncManager = new SyncManager(context);
        assertFalse(syncManager.isOnline());
    }

    @Test
    public void testIsOnlineReturnsFalseWhenCmNull() {
        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(null);
        syncManager = new SyncManager(context);
        assertFalse(syncManager.isOnline());
    }

    @Test
    public void testSyncAllSkipsWhenNotLoggedIn() {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn(null);

        syncManager = new SyncManager(context);
        syncManager.syncAll();
        assertFalse(syncManager.isSyncing());
    }

    @Test
    public void testGetPendingCount() {
        when(pendingOperationDao.getPendingCount()).thenReturn(5);
        syncManager = new SyncManager(context);
        assertEquals(5, syncManager.getPendingCount());
    }

    @Test
    public void testMaxRetriesThreshold() {
        PendingOperation op = new PendingOperation();
        op.setRetryCount(3);
        assertTrue(op.getRetryCount() >= 3);
    }

    @Test
    public void testPendingOperationStatusConstants() {
        assertEquals("PENDING", PendingOperation.STATUS_PENDING);
        assertEquals("SYNCING", PendingOperation.STATUS_SYNCING);
        assertEquals("FAILED", PendingOperation.STATUS_FAILED);
    }

    @Test
    public void testPendingOperationTypeConstants() {
        assertEquals("PROFILE_UPDATE", PendingOperation.TYPE_PROFILE_UPDATE);
        assertEquals("SYMPTOM_ADD", PendingOperation.TYPE_SYMPTOM_ADD);
        assertEquals("SYMPTOM_DELETE", PendingOperation.TYPE_SYMPTOM_DELETE);
        assertEquals("MEDICINE_ADD", PendingOperation.TYPE_MEDICINE_ADD);
        assertEquals("MEDICINE_DELETE", PendingOperation.TYPE_MEDICINE_DELETE);
    }

    @Test
    public void testNullIfEmpty() throws Exception {
        syncManager = new SyncManager(context);
        java.lang.reflect.Method method = SyncManager.class.getDeclaredMethod("nullIfEmpty", String.class);
        method.setAccessible(true);
        assertNull(method.invoke(syncManager, (String) null));
        assertNull(method.invoke(syncManager, ""));
        assertEquals("test", method.invoke(syncManager, "test"));
    }

    @Test
    public void testSyncProfileUpdateParsePayload() throws Exception {
        syncManager = new SyncManager(context);

        Call<PatientProfileResponse> call = mock(Call.class);
        when(api.updateProfile(eq(1L), any())).thenReturn(call);

        PatientProfileResponse resp = new PatientProfileResponse();
        resp.setPatientId(1L);
        when(call.execute()).thenReturn(Response.success(resp));

        java.lang.reflect.Method method = SyncManager.class.getDeclaredMethod("syncProfileUpdate", PendingOperation.class);
        method.setAccessible(true);

        PendingOperation op = new PendingOperation();
        op.setPayload("Ivan|30|none|none|notes");

        assertTrue((boolean) method.invoke(syncManager, op));
    }

    @Test
    public void testSyncSymptomDeleteReturnsTrueOn404() throws Exception {
        syncManager = new SyncManager(context);

        Call<Void> call = mock(Call.class);
        when(api.deleteSymptom(eq(1L), eq(5L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "not found")));

        java.lang.reflect.Method method = SyncManager.class.getDeclaredMethod("syncSymptomDelete", PendingOperation.class);
        method.setAccessible(true);

        PendingOperation op = new PendingOperation();
        op.setPayload("5");

        assertTrue((boolean) method.invoke(syncManager, op));
    }

    @Test
    public void testSyncMedicineDeleteReturnsTrueOn404() throws Exception {
        syncManager = new SyncManager(context);

        Call<Void> call = mock(Call.class);
        when(api.deleteMedicine(eq(1L), eq(10L))).thenReturn(call);
        when(call.execute()).thenReturn(Response.error(404, ResponseBody.create(MediaType.parse("text/plain"), "not found")));

        java.lang.reflect.Method method = SyncManager.class.getDeclaredMethod("syncMedicineDelete", PendingOperation.class);
        method.setAccessible(true);

        PendingOperation op = new PendingOperation();
        op.setPayload("10");

        assertTrue((boolean) method.invoke(syncManager, op));
    }
}
