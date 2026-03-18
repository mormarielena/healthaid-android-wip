package com.example.healthaid;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    // ─── Validation rules ─────────────────────────────────────────────────────

    // RFC-5322 simplified — covers 99.9% of real email addresses
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    // Password rules
    private static final int    MIN_LENGTH         = 8;
    private static final Pattern HAS_UPPERCASE     = Pattern.compile(".*[A-Z].*");
    private static final Pattern HAS_LOWERCASE     = Pattern.compile(".*[a-z].*");
    private static final Pattern HAS_DIGIT         = Pattern.compile(".*[0-9].*");
    private static final Pattern HAS_SPECIAL       = Pattern.compile(".*[^a-zA-Z0-9].*");

    // ─── Views ────────────────────────────────────────────────────────────────

    private TextInputLayout    layoutName, layoutEmail, layoutPassword, layoutConfirm;
    private TextInputEditText  editName, editEmail, editPassword, editConfirm;
    private MaterialButton     buttonRegister;
    private ProgressBar        progressBar;
    private FirebaseAuth       mAuth;
    private FirebaseFirestore  db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        layoutName     = findViewById(R.id.layoutRegName);
        layoutEmail    = findViewById(R.id.layoutRegEmail);
        layoutPassword = findViewById(R.id.layoutRegPassword);
        layoutConfirm  = findViewById(R.id.layoutRegConfirm);

        editName     = findViewById(R.id.editTextRegName);
        editEmail    = findViewById(R.id.editTextRegEmail);
        editPassword = findViewById(R.id.editTextRegPassword);
        editConfirm  = findViewById(R.id.editTextRegConfirmPassword);

        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar    = findViewById(R.id.progressBarRegister);

        TextView goToLogin = findViewById(R.id.textViewGoToLogin);
        goToLogin.setOnClickListener(v -> { startActivity(new Intent(this, LoginActivity.class)); finish(); });

        // Clear errors as the user types
        editName.addTextChangedListener(clearError(layoutName));
        editEmail.addTextChangedListener(clearError(layoutEmail));
        editPassword.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                layoutPassword.setError(null);
                // Show live password strength hint while typing
                String hint = passwordStrengthHint(s.toString());
                layoutPassword.setHelperText(hint);
            }
        });
        editConfirm.addTextChangedListener(clearError(layoutConfirm));

        buttonRegister.setOnClickListener(v -> attemptRegister());
    }

    // ─── Registration flow ────────────────────────────────────────────────────

    private void attemptRegister() {
        String name     = editName.getText().toString().trim();
        String email    = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirm  = editConfirm.getText().toString();

        if (!validateAll(name, email, password, confirm)) return;

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(mAuth.getCurrentUser().getUid(), name, email);
                    } else {
                        setLoading(false);
                        String msg = task.getException() != null
                                ? friendlyAuthError(task.getException().getMessage())
                                : "Registration failed";
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private boolean validateAll(String name, String email,
                                String password, String confirm) {
        boolean valid = true;

        // Name
        if (name.isEmpty()) {
            layoutName.setError("Full name is required");
            valid = false;
        }

        // Email
        if (email.isEmpty()) {
            layoutEmail.setError("Email is required");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            layoutEmail.setError("Enter a valid email address (e.g. name@example.com)");
            valid = false;
        }

        // Password
        String pwErr = passwordError(password);
        if (pwErr != null) {
            layoutPassword.setError(pwErr);
            valid = false;
        }

        // Confirm password
        if (!password.equals(confirm)) {
            layoutConfirm.setError("Passwords do not match");
            valid = false;
        } else if (confirm.isEmpty()) {
            layoutConfirm.setError("Please confirm your password");
            valid = false;
        }

        return valid;
    }

    /**
     * Returns an error message if the password doesn't meet requirements,
     * or null if it passes all checks.
     */
    private String passwordError(String password) {
        if (password.length() < MIN_LENGTH)
            return "Password must be at least " + MIN_LENGTH + " characters";
        if (!HAS_UPPERCASE.matcher(password).matches())
            return "Password must contain at least one uppercase letter (A–Z)";
        if (!HAS_LOWERCASE.matcher(password).matches())
            return "Password must contain at least one lowercase letter (a–z)";
        if (!HAS_DIGIT.matcher(password).matches())
            return "Password must contain at least one number (0–9)";
        if (!HAS_SPECIAL.matcher(password).matches())
            return "Password must contain at least one special character (!@#$…)";
        return null;
    }

    /**
     * Returns a short live hint shown below the password field while typing.
     */
    private String passwordStrengthHint(String pw) {
        if (pw.isEmpty()) return "Min 8 chars · uppercase · lowercase · number · special char";
        int score = 0;
        if (pw.length() >= MIN_LENGTH)                        score++;
        if (HAS_UPPERCASE.matcher(pw).matches())              score++;
        if (HAS_LOWERCASE.matcher(pw).matches())              score++;
        if (HAS_DIGIT.matcher(pw).matches())                  score++;
        if (HAS_SPECIAL.matcher(pw).matches())                score++;

        if      (score <= 2) return "Weak password";
        else if (score <= 3) return "Moderate password";
        else if (score == 4) return "Strong password";
        else                 return "Very strong password";
    }

    // ─── Firestore user write ─────────────────────────────────────────────────

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
                    Toast.makeText(this,
                            "Account created but profile save failed. You can update it later.",
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonRegister.setEnabled(!loading);
        buttonRegister.setText(loading ? "Creating account…" : "Create account");
    }

    /** Converts Firebase auth error messages into user-friendly strings. */
    private String friendlyAuthError(String raw) {
        if (raw == null) return "Registration failed";
        if (raw.contains("email address is already in use"))
            return "An account with this email already exists. Try signing in instead.";
        if (raw.contains("badly formatted"))
            return "The email address format is invalid.";
        if (raw.contains("network"))
            return "No internet connection. Please check your network and try again.";
        return raw;
    }

    private TextWatcher clearError(TextInputLayout layout) {
        return new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) { layout.setError(null); }
        };
    }
}