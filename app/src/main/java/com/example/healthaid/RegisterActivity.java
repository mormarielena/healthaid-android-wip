package com.example.healthaid;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editTextName, editTextEmail,
            editTextPassword, editTextConfirmPassword;
    private MaterialButton    buttonRegister;
    private ProgressBar       progressBar;
    private FirebaseAuth      mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        editTextName            = findViewById(R.id.editTextRegName);
        editTextEmail           = findViewById(R.id.editTextRegEmail);
        editTextPassword        = findViewById(R.id.editTextRegPassword);
        editTextConfirmPassword = findViewById(R.id.editTextRegConfirmPassword);
        buttonRegister          = findViewById(R.id.buttonRegister);
        progressBar             = findViewById(R.id.progressBarRegister);
        TextView goToLogin      = findViewById(R.id.textViewGoToLogin);

        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name            = editTextName.getText().toString().trim();
        String email           = editTextEmail.getText().toString().trim();
        String password        = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            editTextName.setError("Name is required");
            editTextName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            editTextPassword.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match");
            editTextConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Save user profile to Firestore right after account creation
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, name, email);
                    } else {
                        setLoading(false);
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : "Registration failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("name",      name);
        user.put("email",     email);
        user.put("age",       "");
        user.put("weight",    "");
        user.put("allergies", "");

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    // Auth account exists but Firestore write failed — still proceed
                    Toast.makeText(this,
                            "Account created but profile save failed. You can update it later.",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!loading);
        buttonRegister.setText(loading ? "Creating account…" : "Create account");
    }
}