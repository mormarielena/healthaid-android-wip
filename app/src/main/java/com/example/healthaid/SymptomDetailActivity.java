package com.example.healthaid;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SymptomDetailActivity extends AppCompatActivity {

    private TextView titleTextView, pillsTextView, remediesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_detail);

        titleTextView = findViewById(R.id.textViewDetailTitle);
        pillsTextView = findViewById(R.id.textViewPills);
        remediesTextView = findViewById(R.id.textViewRemedies);

        String symptomName = getIntent().getStringExtra("SYMPTOM_NAME");

        if (symptomName != null) {
            titleTextView.setText(symptomName);
            loadTreatments(symptomName);
        }
    }

    private void loadTreatments(String symptom) {
        switch (symptom) {
            case "Headache":
                pillsTextView.setText("• Paracetamol (500mg)\n• Ibuprofen (400mg)\n\nTake 1 pill every 8 hours after meals. Do not exceed 3 pills a day.");
                remediesTextView.setText("• Drink plenty of water (dehydration often causes headaches).\n• Rest in a dark, quiet room.\n• Apply a cold compress to your forehead.");
                break;
            case "Cold & Flu":
                pillsTextView.setText("• Coldrex / Theraflu (hot drinks)\n• Vitamin C & Zinc supplements\n• Paracetamol for fever.");
                remediesTextView.setText("• Hot tea with honey and lemon.\n• Inhale steam with chamomile.\n• Get plenty of sleep to let your body recover.");
                break;
            case "Muscle Pain":
                pillsTextView.setText("• Ibuprofen or Diclofenac\n• Muscle relaxant creams (e.g., Voltaren or Fastum Gel).");
                remediesTextView.setText("• Apply a warm pad to the affected area.\n• Gentle stretching.\n• Epsom salt bath.");
                break;
            default:
                pillsTextView.setText("No specific pills registered yet. Ask your local pharmacist.");
                remediesTextView.setText("Rest and stay hydrated. See a doctor if symptoms persist.");
                break;
        }
    }
}