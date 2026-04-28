package com.example.pillulkin.ui.patient;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.ReminderDao;
import com.example.pillulkin.data.local.entity.Reminder;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.PillulkinApi;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.notifications.ReminderScheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;

import retrofit2.Call;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NotificationsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private Application application;
    @Mock private Context context;
    @Mock private SharedPreferences prefs;
    @Mock private SharedPreferences.Editor editor;
    @Mock private PillulkinApi api;
    @Mock private PillulkinDatabase db;
    @Mock private ReminderDao reminderDao;
    @Mock private LiveData<List<Reminder>> remindersLiveData;
    @Mock private ExecutorService mockExecutor;
    @Mock private Call<List<PatientMedicineResponse>> medicinesCall;

    private MockedStatic<PillulkinDatabase> dbStatic;
    private MockedStatic<ReminderScheduler> schedulerStatic;
    private AutoCloseable mocks;
    private NotificationsViewModel viewModel;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        resetNetworkModule();

        when(application.getApplicationContext()).thenReturn(context);
        when(context.getApplicationContext()).thenReturn(context);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.remove(anyString())).thenReturn(editor);
        when(prefs.getLong(eq("patient_id"), anyLong())).thenReturn(1L);
        when(prefs.getString(eq("patient_token"), any())).thenReturn("token");
        when(prefs.getBoolean(eq("local_mode"), anyBoolean())).thenReturn(false);

        dbStatic = mockStatic(PillulkinDatabase.class);
        dbStatic.when(() -> PillulkinDatabase.getDatabase(any(Context.class))).thenReturn(db);
        when(db.reminderDao()).thenReturn(reminderDao);
        when(reminderDao.getAll()).thenReturn(remindersLiveData);
        when(reminderDao.insert(any(Reminder.class))).thenReturn(1L);

        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        }).when(mockExecutor).execute(any(Runnable.class));

        replaceStaticFinalField(PillulkinDatabase.class, "databaseWriteExecutor", mockExecutor);

        schedulerStatic = mockStatic(ReminderScheduler.class);

        NetworkModule instance = NetworkModule.getInstance(context);
        Field apiField = NetworkModule.class.getDeclaredField("api");
        apiField.setAccessible(true);
        apiField.set(instance, api);

        when(api.getMedicines(anyLong())).thenReturn(medicinesCall);
    }

    private static void replaceStaticFinalField(Class<?> clazz, String fieldName, Object value) throws Exception {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object unsafe = theUnsafeField.get(null);
        Field targetField = clazz.getDeclaredField(fieldName);
        Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
        Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
        Method putObject = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
        Object base = staticFieldBase.invoke(unsafe, targetField);
        long offset = (Long) staticFieldOffset.invoke(unsafe, targetField);
        putObject.invoke(unsafe, base, offset, value);
    }

    @After
    public void tearDown() throws Exception {
        dbStatic.close();
        schedulerStatic.close();
        resetNetworkModule();
        mocks.close();
    }

    private void resetNetworkModule() throws Exception {
        Field f = NetworkModule.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    @Test
    public void getReminders_returnsLiveDataFromDao() {
        viewModel = new NotificationsViewModel(application);
        assertSame(remindersLiveData, viewModel.getReminders());
    }

    @Test
    public void getMedicines_returnsMedicinesLiveData() {
        viewModel = new NotificationsViewModel(application);
        assertNotNull(viewModel.getMedicines());
        assertNull(viewModel.getMedicines().getValue());
    }

    @Test
    public void addReminder_insertsViaDaoAndSchedules() {
        viewModel = new NotificationsViewModel(application);
        viewModel.addReminder(8, 30, "Aspirin", "Take with food");

        ArgumentCaptor<Reminder> captor = ArgumentCaptor.forClass(Reminder.class);
        verify(reminderDao).insert(captor.capture());
        Reminder inserted = captor.getValue();
        assertEquals(8, inserted.getHour());
        assertEquals(30, inserted.getMinute());
        assertEquals("Aspirin", inserted.getMedicineName());
        assertEquals("Take with food", inserted.getCustomText());
        assertTrue(inserted.isEnabled());
        assertEquals(1, inserted.getId());

        schedulerStatic.verify(() -> ReminderScheduler.schedule(eq(application), any(Reminder.class)));
    }

    @Test
    public void updateReminder_updatesFieldsAndCallsDao() {
        viewModel = new NotificationsViewModel(application);
        Reminder reminder = new Reminder();
        reminder.setId(1);
        reminder.setHour(8);
        reminder.setMinute(30);
        reminder.setEnabled(true);

        viewModel.updateReminder(reminder, 14, 0, "Ibuprofen", "After meal");

        assertEquals(14, reminder.getHour());
        assertEquals(0, reminder.getMinute());
        assertEquals("Ibuprofen", reminder.getMedicineName());
        assertEquals("After meal", reminder.getCustomText());
        verify(reminderDao).update(reminder);
        schedulerStatic.verify(() -> ReminderScheduler.cancel(eq(application), eq(reminder)));
        schedulerStatic.verify(() -> ReminderScheduler.schedule(eq(application), eq(reminder)));
    }

    @Test
    public void toggleReminder_flipsEnabledAndCallsDao() {
        viewModel = new NotificationsViewModel(application);
        Reminder reminder = new Reminder();
        reminder.setId(1);
        reminder.setEnabled(true);

        viewModel.toggleReminder(reminder);

        assertFalse(reminder.isEnabled());
        verify(reminderDao).update(reminder);
        schedulerStatic.verify(() -> ReminderScheduler.reschedule(eq(application), eq(reminder)));
    }

    @Test
    public void toggleReminder_enablesWhenDisabled() {
        viewModel = new NotificationsViewModel(application);
        Reminder reminder = new Reminder();
        reminder.setId(2);
        reminder.setEnabled(false);

        viewModel.toggleReminder(reminder);

        assertTrue(reminder.isEnabled());
        verify(reminderDao).update(reminder);
        schedulerStatic.verify(() -> ReminderScheduler.reschedule(eq(application), eq(reminder)));
    }

    @Test
    public void deleteReminder_callsDaoDeleteAndCancels() {
        viewModel = new NotificationsViewModel(application);
        Reminder reminder = new Reminder();
        reminder.setId(3);

        viewModel.deleteReminder(reminder);

        verify(reminderDao).delete(reminder);
        schedulerStatic.verify(() -> ReminderScheduler.cancel(eq(application), eq(reminder)));
    }
}
