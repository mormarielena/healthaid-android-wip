package com.example.healthaid.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthaid.R;
import com.example.healthaid.adapters.SymptomAdapter;
import com.example.healthaid.activities.SymptomDetailActivity;
import com.example.healthaid.models.Symptom;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView  recyclerView;
    private SymptomAdapter adapter;
    private List<Symptom> symptomList;
    private TextView      textViewGreeting, textViewSubGreeting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textViewGreeting    = view.findViewById(R.id.textViewGreeting);
        textViewSubGreeting = view.findViewById(R.id.textViewSubGreeting);
        recyclerView        = view.findViewById(R.id.recyclerViewSymptoms);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadGreeting();
        buildSymptomList();

        adapter = new SymptomAdapter(symptomList, symptom -> {
            Intent intent = new Intent(getActivity(), SymptomDetailActivity.class);
            intent.putExtra("SYMPTOM_NAME", symptom.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    // ─── Greeting personalised by time of day + name from Firestore ───────────

    private void loadGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String timeGreeting;
        if      (hour < 12) timeGreeting = "Good morning";
        else if (hour < 18) timeGreeting = "Good afternoon";
        else                timeGreeting = "Good evening";

        textViewGreeting.setText(timeGreeting + "!");
        textViewSubGreeting.setText("How are you feeling today?");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String name = snapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            String firstName = name.split("\\s+")[0];
                            textViewGreeting.setText(timeGreeting + ", " + firstName + "!");
                        }
                    }
                });
    }

    // ─── Symptom list ─────────────────────────────────────────────────────────

    private void buildSymptomList() {
        symptomList = new ArrayList<>();
        symptomList.add(new Symptom("Headache",     "Tension, migraine, sinus", R.drawable.dizzy));
        symptomList.add(new Symptom("Cold & Flu",   "Fever, runny nose, fatigue", R.drawable.cold));
        symptomList.add(new Symptom("Muscle Pain",  "Soreness, cramps, stiffness", R.drawable.muscle_pain));
        symptomList.add(new Symptom("Stomachache",  "Nausea, bloating, cramps", R.drawable.stomachache));
        symptomList.add(new Symptom("Fever",        "High temp, chills, sweating", R.drawable.fever));
        symptomList.add(new Symptom("Allergies",    "Itching, sneezing, rash", R.drawable.allergy));
    }
}