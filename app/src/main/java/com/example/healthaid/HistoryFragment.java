package com.example.healthaid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private FirebaseFirestore db;
    private String            userId;

    // Adherence views
    private TextView      textAdherenceScore, textAdherenceDetail, textAdherencePeriod;
    private ProgressBar   progressAdherence;

    // Refill views
    private LinearLayout  containerMedications;
    private ProgressBar   progressRefill;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        textAdherenceScore  = view.findViewById(R.id.textAdherenceScore);
        textAdherenceDetail = view.findViewById(R.id.textAdherenceDetail);
        textAdherencePeriod = view.findViewById(R.id.textAdherencePeriod);
        progressAdherence   = view.findViewById(R.id.progressAdherence);
        containerMedications = view.findViewById(R.id.containerMedications);
        progressRefill      = view.findViewById(R.id.progressRefill);

        loadAdherence();
        loadMedicationsForRefill();

        return view;
    }

    // ─── Adherence — count taken vs total this week ───────────────────────────

    private void loadAdherence() {
        // Window: last 7 days
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -6);
        Date weekAgo = cal.getTime();

        String periodLabel = new SimpleDateFormat("MMM d", Locale.getDefault())
                .format(weekAgo) + " – today";
        textAdherencePeriod.setText(periodLabel);

        // Adherence is calculated from the medications collection directly —
        // simpler and requires no extra index.
        db.collection("users").document(userId)
                .collection("medications")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<com.google.firebase.firestore.DocumentSnapshot> docs =
                            snapshots.getDocuments();

                    int total = docs.size() * 7;   // one dose/day per med × 7 days
                    int taken = 0;

                    // Count how many are currently marked taken
                    // (In a production app you'd use dose_logs with timestamps;
                    //  this is a practical approximation for the demo.)
                    for (var doc : docs) {
                        Boolean isTaken = doc.getBoolean("taken");
                        if (Boolean.TRUE.equals(isTaken)) taken++;
                    }

                    // Show today's snapshot: X of N pills taken today
                    int todayTotal = docs.size();
                    int todayTaken = taken;

                    updateAdherenceUI(todayTaken, todayTotal, taken, total);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not load adherence data", Toast.LENGTH_SHORT).show());
    }

    private void updateAdherenceUI(int todayTaken, int todayTotal,
                                   int weekTaken, int weekTotal) {
        int pct = weekTotal > 0 ? (weekTaken * 100 / weekTotal) : 0;

        textAdherenceScore.setText(pct + "%");
        progressAdherence.setProgress(pct);

        String colour = pct >= 80 ? "Good" : pct >= 50 ? "Moderate" : "Low";
        textAdherenceDetail.setText(
                todayTaken + " of " + todayTotal + " pills taken today  ·  "
                        + colour + " adherence this week");
    }

    // ─── Refill tracking — load all active meds with pill counts ─────────────

    private void loadMedicationsForRefill() {
        progressRefill.setVisibility(View.VISIBLE);

        db.collection("users").document(userId)
                .collection("medications")
                .whereEqualTo("isActive", true)
                .orderBy("pillsRemaining", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressRefill.setVisibility(View.GONE);
                    containerMedications.removeAllViews();

                    if (snapshots.isEmpty()) {
                        View empty = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_empty_state, containerMedications, false);
                        ((TextView) empty.findViewById(R.id.textEmpty))
                                .setText("No medications yet.\nAdd one in the Reminders tab.");
                        containerMedications.addView(empty);
                        return;
                    }

                    for (var doc : snapshots.getDocuments()) {
                        PillReminder med = doc.toObject(PillReminder.class);
                        if (med == null) continue;
                        med.setId(doc.getId());
                        addMedRefillCard(med);
                    }
                })
                .addOnFailureListener(e -> {
                    progressRefill.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Could not load medications", Toast.LENGTH_SHORT).show();
                });
    }

    private void addMedRefillCard(PillReminder med) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_refill_card, containerMedications, false);

        TextView textName       = card.findViewById(R.id.textRefillMedName);
        TextView textCount      = card.findViewById(R.id.textRefillPillCount);
        TextView textWarning    = card.findViewById(R.id.textRefillWarning);
        ProgressBar barStock    = card.findViewById(R.id.barPillStock);
        View btnAddRefill       = card.findViewById(R.id.btnAddRefill);

        textName.setText(med.getPillName());

        int remaining = med.getPillsRemaining();
        int threshold = med.getLowStockThreshold();

        if (remaining == 0) {
            textCount.setText("Not tracked");
            textWarning.setVisibility(View.GONE);
            barStock.setVisibility(View.GONE);
        } else {
            textCount.setText(remaining + " pills remaining");
            barStock.setVisibility(View.VISIBLE);
            // Cap bar at 30 pills for visual purposes
            barStock.setMax(Math.max(30, remaining));
            barStock.setProgress(remaining);

            if (med.isLowStock()) {
                textWarning.setVisibility(View.VISIBLE);
                textWarning.setText("Low stock — refill soon");
            } else {
                textWarning.setVisibility(View.GONE);
            }
        }

        btnAddRefill.setOnClickListener(v -> showRefillDialog(med));

        containerMedications.addView(card);
    }

    // ─── Add refill dialog ────────────────────────────────────────────────────

    private void showRefillDialog(PillReminder med) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_refill, null);

        TextInputEditText editCount    = dialogView.findViewById(R.id.editRefillCount);
        TextInputEditText editPharmacy = dialogView.findViewById(R.id.editRefillPharmacy);
        TextInputEditText editRxRef    = dialogView.findViewById(R.id.editRefillRxRef);
        TextInputEditText editNotes    = dialogView.findViewById(R.id.editRefillNotes);

        new AlertDialog.Builder(getContext())
                .setTitle("Log refill — " + med.getPillName())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String countStr = editCount.getText().toString().trim();
                    if (countStr.isEmpty()) {
                        Toast.makeText(getContext(),
                                "Enter number of pills added", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int pillsAdded  = Integer.parseInt(countStr);
                    String pharmacy = editPharmacy.getText().toString().trim();
                    String rxRef    = editRxRef.getText().toString().trim();
                    String notes    = editNotes.getText().toString().trim();

                    saveRefill(med, pillsAdded, pharmacy, rxRef, notes);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Save refill — transaction updates pill count + writes record ─────────

    private void saveRefill(PillReminder med, int pillsAdded,
                            String pharmacy, String rxRef, String notes) {

        var medRef = db.collection("users").document(userId)
                .collection("medications").document(med.getId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            var snapshot     = transaction.get(medRef);
            long currentCount = snapshot.getLong("pillsRemaining") != null
                    ? snapshot.getLong("pillsRemaining") : 0;
            long newTotal    = currentCount + pillsAdded;

            // Update pill count on the medication
            transaction.update(medRef, "pillsRemaining", newTotal);

            // Write refill record to subcollection
            RefillRecord record = new RefillRecord(
                    med.getId(), med.getPillName(),
                    pillsAdded, (int) newTotal,
                    pharmacy, rxRef, notes);

            var refillRef = medRef.collection("refill_records").document();
            transaction.set(refillRef, record);

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(getContext(),
                    pillsAdded + " pills added to " + med.getPillName(),
                    Toast.LENGTH_SHORT).show();
            loadMedicationsForRefill();   // refresh the list
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(),
                        "Refill save failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }
}