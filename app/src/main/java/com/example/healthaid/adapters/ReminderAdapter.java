package com.example.healthaid.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthaid.R;
import com.example.healthaid.models.PillReminder;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    public interface OnReminderActionListener {
        void onTakenToggled(PillReminder reminder, boolean taken);
        void onDeleteClicked(PillReminder reminder);
    }

    private final List<PillReminder>       reminderList;
    private final OnReminderActionListener listener;

    public ReminderAdapter(List<PillReminder> reminderList,
                           OnReminderActionListener listener) {
        this.reminderList = reminderList;
        this.listener     = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        PillReminder reminder = reminderList.get(position);

        holder.textViewPillName.setText(reminder.getPillName());

        String dosagePart = (reminder.getDosage() != null && !reminder.getDosage().isEmpty())
                ? reminder.getDosage() + " " + reminder.getUnit() + " — "
                : "";
        holder.textViewTime.setText(dosagePart + reminder.getTime());

        // Use isTakenToday() — automatically false if takenDate != today,
        // giving us a free daily reset without any server-side job.
        boolean takenToday = reminder.isTakenToday();

        if (takenToday) {
            holder.textViewPillName.setPaintFlags(
                    holder.textViewPillName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewPillName.setAlpha(0.4f);
        } else {
            holder.textViewPillName.setPaintFlags(
                    holder.textViewPillName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewPillName.setAlpha(1.0f);
        }

        holder.checkBoxTaken.setOnCheckedChangeListener(null);
        holder.checkBoxTaken.setChecked(takenToday);
        holder.checkBoxTaken.setOnCheckedChangeListener((btn, isChecked) ->
                listener.onTakenToggled(reminder, isChecked));

        holder.buttonDelete.setOnClickListener(v ->
                listener.onDeleteClicked(reminder));
    }

    @Override
    public int getItemCount() { return reminderList.size(); }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder {
        TextView    textViewPillName;
        TextView    textViewTime;
        CheckBox    checkBoxTaken;
        ImageButton buttonDelete;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPillName = itemView.findViewById(R.id.textViewPillName);
            textViewTime     = itemView.findViewById(R.id.textViewTime);
            checkBoxTaken    = itemView.findViewById(R.id.checkBoxTaken);
            buttonDelete     = itemView.findViewById(R.id.buttonDelete);
        }
    }
}