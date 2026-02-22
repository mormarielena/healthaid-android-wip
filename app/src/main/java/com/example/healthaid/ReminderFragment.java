package com.example.healthaid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ReminderFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<PillReminder> reminderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewReminders);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddReminder);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reminderList = new ArrayList<>();
        reminderList.add(new PillReminder("Vitamin C", "08:00 AM"));
        reminderList.add(new PillReminder("Ibuprofen", "02:00 PM"));
        reminderList.add(new PillReminder("Magnesium", "08:00 PM"));

        adapter = new ReminderAdapter(reminderList);
        recyclerView.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Add new reminder clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}