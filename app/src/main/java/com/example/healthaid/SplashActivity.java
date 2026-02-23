package com.example.healthaid;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_splash);

        TextView textViewName = findViewById(R.id.textViewSplashName);

        SharedPreferences prefs = getSharedPreferences("HealthAidPrefs", MODE_PRIVATE);
        String fullName = prefs.getString("USER_NAME", "User");

        textViewName.setText(fullName + "!");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 2500);
    }
}