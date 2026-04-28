package com.example.pillulkin.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.pillulkin.data.local.entity.Reminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReminderSchedulerTest {

    private Context context;
    private AlarmManager alarmManager;
    private MockedStatic<PendingIntent> pendingIntentMock;
    private MockedConstruction<Intent> intentConstruction;
    private PendingIntent mockPendingIntent;

    @Before
    public void setUp() {
        context = mock(Context.class);
        alarmManager = mock(AlarmManager.class);
        when(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager);
        when(context.getApplicationContext()).thenReturn(context);

        pendingIntentMock = mockStatic(PendingIntent.class);
        mockPendingIntent = mock(PendingIntent.class);
        pendingIntentMock.when(() -> PendingIntent.getBroadcast(
                any(Context.class), anyInt(), any(Intent.class), anyInt()))
                .thenReturn(mockPendingIntent);

        intentConstruction = mockConstruction(Intent.class);
    }

    @After
    public void tearDown() {
        pendingIntentMock.close();
        intentConstruction.close();
    }

    private Reminder createReminder(int id, int hour, int minute, String name, boolean enabled) {
        Reminder r = new Reminder();
        r.setId(id);
        r.setHour(hour);
        r.setMinute(minute);
        r.setMedicineName(name);
        r.setEnabled(enabled);
        return r;
    }

    @Test
    public void schedule_enabled_setsAlarm() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", true);
        ReminderScheduler.schedule(context, reminder);
        verify(alarmManager).setExactAndAllowWhileIdle(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(mockPendingIntent));
    }

    @Test
    public void schedule_disabled_doesNothing() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", false);
        ReminderScheduler.schedule(context, reminder);
        verify(alarmManager, never()).setExactAndAllowWhileIdle(
                anyInt(), anyLong(), any(PendingIntent.class));
    }

    @Test
    public void cancel_cancelsPendingIntent() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", true);
        ReminderScheduler.cancel(context, reminder);
        verify(alarmManager).cancel(mockPendingIntent);
    }

    @Test
    public void reschedule_enabled_cancelsAndSchedules() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", true);
        ReminderScheduler.reschedule(context, reminder);
        verify(alarmManager).cancel(mockPendingIntent);
        verify(alarmManager).setExactAndAllowWhileIdle(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(mockPendingIntent));
    }

    @Test
    public void reschedule_disabled_cancelsOnly() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", false);
        ReminderScheduler.reschedule(context, reminder);
        verify(alarmManager).cancel(mockPendingIntent);
        verify(alarmManager, never()).setExactAndAllowWhileIdle(
                anyInt(), anyLong(), any(PendingIntent.class));
    }

    @Test
    public void schedule_usesCorrectPendingIntentFlags() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", true);
        ReminderScheduler.schedule(context, reminder);
        pendingIntentMock.verify(() -> PendingIntent.getBroadcast(
                eq(context), eq(1), any(Intent.class),
                eq(PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)));
    }

    @Test
    public void cancel_usesCorrectPendingIntentFlags() {
        Reminder reminder = createReminder(5, 10, 0, "Nurofen", true);
        ReminderScheduler.cancel(context, reminder);
        pendingIntentMock.verify(() -> PendingIntent.getBroadcast(
                eq(context), eq(5), any(Intent.class),
                eq(PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)));
    }

    @Test
    public void schedule_securityException_fallsBack() {
        Reminder reminder = createReminder(1, 8, 30, "Aspirin", true);
        doThrow(new SecurityException()).when(alarmManager)
                .setExactAndAllowWhileIdle(anyInt(), anyLong(), any(PendingIntent.class));
        ReminderScheduler.schedule(context, reminder);
        verify(alarmManager).setAndAllowWhileIdle(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(mockPendingIntent));
    }

    @Test
    public void schedule_withAllFields_usesReminderData() {
        Reminder reminder = createReminder(42, 14, 45, "Ibuprofen", true);
        ReminderScheduler.schedule(context, reminder);
        pendingIntentMock.verify(() -> PendingIntent.getBroadcast(
                eq(context), eq(42), any(Intent.class), anyInt()));
        verify(alarmManager).setExactAndAllowWhileIdle(
                eq(AlarmManager.RTC_WAKEUP), anyLong(), eq(mockPendingIntent));
    }
}
