package com.example.pillulkin.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.Reminder;

import java.util.List;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "pillulkin_reminders";
    private static final String EXTRA_REMINDER_ID = "reminder_id";
    private static final String EXTRA_TEXT = "reminder_text";

    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, -1);
        String text = intent.getStringExtra(EXTRA_TEXT);
        if (reminderId == -1) return;
        if (text == null || text.isEmpty()) text = "Не забудьте принять лекарство";

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel(nm);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        nm.notify(reminderId, builder.build());

        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<Reminder> enabled = PillulkinDatabase.getDatabase(context)
                        .reminderDao().getEnabled();
                for (Reminder r : enabled) {
                    if (r.getId() == reminderId) {
                        ReminderScheduler.schedule(context, r);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    static void createChannel(NotificationManager nm) {
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о лекарствах",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Напоминания о приёме лекарств");
            nm.createNotificationChannel(channel);
        }
    }
}
