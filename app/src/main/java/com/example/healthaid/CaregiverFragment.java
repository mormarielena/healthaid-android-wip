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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_caregiver, container, false);

        db     = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        containerCaregivers = view.findViewById(R.id.containerCaregivers);
        textViewEmpty       = view.findViewById(R.id.textViewCaregiverEmpty);
        progressBar         = view.findViewById(R.id.progressBarCaregiver);
        FloatingActionButton fab = view.findViewById(R.id.fabAddCaregiver);

        fab.setOnClickListener(v -> loadUserNameThenShowDialog());
        loadCurrentUserName();

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

    // ─── Load current user name ───────────────────────────────────────────────

    private void loadCurrentUserName() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists()) {
                        currentUserName = snap.getString("name");
                    }
                    if (currentUserName == null || currentUserName.isEmpty()) {
                        currentUserName = "A HealthAid user";
                    }
                });
    }

    // ─── Real-time caregiver list ─────────────────────────────────────────────
    // Query only on patientUserId (single-field, no index needed).
    // Filter isActive in Java to avoid requiring a composite index.

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

        if (snapshots == null) {
            textViewEmpty.setVisibility(View.VISIBLE);
            return;
        }

        int added = 0;
        for (var doc : snapshots.getDocuments()) {
            CaregiverLink link = doc.toObject(CaregiverLink.class);
            if (link == null) continue;

            // Filter isActive in Java — no composite index required
            if (!link.getIsActive()) continue;

            link.setId(doc.getId());
            addCaregiverCard(link);
            added++;
        }

        textViewEmpty.setVisibility(added == 0 ? View.VISIBLE : View.GONE);
    }

    private void addCaregiverCard(CaregiverLink link) {
        View card = LayoutInflater.from(getContext())
                .inflate(R.layout.item_caregiver_card, containerCaregivers, false);

        TextView textName       = card.findViewById(R.id.textCaregiverName);
        TextView textEmail      = card.findViewById(R.id.textCaregiverEmail);
        TextView textPermission = card.findViewById(R.id.textCaregiverPermission);
        View     btnRevoke      = card.findViewById(R.id.btnRevokeCaregiver);

        String displayName = (link.getCaregiverName() != null
                && !link.getCaregiverName().isEmpty())
                ? link.getCaregiverName() : "Unknown";

        textName.setText(displayName);
        textEmail.setText(link.getCaregiverEmail());

        String perm = CaregiverLink.PERMISSION_EDITOR.equals(link.getPermissionLevel())
                ? "Can view & edit" : "View only";
        textPermission.setText(perm);

        btnRevoke.setOnClickListener(v -> confirmRevoke(link));
        containerCaregivers.addView(card);
    }

    // ─── Invite dialog ────────────────────────────────────────────────────────

    private void loadUserNameThenShowDialog() {
        if (currentUserName != null) {
            showInviteDialog();
        } else {
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(snap -> {
                        currentUserName = snap.getString("name");
                        if (currentUserName == null) currentUserName = "A HealthAid user";
                        showInviteDialog();
                    });
        }
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

    // ─── Look up caregiver by email and write the link ────────────────────────

    private void findUserByEmailAndLink(String email, String permission) {
        db.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        Toast.makeText(getContext(),
                                "No HealthAid account found for " + email
                                        + ". They must register first.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    var    caregiverDoc  = snapshots.getDocuments().get(0);
                    String caregiverId   = caregiverDoc.getId();
                    String caregiverName = caregiverDoc.getString("name");

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
        CaregiverLink link = new CaregiverLink(
                userId, caregiverId, caregiverEmail, permission);
        link.setCaregiverName(caregiverName != null ? caregiverName : "");
        link.setPatientName(currentUserName);

        db.collection("caregiver_links")
                .add(link)
                .addOnSuccessListener(ref ->
                        Toast.makeText(getContext(),
                                caregiverEmail + " added as caregiver",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Could not add caregiver: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ─── Revoke access ────────────────────────────────────────────────────────

    private void confirmRevoke(CaregiverLink link) {
        String name = (link.getCaregiverName() != null && !link.getCaregiverName().isEmpty())
                ? link.getCaregiverName() : link.getCaregiverEmail();

        new AlertDialog.Builder(getContext())
                .setTitle("Remove caregiver")
                .setMessage("Remove " + name + "'s access to your medications?")
                .setPositiveButton("Remove", (dialog, which) ->
                        db.collection("caregiver_links")
                                .document(link.getId())
                                .update("isActive", false)
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(),
                                                "Could not revoke: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show()))
                .setNegativeButton("Cancel", null)
                .show();
    }
}