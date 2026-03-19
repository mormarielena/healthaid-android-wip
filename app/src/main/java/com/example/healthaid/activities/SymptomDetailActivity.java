package com.example.healthaid.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthaid.R;

public class SymptomDetailActivity extends AppCompatActivity {

    // ─── Data model ──────────────────────────────────────────────────────────

    static class Remedy {
        String icon, title, body;
        Remedy(String icon, String title, String body) {
            this.icon = icon; this.title = title; this.body = body;
        }
    }

    static class MedEntry {
        String name, dose, note;
        MedEntry(String name, String dose, String note) {
            this.name = name; this.dose = dose; this.note = note;
        }
    }

    static class SymptomInfo {
        String      symptomLabel;
        String      whenToSeek;
        MedEntry[]  meds;
        Remedy[]    remedies;

        SymptomInfo(String symptomLabel, String whenToSeek,
                    MedEntry[] meds, Remedy[] remedies) {
            this.symptomLabel = symptomLabel;
            this.whenToSeek   = whenToSeek;
            this.meds         = meds;
            this.remedies     = remedies;
        }
    }

    // ─── Views ────────────────────────────────────────────────────────────────

    private TextView      textViewDetailTitle, textViewWhenToSeek;
    private LinearLayout  containerMeds, containerRemedies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptom_detail);

        // Back arrow in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        textViewDetailTitle = findViewById(R.id.textViewDetailTitle);
        textViewWhenToSeek  = findViewById(R.id.textViewWhenToSeek);
        containerMeds       = findViewById(R.id.containerMeds);
        containerRemedies   = findViewById(R.id.containerRemedies);

        String symptomName = getIntent().getStringExtra("SYMPTOM_NAME");
        if (symptomName != null) {
            SymptomInfo info = getSymptomInfo(symptomName);
            textViewDetailTitle.setText(info.symptomLabel);
            textViewWhenToSeek.setText(info.whenToSeek);
            buildMedCards(info.meds);
            buildRemedyCards(info.remedies);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }

    // ─── Card builders ────────────────────────────────────────────────────────

    private void buildMedCards(MedEntry[] meds) {
        containerMeds.removeAllViews();
        for (MedEntry med : meds) {
            View card = getLayoutInflater().inflate(R.layout.item_med_card, containerMeds, false);
            ((TextView) card.findViewById(R.id.textMedName)).setText(med.name);
            ((TextView) card.findViewById(R.id.textMedDose)).setText(med.dose);
            ((TextView) card.findViewById(R.id.textMedNote)).setText(med.note);
            containerMeds.addView(card);
        }
    }

    private void buildRemedyCards(Remedy[] remedies) {
        containerRemedies.removeAllViews();
        for (Remedy remedy : remedies) {
            View card = getLayoutInflater().inflate(R.layout.item_remedy_card, containerRemedies, false);
            ((TextView) card.findViewById(R.id.textRemedyIcon)).setText(remedy.icon);
            ((TextView) card.findViewById(R.id.textRemedyTitle)).setText(remedy.title);
            ((TextView) card.findViewById(R.id.textRemedyBody)).setText(remedy.body);
            containerRemedies.addView(card);
        }
    }

    // ─── Medical data ─────────────────────────────────────────────────────────

    private SymptomInfo getSymptomInfo(String name) {
        switch (name) {

            case "Headache":
                return new SymptomInfo(
                        "Headache",
                        "See a doctor if: pain is sudden and severe (\"thunderclap\"), accompanied by fever/stiff neck, vision changes, or lasts more than 72 hours.",
                        new MedEntry[]{
                                new MedEntry("Paracetamol (Acetaminophen)",
                                        "500 – 1000 mg every 6–8 h · max 4 g/day",
                                        "First-line for mild to moderate headache. Take with food if stomach-sensitive. Avoid alcohol."),
                                new MedEntry("Ibuprofen (NSAID)",
                                        "200 – 400 mg every 6–8 h · max 1200 mg/day (OTC)",
                                        "Effective for tension and menstrual headaches. Avoid on empty stomach or if you have gastric ulcers."),
                                new MedEntry("Aspirin",
                                        "500 mg every 4–6 h · max 4 g/day",
                                        "Good for tension headaches. Not recommended under 16 years old or if taking blood thinners.")
                        },
                        new Remedy[]{
                                new Remedy("💧", "Hydrate first",
                                        "Dehydration is the #1 cause of tension headaches. Drink 1–2 glasses of water immediately and wait 20 minutes before reaching for medication."),
                                new Remedy("🌡️", "Cold or warm compress",
                                        "For tension headaches: apply a warm compress to the neck and shoulders. For migraines: a cold pack on the forehead works better."),
                                new Remedy("🌑", "Dark, quiet room",
                                        "Reduce sensory input — dim lights, lower noise. Migraine pain is significantly amplified by light and sound stimulus."),
                                new Remedy("🫁", "Breathing exercise",
                                        "Try 4-7-8 breathing: inhale 4 s, hold 7 s, exhale 8 s. Reduces cortisol levels which can trigger tension headaches.")
                        }
                );

            case "Cold & Flu":
                return new SymptomInfo(
                        "Cold & Flu",
                        "See a doctor if: fever exceeds 39.5 °C (103 °F), difficulty breathing, symptoms worsen after day 7, or chest pain appears.",
                        new MedEntry[]{
                                new MedEntry("Paracetamol / Acetaminophen",
                                        "500 – 1000 mg every 6 h · max 4 g/day",
                                        "Reduces fever and relieves aches. Safe for most people including pregnant women at standard doses."),
                                new MedEntry("Cetirizine (Antihistamine)",
                                        "10 mg once daily",
                                        "Relieves runny nose, sneezing, and itchy eyes. Non-drowsy formula — take in the morning."),
                                new MedEntry("Pseudoephedrine (Decongestant)",
                                        "60 mg every 4–6 h · max 240 mg/day",
                                        "Reduces nasal congestion. Avoid if you have high blood pressure or heart conditions. Short-term use only (max 3 days).")
                        },
                        new Remedy[]{
                                new Remedy("🍯", "Honey & lemon tea",
                                        "Honey has proven antimicrobial properties and coats the throat. Combine with fresh lemon juice and hot water. As effective as some OTC cough suppressants for mild symptoms."),
                                new Remedy("🫧", "Steam inhalation",
                                        "Lean over a bowl of hot water with a towel over your head for 10 minutes. Add 2 drops of eucalyptus oil for extra decongestant effect. Clears mucus from nasal passages."),
                                new Remedy("🧂", "Saline nasal rinse",
                                        "Mix ¼ tsp salt + ¼ tsp baking soda in 240 ml warm water. Use a neti pot or bulb syringe to rinse each nostril. Proven to shorten cold duration."),
                                new Remedy("😴", "Prioritise sleep",
                                        "Your immune system releases cytokines during sleep. Cutting sleep short reduces immune response significantly. Aim for 8–9 hours while symptomatic.")
                        }
                );

            case "Muscle Pain":
                return new SymptomInfo(
                        "Muscle Pain",
                        "See a doctor if: pain follows an injury with swelling/bruising, muscle weakness or numbness is present, or pain persists beyond 1 week of home treatment.",
                        new MedEntry[]{
                                new MedEntry("Ibuprofen (NSAID)",
                                        "400 mg every 6–8 h · max 1200 mg/day (OTC)",
                                        "Reduces inflammation at the muscle site — not just pain masking. Take with food. Not suitable for kidney disease or gastric ulcers."),
                                new MedEntry("Diclofenac gel (Voltaren)",
                                        "Apply 2–4 g to affected area 3–4× daily",
                                        "Topical NSAID. Delivers anti-inflammatory directly to tissue with fewer GI side effects than oral NSAIDs. Wash hands after application."),
                                new MedEntry("Magnesium supplement",
                                        "300 – 400 mg daily (elemental Mg)",
                                        "Helps with muscle cramps, especially nocturnal leg cramps. Takes 2–4 weeks of consistent use to see full benefit. Safe long-term.")
                        },
                        new Remedy[]{
                                new Remedy("🔥", "Heat therapy",
                                        "Apply a heating pad or warm towel to sore muscles for 15–20 minutes. Heat increases blood flow and relaxes muscle fibres. Best for chronic soreness or stiffness."),
                                new Remedy("🧊", "Ice therapy (acute)",
                                        "For fresh injuries (first 48 h), use ice wrapped in a cloth for 15 min on, 15 min off. Reduces inflammation and numbs acute pain. Never apply ice directly to skin."),
                                new Remedy("🛁", "Epsom salt bath",
                                        "Dissolve 2 cups of Epsom salt (magnesium sulphate) in a warm bath and soak for 15–20 minutes. Magnesium is absorbed through the skin and aids muscle recovery."),
                                new Remedy("🧘", "Gentle stretching",
                                        "After heat application, gently stretch the affected muscle group — hold each stretch 20–30 seconds. Never stretch a cold or acutely injured muscle.")
                        }
                );

            case "Stomachache":
                return new SymptomInfo(
                        "Stomachache",
                        "See a doctor if: severe or worsening pain, blood in stool or vomit, pain localised to lower-right abdomen (appendix), high fever, or vomiting for more than 24 hours.",
                        new MedEntry[]{
                                new MedEntry("Simethicone (Gas-X)",
                                        "80 – 125 mg after meals and at bedtime",
                                        "Breaks up gas bubbles in the digestive tract. Safe for all ages, no known drug interactions. Fast acting — usually within 30 minutes."),
                                new MedEntry("Loperamide (Imodium)",
                                        "4 mg initially, then 2 mg after each loose stool · max 16 mg/day",
                                        "For diarrhoea. Slows gut motility. Not recommended if you have fever or blood in stool — those require medical evaluation."),
                                new MedEntry("Antacid (Calcium carbonate)",
                                        "500 – 1000 mg as needed · max 7500 mg/day",
                                        "Neutralises stomach acid for fast heartburn and indigestion relief. Onset within 5 minutes. Take 1 hour after meals for best effect.")
                        },
                        new Remedy[]{
                                new Remedy("🫚", "Ginger tea",
                                        "Ginger contains gingerols which have proven anti-nausea and anti-inflammatory effects on the gut. Steep 1 tsp fresh grated ginger in hot water for 10 minutes."),
                                new Remedy("🌿", "Peppermint tea",
                                        "Menthol in peppermint relaxes the smooth muscles of the GI tract, relieving spasms and bloating. Avoid if you have acid reflux — it can relax the lower oesophageal sphincter."),
                                new Remedy("🍚", "BRAT diet",
                                        "Bananas, Rice, Applesauce, Toast. These bland, low-fibre foods are easy to digest and help firm up stools. Follow for 24–48 hours during acute stomach upset."),
                                new Remedy("🫃", "Gentle abdominal massage",
                                        "Lie flat and use your fingertips to massage in small clockwise circles around your navel. Follows the natural direction of the large intestine and helps move trapped gas.")
                        }
                );

            case "Fever":
                return new SymptomInfo(
                        "Fever",
                        "See a doctor if: temperature exceeds 39.5 °C (103 °F) in adults, any fever in infants under 3 months, fever lasting more than 3 days, or accompanied by severe headache or rash.",
                        new MedEntry[]{
                                new MedEntry("Paracetamol (Acetaminophen)",
                                        "500 – 1000 mg every 6 h · max 4 g/day",
                                        "First choice for fever. Safe across all age groups at correct doses. Takes effect within 30–45 minutes. Do not combine with other products containing paracetamol."),
                                new MedEntry("Ibuprofen (NSAID)",
                                        "200 – 400 mg every 6–8 h · max 1200 mg/day (OTC)",
                                        "Effective antipyretic. Can alternate with paracetamol every 3 hours to maintain steadier fever control. Take with food or milk."),
                                new MedEntry("Electrolyte solution (ORS)",
                                        "Sip 200 – 400 ml per hour during fever",
                                        "Not a fever reducer, but essential. Fever increases fluid loss by 10–15% per degree above 37 °C. Prevents dangerous dehydration. Commercial or homemade (1L water + 6 tsp sugar + ½ tsp salt).")
                        },
                        new Remedy[]{
                                new Remedy("🌡️", "Lukewarm sponge bath",
                                        "Sponge the forehead, neck, armpits, and groin with lukewarm (not cold) water. Evaporation lowers skin temperature. Avoid cold water — it causes shivering which raises core temperature."),
                                new Remedy("💧", "Aggressive hydration",
                                        "Drink water, diluted juice, or herbal teas every 15–20 minutes. Fever of 38.5 °C for 24 hours can lead to 1–2 L of extra fluid loss. Monitor urine colour — pale yellow is the target."),
                                new Remedy("🛏️", "Rest and light clothing",
                                        "Dress in single, light-cotton layers. Heavy blankets trap heat and prevent the body from cooling. Rest is essential — the immune system works hardest during sleep."),
                                new Remedy("❄️", "Cool compress",
                                        "Apply a cool (not cold) damp cloth to the forehead and wrists. Replace every 5–10 minutes as it warms. Provides comfort and mild temperature reduction.")
                        }
                );

            case "Allergies":
                return new SymptomInfo(
                        "Allergies",
                        "See a doctor if: severe throat swelling, difficulty breathing, dizziness or sudden drop in blood pressure (anaphylaxis — call emergency services immediately), or symptoms are uncontrolled after 2 weeks.",
                        new MedEntry[]{
                                new MedEntry("Cetirizine (Zyrtec)",
                                        "10 mg once daily",
                                        "2nd-generation antihistamine. Non-drowsy in most people. Best taken consistently every day during allergy season rather than only when symptomatic. Onset: 1 hour."),
                                new MedEntry("Loratadine (Claritin)",
                                        "10 mg once daily",
                                        "Non-sedating antihistamine. Good for daytime use. Least likely to cause drowsiness among antihistamines. Safe for long-term daily use."),
                                new MedEntry("Fluticasone nasal spray (Flonase)",
                                        "2 sprays per nostril once daily",
                                        "Corticosteroid nasal spray. Most effective treatment for allergic rhinitis — reduces nasal inflammation directly. Full effect takes 1–2 weeks of daily use. Do not stop suddenly.")
                        },
                        new Remedy[]{
                                new Remedy("🪟", "Keep windows closed",
                                        "Pollen counts are highest in the morning (5 AM – 10 AM) and on windy days. Keep windows closed during these times, especially in spring. Use air conditioning with a HEPA filter."),
                                new Remedy("🚿", "Shower after outdoor exposure",
                                        "Pollen sticks to skin, hair, and clothing. Showering immediately after being outdoors prevents transfer to bedding, which significantly reduces nocturnal symptoms."),
                                new Remedy("🍵", "Local raw honey",
                                        "Contains trace amounts of local pollen — regular small doses (1 tsp/day) may help desensitise the immune response over time. Most benefit when taken 4–6 weeks before allergy season."),
                                new Remedy("💨", "Saline nasal rinse",
                                        "Physically removes pollen and dust from nasal passages. Use a neti pot or saline spray twice daily. Proven to reduce symptom severity and medication dependency in allergic rhinitis.")
                        }
                );

            default:
                return new SymptomInfo(
                        name,
                        "If symptoms are severe, sudden, or worsening, consult a healthcare professional.",
                        new MedEntry[]{
                                new MedEntry("No data available", "", "Ask your local pharmacist for advice specific to your symptoms.")
                        },
                        new Remedy[]{
                                new Remedy("💧", "Rest and hydrate", "Drink plenty of fluids and allow your body time to recover.")
                        }
                );
        }
    }
}