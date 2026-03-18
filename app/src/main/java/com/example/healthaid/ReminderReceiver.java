package com.example.healthaid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_PILL_NAME   = "pill_name";
    public static final String EXTRA_DOSAGE      = "dosage";
    public static final String EXTRA_REMINDER_ID = "reminder_id";

    static final String CHANNEL_ID   = "pill_reminders";
    static final String CHANNEL_NAME = "Pill reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String pillName   = intent.getStringExtra(EXTRA_PILL_NAME);
        String dosage     = intent.getStringExtra(EXTRA_DOSAGE);
        String reminderId = intent.getStringExtra(EXTRA_REMINDER_ID);

        if (pillName == null) return;

        createChannelIfNeeded(context);

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openApp.putExtra("open_tab", "reminders");

        PendingIntent tapIntent = PendingIntent.getActivity(
                context,
                reminderId != null ? reminderId.hashCode() : 0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String contentText = (dosage != null && !dosage.trim().isEmpty())
                ? "Time to take " + dosage + " of " + pillName
                : "Time to take " + pillName;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle("Pill reminder — " + pillName)
                        .setContentText(contentText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setAutoCancel(true)
                        .setContentIntent(tapIntent)
                        .setVibrate(new long[]{0, 300, 200, 300});

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Unique notification ID per reminder so each pill gets its own notification
        int notifId = reminderId != null
                ? reminderId.hashCode()
                : (int) System.currentTimeMillis();

        nm.notify(notifId, builder.build());
    }


    static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily pill reminders from HealthAid");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 300, 200, 300});

            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }
}