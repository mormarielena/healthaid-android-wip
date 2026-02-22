package com.example.healthaid; // Asigură-te că pachetul este corect

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {

    private List<Symptom> symptomList;
    private OnItemClickListener listener;

    // Interface for handling item clicks
    public interface OnItemClickListener {
        void onItemClick(Symptom symptom);
    }

    // Constructor for the adapter
    public SymptomAdapter(List<Symptom> symptomList, OnItemClickListener listener) {
        this.symptomList = symptomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        Symptom currentSymptom = symptomList.get(position);

        holder.textViewName.setText(currentSymptom.getName());
        holder.imageViewIcon.setImageResource(currentSymptom.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentSymptom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return symptomList.size();
    }

    // Class for representing a single symptom item
    public static class SymptomViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageViewIcon;
        public TextView textViewName;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon = itemView.findViewById(R.id.imageViewSymptomIcon);
            textViewName = itemView.findViewById(R.id.textViewSymptomName);
        }
    }
}