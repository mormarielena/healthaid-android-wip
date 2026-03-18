package com.example.healthaid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextInputEditText editTextName, editTextAge,
            editTextWeight, editTextAllergies;
    private TextView          textViewInitials, textViewProfileName, textViewProfileEmail;
    private MaterialButton    buttonSave, buttonLogout;
    private ProgressBar       progressBar;

    private FirebaseFirestore db;
    private String            userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Views
        editTextName         = view.findViewById(R.id.editTextName);
        editTextAge          = view.findViewById(R.id.editTextAge);
        editTextWeight       = view.findViewById(R.id.editTextWeight);
        editTextAllergies    = view.findViewById(R.id.editTextAllergies);
        textViewInitials     = view.findViewById(R.id.textViewInitials);
        textViewProfileName  = view.findViewById(R.id.textViewProfileName);
        textViewProfileEmail = view.findViewById(R.id.textViewProfileEmail);
        buttonSave           = view.findViewById(R.id.buttonSaveProfile);
        buttonLogout         = view.findViewById(R.id.buttonLogout);
        progressBar          = view.findViewById(R.id.progressBarProfile);

        // Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return view;

        userId = currentUser.getUid();

        // Show email from Firebase Auth immediately
        String email = currentUser.getEmail();
        if (email != null) textViewProfileEmail.setText(email);

        loadProfile();

        buttonSave.setOnClickListener(v -> saveProfile());
        buttonLogout.setOnClickListener(v -> signOut());

        return view;
    }

    // ─── Load profile from Firestore ─────────────────────────────────────────

    private void loadProfile() {
        DocumentReference ref = db.collection("users").document(userId);

        ref.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name      = snapshot.getString("name");
                String age       = snapshot.getString("age");
                String weight    = snapshot.getString("weight");
                String allergies = snapshot.getString("allergies");

                if (name != null)      editTextName.setText(name);
                if (age != null)       editTextAge.setText(age);
                if (weight != null)    editTextWeight.setText(weight);
                if (allergies != null) editTextAllergies.setText(allergies);

                updateHeader(name);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(),
                        "Could not load profile: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
    }

    // ─── Save profile to Firestore ────────────────────────────────────────────

    private void saveProfile() {
        String name      = editTextName.getText().toString().trim();
        String age       = editTextAge.getText().toString().trim();
        String weight    = editTextWeight.getText().toString().trim();
        String allergies = editTextAllergies.getText().toString().trim();

        Map<String, Object> data = new HashMap<>();
        data.put("name",      name);
        data.put("age",       age);
        data.put("weight",    weight);
        data.put("allergies", allergies);

        setLoading(true);

        db.collection("users").document(userId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    updateHeader(name);
                    Toast.makeText(getContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(getContext(),
                            "Save failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ─── Update avatar initials + name header ─────────────────────────────────

    private void updateHeader(String name) {
        if (name != null && !name.isEmpty()) {
            textViewProfileName.setText(name);

            // Build initials: up to 2 words → "John Doe" → "JD"
            String[] parts = name.trim().split("\\s+");
            String initials = String.valueOf(parts[0].charAt(0)).toUpperCase();
            if (parts.length > 1) {
                initials += String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
            }
            textViewInitials.setText(initials);
        }
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonSave.setEnabled(!loading);
    }

    // ─── Sign out ─────────────────────────────────────────────────────────────

    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        MissedDoseWatcher.stop();
        SessionManager.reset();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}