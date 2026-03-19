package com.example.healthaid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthaid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION_MS = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);

        TextView textViewName = findViewById(R.id.textViewSplashName);

        // Fetch name from Firestore, fall back to "there" if not set yet
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = null;
                    if (snapshot.exists()) {
                        name = snapshot.getString("name");
                    }

                    // Use first name only so it fits on screen nicely
                    if (name != null && !name.trim().isEmpty()) {
                        String firstName = name.trim().split("\\s+")[0];
                        textViewName.setText(firstName + "!");
                    } else {
                        textViewName.setText("there!");
                    }

                    proceedToMain();
                })
                .addOnFailureListener(e -> {
                    // Firestore failed — still show the splash and move on
                    textViewName.setText("there!");
                    proceedToMain();
                });
    }

    private void proceedToMain() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION_MS);
    }
}