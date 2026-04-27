package com.example.pillulkin.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.Reminder;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            List<Reminder> reminders = PillulkinDatabase.getDatabase(context)
                    .reminderDao().getEnabled();
            for (Reminder reminder : reminders) {
                ReminderScheduler.schedule(context, reminder);
            }
        });
    }
}
