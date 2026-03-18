package com.example.healthaid;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // Open reminders tab if launched from a notification tap
        if (getIntent() != null &&
                "reminders".equals(getIntent().getStringExtra("open_tab"))) {
            loadFragment(new ReminderFragment());
            bottomNav.setSelectedItemId(R.id.nav_reminders);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)      loadFragment(new HomeFragment());
            else if (id == R.id.nav_reminders) loadFragment(new ReminderFragment());
            else if (id == R.id.nav_history)   loadFragment(new HistoryFragment());
            else if (id == R.id.nav_caregiver) loadFragment(new CaregiverFragment());
            else if (id == R.id.nav_profile)   loadFragment(new ProfileFragment());
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}