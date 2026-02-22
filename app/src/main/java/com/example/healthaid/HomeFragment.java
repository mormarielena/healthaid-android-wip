package com.example.healthaid;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private SymptomAdapter adapter;
    private List<Symptom> symptomList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewSymptoms);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        symptomList = new ArrayList<>();
        symptomList.add(new Symptom("Headache", android.R.drawable.ic_dialog_alert));
        symptomList.add(new Symptom("Cold & Flu", android.R.drawable.ic_menu_sort_by_size));
        symptomList.add(new Symptom("Muscle Pain", android.R.drawable.ic_menu_directions));
        symptomList.add(new Symptom("Stomachache", android.R.drawable.ic_menu_info_details));
        symptomList.add(new Symptom("Fever", android.R.drawable.ic_menu_day));
        symptomList.add(new Symptom("Allergies", android.R.drawable.ic_menu_camera));

        adapter = new SymptomAdapter(symptomList, new SymptomAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Symptom symptom) {
                Intent intent = new Intent(getActivity(), SymptomDetailActivity.class);
                intent.putExtra("SYMPTOM_NAME", symptom.getName());
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        return view;
    }
}