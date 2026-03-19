package com.example.healthaid.fragments;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthaid.R;
import com.example.healthaid.adapters.ReminderAdapter;
import com.example.healthaid.utils.ReminderScheduler;
import com.example.healthaid.utils.SessionManager;
import com.example.healthaid.models.PillReminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReminderFragment extends Fragment
        implements ReminderAdapter.OnReminderActionListener {

    private RecyclerView         recyclerView;
    private ReminderAdapter      adapter;
    private List<PillReminder>   reminderList;
    private ProgressBar          progressBar;
    private TextView             textViewEmpty;
    private TextView             textViewTitle;
    private FloatingActionButton fabAdd;

    private FirebaseFirestore    db;
    private ListenerRegistration listenerReg;
    private String               selectedTime = "";

    // The UID whose data we load — own UID or patient UID in caregiver mode
    private String activeUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        recyclerView   = view.findViewById(R.id.recyclerViewReminders);
        progressBar    = view.findViewById(R.id.progressBarReminders);
        textViewEmpty  = view.findViewById(R.id.textViewEmpty);
        textViewTitle  = view.findViewById(R.id.textViewTitle);
        fabAdd         = view.findViewById(R.id.fabAddReminder);

        db           = FirebaseFirestore.getInstance();
        activeUserId = SessionManager.get().getActiveUserId();

        reminderList = new ArrayList<>();
        adapter      = new ReminderAdapter(reminderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // ── Caregiver mode adjustments ────────────────────────────────────────
        SessionManager session = SessionManager.get();
        if (session.isCaregiverMode()) {
            String firstName = session.getPatientName() != null
                    ? session.getPatientName().split("\\s+")[0] : "Patient";
            textViewTitle.setText(firstName + "'s pills");

            fabAdd.setVisibility(session.canEdit() ? View.VISIBLE : View.GONE);
        } else {
            textViewTitle.setText("Your daily pills");
            fabAdd.setVisibility(View.VISIBLE);
        }

        fabAdd.setOnClickListener(v -> showAddReminderDialog());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForReminders();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerReg != null) listenerReg.remove();
    }

    // ─── Firestore real-time listener ────────────────────────────────────────

    private void listenForReminders() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);

        listenerReg = db.collection("users")
                .document(activeUserId)
                .collection("medications")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((snapshots, error) -> {

                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Failed to load reminders: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    reminderList.clear();
                    if (snapshots != null) {
                        for (var doc : snapshots.getDocuments()) {
                            PillReminder r = doc.toObject(PillReminder.class);
                            if (r != null) {
                                r.setId(doc.getId());
                                reminderList.add(r);
                            }
                        }
                    }

                    reminderList.sort((a, b) ->
                            a.getPillName().compareToIgnoreCase(b.getPillName()));

                    adapter.notifyDataSetChanged();
                    textViewEmpty.setVisibility(
                            reminderList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // ─── Add reminder dialog ─────────────────────────────────────────────────

    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_reminder, null);

        TextInputEditText editPillName    = dialogView.findViewById(R.id.editDialogPillName);
        TextInputEditText editDosage      = dialogView.findViewById(R.id.editDialogDosage);
        TextInputEditText editUnit        = dialogView.findViewById(R.id.editDialogUnit);
        View              rowPickTime     = dialogView.findViewById(R.id.rowPickTime);
        TextView          textTimeDisplay = dialogView.findViewById(R.id.textDialogTimeDisplay);

        selectedTime = "";

        rowPickTime.setOnClickListener(v ->
                new TimePickerDialog(getContext(), (picker, hour, minute) -> {
                    String amPm        = hour < 12 ? "AM" : "PM";
                    int    displayHour = hour % 12;
                    if (displayHour == 0) displayHour = 12;
                    selectedTime = String.format(Locale.US,
                            "%02d:%02d %s", displayHour, minute, amPm);
                    textTimeDisplay.setText(selectedTime);
                }, 8, 0, false).show()
        );

        new AlertDialog.Builder(getContext())
                .setTitle("Add pill reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name   = editPillName.getText().toString().trim();
                    String dosage = editDosage.getText().toString().trim();
                    String unit   = editUnit.getText().toString().trim();

                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(getContext(),
                                "Pill name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(selectedTime)) {
                        Toast.makeText(getContext(),
                                "Please pick a time", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveReminder(new PillReminder(name, dosage, unit, selectedTime));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Save reminder ────────────────────────────────────────────────────────

    private void saveReminder(PillReminder reminder) {
        db.collection("users")
                .document(activeUserId)
                .collection("medications")
                .add(reminder)
                .addOnSuccessListener(ref -> {
                    reminder.setId(ref.getId());
                    // Only schedule alarm for own account
                    if (!SessionManager.get().isCaregiverMode()) {
                        ReminderScheduler.schedule(requireContext(), reminder);
                    }
                    Toast.makeText(getContext(),
                            "Reminder set for " + reminder.getTime(),
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to save: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ─── Taken toggle ─────────────────────────────────────────────────────────

    @Override
    public void onTakenToggled(PillReminder reminder, boolean taken) {
        if (reminder.getId() == null) return;
        if (!SessionManager.get().canEdit()) return;  // view-only caregivers blocked

        var medRef = db.collection("users").document(activeUserId)
                .collection("medications").document(reminder.getId());

        if (taken) {
            db.runTransaction((Transaction.Function<Void>) transaction -> {
                var snap          = transaction.get(medRef);
                long remaining    = snap.getLong("pillsRemaining") != null
                        ? snap.getLong("pillsRemaining") : 0;
                long newRemaining = remaining > 0 ? remaining - 1 : 0;

                Map<String, Object> updates = new HashMap<>();
                updates.put("taken",         true);
                updates.put("takenDate",      PillReminder.today());
                updates.put("pillsRemaining", newRemaining);
                transaction.update(medRef, updates);
                return null;
            }).addOnFailureListener(e ->
                    Toast.makeText(getContext(),
                            "Could not update: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("taken",     false);
            updates.put("takenDate", "");
            medRef.update(updates)
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Could not update: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    public void onDeleteClicked(PillReminder reminder) {
        if (reminder.getId() == null) return;
        if (!SessionManager.get().canEdit()) {
            Toast.makeText(getContext(),
                    "You have view-only access", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Remove reminder")
                .setMessage("Remove " + reminder.getPillName() + "?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    if (!SessionManager.get().isCaregiverMode()) {
                        ReminderScheduler.cancel(requireContext(), reminder);
                    }
                    db.collection("users").document(activeUserId)
                            .collection("medications").document(reminder.getId())
                            .update("isActive", false)
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "Could not remove: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}