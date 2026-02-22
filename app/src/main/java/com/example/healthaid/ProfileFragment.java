package com.example.healthaid;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private EditText editTextName, editTextAge, editTextWeight, editTextAllergies;
    private Button buttonSave;

    private static final String SHARED_PREFS = "HealthAidPrefs";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editTextName = view.findViewById(R.id.editTextName);
        editTextAge = view.findViewById(R.id.editTextAge);
        editTextWeight = view.findViewById(R.id.editTextWeight);
        editTextAllergies = view.findViewById(R.id.editTextAllergies);
        buttonSave = view.findViewById(R.id.buttonSaveProfile);

        loadData();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        return view;
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("USER_NAME", editTextName.getText().toString());
        editor.putString("USER_AGE", editTextAge.getText().toString());
        editor.putString("USER_WEIGHT", editTextWeight.getText().toString());
        editor.putString("USER_ALLERGIES", editTextAllergies.getText().toString());

        editor.apply();

        Toast.makeText(getContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);

        String name = sharedPreferences.getString("USER_NAME", "");
        String age = sharedPreferences.getString("USER_AGE", "");
        String weight = sharedPreferences.getString("USER_WEIGHT", "");
        String allergies = sharedPreferences.getString("USER_ALLERGIES", "");

        editTextName.setText(name);
        editTextAge.setText(age);
        editTextWeight.setText(weight);
        editTextAllergies.setText(allergies);
    }
}