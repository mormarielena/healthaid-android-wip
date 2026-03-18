package com.example.healthaid;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderFragment extends Fragment
        implements ReminderAdapter.OnReminderActionListener {

    private RecyclerView         recyclerView;
    private ReminderAdapter      adapter;
    private List<PillReminder>   reminderList;
    private ProgressBar          progressBar;
    private TextView             textViewEmpty;

    private FirebaseFirestore    db;
    private String               userId;
    private ListenerRegistration listenerReg;

    private String selectedTime = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        recyclerView  = view.findViewById(R.id.recyclerViewReminders);
        progressBar   = view.findViewById(R.id.progressBarReminders);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddReminder);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        reminderList = new ArrayList<>();
        adapter      = new ReminderAdapter(reminderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        ReminderReceiver.createChannelIfNeeded(requireContext());

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
                .document(userId)
                .collection("medications")
                .whereEqualTo("isActive", true)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Failed to load reminders: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
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

                    adapter.notifyDataSetChanged();
                    textViewEmpty.setVisibility(
                            reminderList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // ─── Add reminder dialog ─────────────────────────────────────────────────

    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_reminder, null);

        TextInputEditText editPillName = dialogView.findViewById(R.id.editDialogPillName);
        TextInputEditText editDosage   = dialogView.findViewById(R.id.editDialogDosage);
        TextInputEditText editUnit     = dialogView.findViewById(R.id.editDialogUnit);
        TextView          textPickTime = dialogView.findViewById(R.id.textPickTime);

        selectedTime = "";

        textPickTime.setOnClickListener(v ->
                new TimePickerDialog(getContext(), (picker, hour, minute) -> {
                    String amPm       = hour < 12 ? "AM" : "PM";
                    int    displayHour = hour % 12;
                    if (displayHour == 0) displayHour = 12;
                    selectedTime = String.format(Locale.getDefault(),
                            "%02d:%02d %s", displayHour, minute, amPm);
                    textPickTime.setText(selectedTime);
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

    // ─── Save to Firestore + schedule alarm ───────────────────────────────────

    private void saveReminder(PillReminder reminder) {
        db.collection("users")
                .document(userId)
                .collection("medications")
                .add(reminder)
                .addOnSuccessListener(ref -> {
                    reminder.setId(ref.getId());
                    ReminderScheduler.schedule(requireContext(), reminder);
                    Toast.makeText(getContext(),
                            "Reminder set for " + reminder.getTime(),
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to save: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ─── Taken toggle ─────────────────────────────────────────────────────────

    @Override
    public void onTakenToggled(PillReminder reminder, boolean taken) {
        if (reminder.getId() == null) return;
        db.collection("users").document(userId)
                .collection("medications").document(reminder.getId())
                .update("taken", taken)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ─── Delete + cancel alarm ────────────────────────────────────────────────

    @Override
    public void onDeleteClicked(PillReminder reminder) {
        if (reminder.getId() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Remove reminder")
                .setMessage("Remove " + reminder.getPillName() + "?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    ReminderScheduler.cancel(requireContext(), reminder);
                    db.collection("users").document(userId)
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