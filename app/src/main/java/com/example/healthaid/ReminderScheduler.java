package com.example.healthaid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class ReminderScheduler {

    // ─── Schedule a daily repeating alarm ────────────────────────────────────

    /** PILL REMINDER NOTIFICATION
     * Parses the time string (e.g. "08:00 AM" or "14:30"), sets the alarm
     * for that time today — or tomorrow if it has already passed — and
     * repeats every 24 hours until cancelled.
     */
    public static void schedule(Context context, PillReminder reminder) {
        if (reminder.getId() == null || reminder.getTime() == null) return;

        int[] hm = parseTime(reminder.getTime());
        if (hm == null) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hm[0]);
        cal.set(Calendar.MINUTE,      hm[1]);
        cal.set(Calendar.SECOND,      0);
        cal.set(Calendar.MILLISECOND, 0);

        // Already passed today → push to tomorrow
        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context, reminder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            } else {

                am.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            }
        } else {
            am.setRepeating(AlarmManager.RTC_WAKEUP,
                    cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        }
    }

    // ─── Cancel an alarm ─────────────────────────────────────────────────────

    public static void cancel(Context context, PillReminder reminder) {
        if (reminder.getId() == null) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(buildPendingIntent(context, reminder));
    }


    private static PendingIntent buildPendingIntent(Context context, PillReminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_PILL_NAME,    reminder.getPillName());
        intent.putExtra(ReminderReceiver.EXTRA_DOSAGE,
                reminder.getDosage() + " " + reminder.getUnit());
        intent.putExtra(ReminderReceiver.EXTRA_REMINDER_ID,  reminder.getId());

        int requestCode = reminder.getId().hashCode();

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }


    public static int[] parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            timeStr = timeStr.trim().toUpperCase();
            boolean isPm = timeStr.contains("PM");
            boolean isAm = timeStr.contains("AM");
            timeStr = timeStr.replace("AM", "").replace("PM", "").trim();

            String[] parts = timeStr.split(":");
            int hour   = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());

            if (isAm && hour == 12) hour = 0;
            if (isPm && hour != 12) hour += 12;

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
            return new int[]{hour, minute};
        } catch (Exception e) {
            return null;
        }
    }
}