package com.example.wakelockjava.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wakelockjava.R;
import com.example.wakelockjava.data.AlarmEntity;

import java.util.List;
import java.util.Locale;

public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.AlarmViewHolder> {

    public interface Listener {
        void onToggle(AlarmEntity alarm, boolean enabled);
        void onDelete(AlarmEntity alarm);
        void onEdit(AlarmEntity alarm);
    }

    private List<AlarmEntity> alarms;
    private final Listener listener;

    public AlarmListAdapter(List<AlarmEntity> alarms, Listener listener) {
        this.alarms = alarms;
        this.listener = listener;
    }

    public void updateData(List<AlarmEntity> newAlarms) {
        this.alarms = newAlarms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmEntity alarm = alarms.get(position);
        holder.time.setText(String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute));
        holder.label.setText(alarm.label);
        holder.challenge.setText(alarm.challengeType +
                (alarm.targetCount > 0
                        ? " - " + alarm.targetCount + " (" + alarm.difficulty + ")"
                        : ""));

        holder.days.setText(formatDays(alarm.repeatDays));

        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(alarm.isEnabled);
        holder.switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onToggle(alarm, isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(alarm);
        });

        holder.remove.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(alarm);
        });
    }

    private String formatDays(String repeatDays) {
        if (repeatDays == null || repeatDays.isEmpty()) return "Once";
        String[] days = repeatDays.split(",");
        if (days.length == 7) return "Every day";
        if (days.length == 5 && repeatDays.contains("1") && repeatDays.contains("2") && repeatDays.contains("3") && repeatDays.contains("4") && repeatDays.contains("5")) return "Weekdays";
        
        String[] names = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        StringBuilder sb = new StringBuilder();
        for (String day : days) {
            try {
                int index = Integer.parseInt(day) - 1;
                if (index >= 0 && index < 7) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(names[index]);
                }
            } catch (Exception ignored) {}
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView time, label, challenge, days, remove;
        MaterialSwitch switchEnabled;

        AlarmViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.tvTime);
            label = itemView.findViewById(R.id.tvLabel);
            challenge = itemView.findViewById(R.id.tvChallenge);
            days = itemView.findViewById(R.id.tvDays);
            switchEnabled = itemView.findViewById(R.id.switchEnabled);
            remove = itemView.findViewById(R.id.tvRemove);
        }
    }
}