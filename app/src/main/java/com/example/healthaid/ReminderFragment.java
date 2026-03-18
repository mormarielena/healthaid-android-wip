package com.example.healthaid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ReminderFragment extends Fragment implements ReminderAdapter.OnReminderActionListener {

    private RecyclerView          recyclerView;
    private ReminderAdapter       adapter;
    private List<PillReminder>    reminderList;
    private ProgressBar           progressBar;
    private TextView              textViewEmpty;

    private FirebaseFirestore     db;
    private String                userId;
    private ListenerRegistration  listenerReg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        // Views
        recyclerView  = view.findViewById(R.id.recyclerViewReminders);
        progressBar   = view.findViewById(R.id.progressBarReminders);
        textViewEmpty = view.findViewById(R.id.textViewEmpty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddReminder);

        // Firebase
        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // RecyclerView setup
        reminderList = new ArrayList<>();
        adapter      = new ReminderAdapter(reminderList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

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
        if (listenerReg != null) {
            listenerReg.remove();
        }
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
                            PillReminder reminder = doc.toObject(PillReminder.class);
                            if (reminder != null) {
                                reminder.setId(doc.getId());
                                reminderList.add(reminder);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    textViewEmpty.setVisibility(reminderList.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    // ─── Add reminder dialog ──────────────────────────────────────────────────

    private void showAddReminderDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_reminder, null);

        EditText editPillName = dialogView.findViewById(R.id.editDialogPillName);
        EditText editDosage   = dialogView.findViewById(R.id.editDialogDosage);
        EditText editUnit     = dialogView.findViewById(R.id.editDialogUnit);
        EditText editTime     = dialogView.findViewById(R.id.editDialogTime);

        new AlertDialog.Builder(getContext())
                .setTitle("Add pill reminder")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name   = editPillName.getText().toString().trim();
                    String dosage = editDosage.getText().toString().trim();
                    String unit   = editUnit.getText().toString().trim();
                    String time   = editTime.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(time)) {
                        Toast.makeText(getContext(),
                                "Pill name and time are required",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveReminder(new PillReminder(name, dosage, unit, time));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Firestore write: add ─────────────────────────────────────────────────

    private void saveReminder(PillReminder reminder) {
        db.collection("users")
                .document(userId)
                .collection("medications")
                .add(reminder)
                .addOnSuccessListener(ref ->
                        Toast.makeText(getContext(), "Reminder added!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to save: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ─── Firestore write: toggle taken ────────────────────────────────────────

    @Override
    public void onTakenToggled(PillReminder reminder, boolean taken) {
        if (reminder.getId() == null) return;

        DocumentReference ref = db.collection("users")
                .document(userId)
                .collection("medications")
                .document(reminder.getId());

        ref.update("taken", taken)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not update: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ─── Firestore write: soft delete ─────────────────────────────────────────

    @Override
    public void onDeleteClicked(PillReminder reminder) {
        if (reminder.getId() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Remove reminder")
                .setMessage("Remove " + reminder.getPillName() + "?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    db.collection("users")
                            .document(userId)
                            .collection("medications")
                            .document(reminder.getId())
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