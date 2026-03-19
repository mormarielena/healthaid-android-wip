package com.example.healthaid.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.healthaid.fragments.CaregiverFragment;
import com.example.healthaid.models.CaregiverLink;
import com.example.healthaid.fragments.HistoryFragment;
import com.example.healthaid.fragments.HomeFragment;
import com.example.healthaid.utils.MissedDoseWatcher;
import com.example.healthaid.fragments.ProfileFragment;
import com.example.healthaid.R;
import com.example.healthaid.fragments.ReminderFragment;
import com.example.healthaid.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextView             textToggleBanner;
    private View                 bannerContainer;
    private BottomNavigationView bottomNav;
    private FirebaseFirestore    db;
    private String               currentFragmentTag = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db          = FirebaseFirestore.getInstance();
        bottomNav   = findViewById(R.id.bottom_navigation);
        bannerContainer  = findViewById(R.id.caregiverBannerContainer);
        textToggleBanner = findViewById(R.id.textCaregiverToggle);

        bannerContainer.setVisibility(View.GONE);

        // Initialise session with own user info, then check for caregiver links
        initSessionAndCheckCaregiverLinks();

        // Listen for missed-dose nudges sent by caregivers
        MissedDoseWatcher.start(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), "home");
        }

        // Open reminders tab if launched from a notification tap
        if (getIntent() != null &&
                "reminders".equals(getIntent().getStringExtra("open_tab"))) {
            loadFragment(new ReminderFragment(), "reminders");
            bottomNav.setSelectedItemId(R.id.nav_reminders);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)      { loadFragment(new HomeFragment(),      "home"); }
            else if (id == R.id.nav_reminders) { loadFragment(new ReminderFragment(),  "reminders"); }
            else if (id == R.id.nav_history)   { loadFragment(new HistoryFragment(),   "history"); }
            else if (id == R.id.nav_caregiver) { loadFragment(new CaregiverFragment(), "caregiver"); }
            else if (id == R.id.nav_profile)   { loadFragment(new ProfileFragment(),   "profile"); }
            return true;
        });

        textToggleBanner.setOnClickListener(v -> toggleCaregiverMode());
    }

    // ─── Session init + caregiver link check ─────────────────────────────────

    private void initSessionAndCheckCaregiverLinks() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(snap -> {
                    String name = snap.getString("name");
                    SessionManager.get().init(uid, name != null ? name : "");

                    // Check if this user is a caregiver for anyone
                    checkForCaregiverLinks(uid);
                });
    }

    private void checkForCaregiverLinks(String uid) {
        db.collection("caregiver_links")
                .whereEqualTo("caregiverUserId", uid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (var doc : snapshots.getDocuments()) {
                        CaregiverLink link = doc.toObject(CaregiverLink.class);
                        if (link == null || !link.getIsActive()) continue;

                        // Found at least one active patient — show the banner
                        // (If multiple patients exist, show the first one for now)
                        link.setId(doc.getId());
                        showCaregiverBanner(link);
                        return;
                    }
                    // No active caregiver links — hide banner
                    bannerContainer.setVisibility(View.GONE);
                });
    }

    private void showCaregiverBanner(CaregiverLink link) {
        runOnUiThread(() -> {
            bannerContainer.setVisibility(View.VISIBLE);
            updateBannerText();

            // Pre-load patient name into session so toggle is instant
            SessionManager.get();
            // Store link details for when user switches mode
            bannerContainer.setTag(link);
        });
    }

    // ─── Toggle between own view and caregiver view ───────────────────────────

    private void toggleCaregiverMode() {
        SessionManager session = SessionManager.get();
        Object tag = bannerContainer.getTag();
        if (!(tag instanceof CaregiverLink)) return;
        CaregiverLink link = (CaregiverLink) tag;

        if (session.isCaregiverMode()) {
            session.exitCaregiverMode();
        } else {
            session.enterCaregiverMode(
                    link.getPatientUserId(),
                    link.getPatientName(),
                    link.getPermissionLevel(),
                    link.getId());
        }

        updateBannerText();

        // Reload the current fragment so it picks up the new session state
        reloadCurrentFragment();
    }

    private void updateBannerText() {
        SessionManager session = SessionManager.get();
        Object tag = bannerContainer.getTag();
        if (!(tag instanceof CaregiverLink)) return;
        CaregiverLink link = (CaregiverLink) tag;

        String patientFirst = link.getPatientName() != null
                && !link.getPatientName().isEmpty()
                ? link.getPatientName().split("\\s+")[0]
                : "Patient";

        if (session.isCaregiverMode()) {
            textToggleBanner.setText("Monitoring " + patientFirst + "  ·  Switch to my account");
        } else {
            textToggleBanner.setText("My account  ·  Switch to monitoring " + patientFirst);
        }
    }

    private void reloadCurrentFragment() {
        Fragment f;
        switch (currentFragmentTag) {
            case "reminders": f = new ReminderFragment();  break;
            case "history":   f = new HistoryFragment();   break;
            case "caregiver": f = new CaregiverFragment(); break;
            case "profile":   f = new ProfileFragment();   break;
            default:          f = new HomeFragment();       break;
        }
        loadFragment(f, currentFragmentTag);
    }

    // ─── Fragment loading ─────────────────────────────────────────────────────

    private void loadFragment(Fragment fragment, String tag) {
        currentFragmentTag = tag;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}