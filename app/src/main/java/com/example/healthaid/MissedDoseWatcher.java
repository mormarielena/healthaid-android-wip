package com.example.healthaid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Started in MainActivity for logged-in patients.
 * Watches users/{uid}/notifications for new missed-dose nudges
 * written by a caregiver, and fires a local notification when one arrives.
 */
public class MissedDoseWatcher {

    private static final String CHANNEL_ID = "missed_dose_alerts";
    private static ListenerRegistration listenerReg;

    public static void start(Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        createChannel(context);

        listenerReg = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("notifications")
                .whereEqualTo("delivered", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    for (var doc : snapshots.getDocuments()) {
                        String pillName = doc.getString("pillName");
                        String message  = doc.getString("message");
                        if (pillName == null) continue;

                        fireNotification(context, pillName,
                                message != null ? message
                                        : "Your caregiver is checking — did you take " + pillName + "?");

                        // Mark delivered so it doesn't fire again
                        doc.getReference().update("delivered", true);
                    }
                });
    }

    public static void stop() {
        if (listenerReg != null) listenerReg.remove();
    }

    // ─── Write a nudge from caregiver side ────────────────────────────────────

    /**
     * Called by CaregiverReminderFragment when caregiver taps "Send reminder".
     * Writes to the PATIENT's notifications subcollection.
     */
    public static void sendNudge(String patientUserId, String pillName) {
        java.util.Map<String, Object> nudge = new java.util.HashMap<>();
        nudge.put("pillName",  pillName);
        nudge.put("message",   "Reminder from your caregiver: time to take " + pillName);
        nudge.put("delivered", false);
        nudge.put("sentAt",    com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users").document(patientUserId)
                .collection("notifications")
                .add(nudge);
    }

    // ─── Notification helpers ─────────────────────────────────────────────────

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Missed dose alerts",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Nudges from your caregiver");
            NotificationManager nm =
                    context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private static void fireNotification(Context context,
                                         String pillName, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_tab", "reminders");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(context, pillName.hashCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_popup_reminder)
                        .setContentTitle("Pill reminder from caregiver")
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .setVibrate(new long[]{0, 300, 200, 300});

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(pillName.hashCode(), builder.build());
    }
}