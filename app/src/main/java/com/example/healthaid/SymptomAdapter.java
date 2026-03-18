package com.example.healthaid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SymptomAdapter extends RecyclerView.Adapter<SymptomAdapter.SymptomViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Symptom symptom);
    }

    private final List<Symptom>      symptomList;
    private final OnItemClickListener listener;

    public SymptomAdapter(List<Symptom> symptomList, OnItemClickListener listener) {
        this.symptomList = symptomList;
        this.listener    = listener;
    }

    @NonNull
    @Override
    public SymptomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_symptom, parent, false);
        return new SymptomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SymptomViewHolder holder, int position) {
        Symptom symptom = symptomList.get(position);
        holder.textViewName.setText(symptom.getName());
        holder.textViewSubtitle.setText(symptom.getSubtitle());
        holder.imageViewIcon.setImageResource(symptom.getIconResId());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(symptom);
        });
    }

    @Override
    public int getItemCount() { return symptomList.size(); }

    public static class SymptomViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIcon;
        TextView  textViewName;
        TextView  textViewSubtitle;

        public SymptomViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIcon   = itemView.findViewById(R.id.imageViewSymptomIcon);
            textViewName    = itemView.findViewById(R.id.textViewSymptomName);
            textViewSubtitle = itemView.findViewById(R.id.textViewSymptomSubtitle);
        }
    }
}