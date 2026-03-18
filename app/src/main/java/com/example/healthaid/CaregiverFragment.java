package com.example.healthaid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class CaregiverFragment extends Fragment {

    private FirebaseFirestore    db;
    private String               userId;
    private String               currentUserName;

    private LinearLayout         containerCaregivers;
    private TextView             textViewEmpty;
    private ProgressBar          progressBar;
    private ListenerRegistration listenerReg;

    // Patient pill list section (visible in caregiver mode)
    private LinearLayout         containerPatientPills;
    private TextView             textPatientPillsHeader;
    private ProgressBar          progressPatientPills;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_caregiver, container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        containerCaregivers  = view.findViewById(R.id.containerCaregivers);
        textViewEmpty        = view.findViewById(R.id.textViewCaregiverEmpty);
        progressBar          = view.findViewById(R.id.progressBarCaregiver);
        containerPatientPills = view.findViewById(R.id.containerPatientPills);
        textPatientPillsHeader = view.findViewById(R.id.textPatientPillsHeader);
        progressPatientPills  = view.findViewById(R.id.progressPatientPills);

        FloatingActionButton fab = view.findViewById(R.id.fabAddCaregiver);
        fab.setOnClickListener(v -> loadUserNameThenShowDialog());

        loadCurrentUserName();

        // If currently in caregiver mode, show the patient pill section
        if (SessionManager.get().isCaregiverMode()) {
            showPatientPillSection();
        } else {
            containerPatientPills.setVisibility(View.GONE);
            textPatientPillsHeader.setVisibility(View.GONE);
            progressPatientPills.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForCaregivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerReg != null) listenerReg.remove();
    }

    // ─── Patient pill section (caregiver mode) ────────────────────────────────

    private void showPatientPillSection() {
        SessionManager session = SessionManager.get();
        String patientFirst = session.getPatientName() != null
                ? session.getPatientName().split("\\s+")[0] : "Patient";

        textPatientPillsHeader.setVisibility(View.VISIBLE);
        textPatientPillsHeader.setText(patientFirst + "'s medications today");
        containerPatientPills.setVisibility(View.VISIBLE);
        progressPatientPills.setVisibility(View.VISIBLE);

        db.collection("users").document(session.getPatientUserId())
                .collection("medications")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(snapshots -> {
                    progressPatientPills.setVisibility(View.GONE);
                    containerPatientPills.removeAllViews();

                    if (snapshots.isEmpty()) {
                        TextView empty = new TextView(getContext());
                        empty.setText("No medications found for this patient.");
                        empty.setTextColor(getResources().getColor(R.color.text_gray, null));
                        containerPatientPills.addView(empty);
                        return;
                    }

                    String today = PillReminder.today();
                    for (var doc : snapshots.getDocuments()) {
                        PillReminder med = doc.toObject(PillReminder.class);
                        if (med == null) continue;
                        med.setId(doc.getId());
                        addPatientPillRow(med, today);
                    }
                })
                .addOnFailureListener(e -> {
                    progressPatientPills.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            "Could not load patient's medications",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void addPatientPillRow(PillReminder med, String today) {
        View row = LayoutInflater.from(getContext())
                .inflate(R.layout.item_patient_pill_row, containerPatientPills, false);

        TextView textName   = row.findViewById(R.id.textPatientPillName);
        TextView textTime   = row.findViewById(R.id.textPatientPillTime);
        TextView textStatus = row.findViewById(R.id.textPatientPillStatus);
        View     btnNudge   = row.findViewById(R.id.btnSendReminder);

        textName.setText(med.getPillName());
        textTime.setText(med.getTime());

        boolean takenToday = med.isTakenToday();
        if (takenToday) {
            textStatus.setText("Taken");
            textStatus.setTextColor(getResources().getColor(R.color.mint_green_dark, null));
            btnNudge.setVisibility(View.GONE);   // no need to nudge if already taken
        } else {
            textStatus.setText("Not taken");
            textStatus.setTextColor(0xFFD32F2F);
            btnNudge.setVisibility(View.VISIBLE);
            btnNudge.setOnClickListener(v -> confirmNudge(med));
        }

        containerPatientPills.addView(row);
    }

    private void confirmNudge(PillReminder med) {
        new AlertDialog.Builder(getContext())
                .setTitle("Send reminder")
                .setMessage("Send a notification to your patient to take " + med.getPillName() + "?")
                .setPositiveButton("Send", (dialog, which) -> {
                    MissedDoseWatcher.sendNudge(
                            SessionManager.get().getPatientUserId(),
                            med.getPillName());
                    Toast.makeText(getContext(),
                            "Reminder sent!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ─── Own caregivers list ──────────────────────────────────────────────────

    private void loadCurrentUserName() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) currentUserName = snap.getString("name");
                    if (currentUserName == null || currentUserName.isEmpty())
                        currentUserName = "A HealthAid user";
                });
    }

    private void listenForCaregivers() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);

        listenerReg = db.collection("caregiver_links")
                .whereEqualTo("patientUserId", userId)
                .addSnapshotListener((snapshots, error) -> {
                    progressBar.setVisibility(View.GONE);
                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Failed to load caregivers: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    rebuildCaregiverList(snapshots);
                });
    }

    private void rebuildCaregiverList(QuerySnapshot snapshots) {
        containerCaregivers.removeAllViews();
        if (snapshots == null) { textViewEmpty.setVisibility(View.VISIBLE); return; }

        int added = 0;
        for (var doc : snapshots.getDocuments()) {
            CaregiverLink link = doc.toObject(CaregiverLink.class);
            if (link == null || !link.getIsActive()) continue;
            link.setId(doc.getId());
            addCaregiverCard(link);
            added++;
        }
        textViewEmpty.setVisibility(added == 0 ? View.VISIBLE : View.GONE);
    }

    private void addCaregiverCard(CaregiverLink link) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_caregiver_card, containerCaregivers, false);

        ((TextView) card.findViewById(R.id.textCaregiverName))
                .setText(link.getCaregiverName() != null
                        && !link.getCaregiverName().isEmpty()
                        ? link.getCaregiverName() : "Unknown");
        ((TextView) card.findViewById(R.id.textCaregiverEmail))
                .setText(link.getCaregiverEmail());
        ((TextView) card.findViewById(R.id.textCaregiverPermission))
                .setText(CaregiverLink.PERMISSION_EDITOR.equals(link.getPermissionLevel())
                        ? "Can view & edit" : "View only");

        card.findViewById(R.id.btnRevokeCaregiver)
                .setOnClickListener(v -> confirmRevoke(link));
        containerCaregivers.addView(card);
    }

    // ─── Invite dialog ────────────────────────────────────────────────────────

    private void loadUserNameThenShowDialog() {
        if (currentUserName != null) { showInviteDialog(); return; }
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snap -> {
                    currentUserName = snap.getString("name");
                    if (currentUserName == null) currentUserName = "A HealthAid user";
                    showInviteDialog();
                });
    }

    private void showInviteDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_invite_caregiver, null);
        TextInputEditText editEmail  = dialogView.findViewById(R.id.editCaregiverEmail);
        RadioGroup        radioPerms = dialogView.findViewById(R.id.radioPermissions);

        new AlertDialog.Builder(getContext())
                .setTitle("Invite caregiver")
                .setView(dialogView)
                .setPositiveButton("Send invite", (dialog, which) -> {
                    String email = editEmail.getText().toString().trim().toLowerCase();
                    if (TextUtils.isEmpty(email) || !email.contains("@")) {
                        Toast.makeText(getContext(),
                                "Enter a valid email address", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String permission = radioPerms.getCheckedRadioButtonId() == R.id.radioEditor
                            ? CaregiverLink.PERMISSION_EDITOR
                            : CaregiverLink.PERMISSION_VIEW_ONLY;
                    findUserByEmailAndLink(email, permission);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void findUserByEmailAndLink(String email, String permission) {
        db.collection("users").whereEqualTo("email", email).limit(1).get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        Toast.makeText(getContext(),
                                "No HealthAid account found for " + email
                                        + ". They must register first.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    var    doc           = snapshots.getDocuments().get(0);
                    String caregiverId   = doc.getId();
                    String caregiverName = doc.getString("name");
                    if (caregiverId.equals(userId)) {
                        Toast.makeText(getContext(),
                                "You can't add yourself as a caregiver",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    writeCaregiverLink(caregiverId, caregiverName, email, permission);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Lookup failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void writeCaregiverLink(String caregiverId, String caregiverName,
                                    String caregiverEmail, String permission) {
        CaregiverLink link = new CaregiverLink(userId, caregiverId, caregiverEmail, permission);
        link.setCaregiverName(caregiverName != null ? caregiverName : "");
        link.setPatientName(currentUserName);

        db.collection("caregiver_links").add(link)
                .addOnSuccessListener(ref ->
                        Toast.makeText(getContext(),
                                caregiverEmail + " added as caregiver",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not add caregiver: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void confirmRevoke(CaregiverLink link) {
        String name = (link.getCaregiverName() != null && !link.getCaregiverName().isEmpty())
                ? link.getCaregiverName() : link.getCaregiverEmail();
        new AlertDialog.Builder(getContext())
                .setTitle("Remove caregiver")
                .setMessage("Remove " + name + "'s access?")
                .setPositiveButton("Remove", (dialog, which) ->
                        db.collection("caregiver_links").document(link.getId())
                                .update("isActive", false)
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Could not revoke: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}