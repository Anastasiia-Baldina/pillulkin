package com.example.pillulkin.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NetworkModuleTest {

    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;

    private NetworkModule networkModule;
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
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);

        networkModule = NetworkModule.getInstance(context);
    }

    @After
    public void tearDown() throws Exception {
        resetNetworkModule();
        mocks.close();
    }

    private void resetNetworkModule() throws Exception {
        Field f = NetworkModule.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void testSingletonReturnsSameInstance() {
        NetworkModule a = NetworkModule.getInstance(context);
        NetworkModule b = NetworkModule.getInstance(context);
        assertSame(a, b);
    }

    @Test
    public void testGetPatientIdDefault() {
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        assertEquals(-1L, networkModule.getPatientId());
    }

    @Test
    public void testSavePatientId() {
        networkModule.savePatientId(42L);
        verify(editor).putLong("patient_id", 42L);
    }

    @Test
    public void testIsPatientLoggedInFalse() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(-1L);
        assertFalse(networkModule.isPatientLoggedIn());
    }

    @Test
    public void testIsPatientLoggedInTrue() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(5L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("abc");
        assertTrue(networkModule.isPatientLoggedIn());
    }

    @Test
    public void testIsPatientLoggedInLocalMode() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);
        assertTrue(networkModule.isPatientLoggedIn());
    }

    @Test
    public void testIsDoctorLoggedInFalse() {
        when(prefs.getString(eq("doctor_token"), any())).thenReturn(null);
        assertFalse(networkModule.isDoctorLoggedIn());
    }

    @Test
    public void testIsDoctorLoggedInTrue() {
        when(prefs.getString(eq("doctor_token"), any())).thenReturn("doc_token");
        when(prefs.getLong(eq("doctor_patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getLong(eq("doctor_expires"), anyLong())).thenReturn(Long.MAX_VALUE);
        assertTrue(networkModule.isDoctorLoggedIn());
    }

    @Test
    public void testIsDoctorLoggedInExpired() {
        when(prefs.getString(eq("doctor_token"), any())).thenReturn("doc_token");
        when(prefs.getLong(eq("doctor_patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getLong(eq("doctor_expires"), anyLong())).thenReturn(0L);
        assertFalse(networkModule.isDoctorLoggedIn());
    }

    @Test
    public void testIsGoogleLoggedInTrue() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        assertTrue(networkModule.isGoogleLoggedIn());
    }

    @Test
    public void testIsGoogleLoggedInFalseInLocalMode() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);
        assertFalse(networkModule.isGoogleLoggedIn());
    }

    @Test
    public void testSetLocalMode() {
        networkModule.setLocalMode();
        verify(editor).putBoolean("local_mode", true);
        verify(editor).putLong("patient_id", NetworkModule.LOCAL_PATIENT_ID);
    }

    @Test
    public void testIsLocalMode() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);
        assertTrue(networkModule.isLocalMode());
    }

    @Test
    public void testGetEffectivePatientIdLocal() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(true);
        assertEquals(NetworkModule.LOCAL_PATIENT_ID, networkModule.getEffectivePatientId());
    }

    @Test
    public void testGetEffectivePatientIdRemote() {
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(42L);
        assertEquals(42L, networkModule.getEffectivePatientId());
    }

    @Test
    public void testGenerateLocalIdDecrements() {
        when(prefs.getLong(eq("local_next_id"), anyLong())).thenReturn(-5L);
        long id = networkModule.generateLocalId();
        assertEquals(-6L, id);
        verify(editor).putLong("local_next_id", -6L);
    }

    @Test
    public void testClearPatientSession() {
        networkModule.clearPatientSession();
        verify(editor).remove("patient_id");
        verify(editor).remove("patient_token");
        verify(editor).remove("local_mode");
    }

    @Test
    public void testClearDoctorSession() {
        networkModule.clearDoctorSession();
        verify(editor).remove("doctor_token");
        verify(editor).remove("doctor_patient_id");
        verify(editor).remove("doctor_expires");
    }

    @Test
    public void testLocalDoctorCode() {
        String code = networkModule.generateLocalDoctorCode();
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(Integer.parseInt(code) >= 100000);
        assertTrue(Integer.parseInt(code) <= 999999);
    }

    @Test
    public void testValidateLocalDoctorCodeCorrect() {
        when(prefs.getString(eq("local_doctor_code"), any())).thenReturn("123456");
        assertTrue(networkModule.validateLocalDoctorCode("123456"));
    }

    @Test
    public void testValidateLocalDoctorCodeIncorrect() {
        when(prefs.getString(eq("local_doctor_code"), any())).thenReturn("123456");
        assertFalse(networkModule.validateLocalDoctorCode("654321"));
    }

    @Test
    public void testValidateLocalDoctorCodeNull() {
        when(prefs.getString(eq("local_doctor_code"), any())).thenReturn(null);
        assertFalse(networkModule.validateLocalDoctorCode("123456"));
    }

    @Test
    public void testGetApiNotNull() {
        assertNotNull(networkModule.getApi());
    }

    @Test
    public void testSaveDoctorToken() {
        networkModule.saveDoctorToken("my_token");
        verify(editor).putString("doctor_token", "my_token");
    }

    @Test
    public void testSaveDoctorPatientId() {
        networkModule.saveDoctorPatientId(5L);
        verify(editor).putLong("doctor_patient_id", 5L);
    }

    @Test
    public void testLocalDoctorSession() {
        networkModule.setLocalDoctorSession(true);
        verify(editor).putBoolean("local_doctor_session", true);
    }
}
