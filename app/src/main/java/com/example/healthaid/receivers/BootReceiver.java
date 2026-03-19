package com.example.healthaid.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.healthaid.utils.ReminderScheduler;
import com.example.healthaid.models.PillReminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Re-read active reminders from Firestore and reschedule every one.
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("medications")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (var doc : snapshots.getDocuments()) {
                        PillReminder reminder = doc.toObject(PillReminder.class);
                        if (reminder != null) {
                            reminder.setId(doc.getId());
                            ReminderScheduler.schedule(context, reminder);
                        }
                    }
                });
    }
}