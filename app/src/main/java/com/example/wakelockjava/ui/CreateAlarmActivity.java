package com.example.wakelockjava.ui;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Build;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.wakelockjava.R;
import com.example.wakelockjava.alarm.AlarmScheduler;
import com.example.wakelockjava.data.AlarmEntity;
import com.example.wakelockjava.model.ChallengeType;
import com.example.wakelockjava.ui.viewmodel.AlarmViewModel;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.button.MaterialButton;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateAlarmActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private EditText etAlarmName;
    private TextView tvChallengeName, tvReps;
    private ChallengeType selectedChallenge = ChallengeType.SQUATS;
    private int reps = 10;
    private AlarmViewModel viewModel;
    private long editingAlarmId = -1;
    
    private final Set<Integer> selectedDays = new HashSet<>();
    private String selectedDifficulty = "MEDIUM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        viewModel = new ViewModelProvider(this).get(AlarmViewModel.class);

        editingAlarmId = getIntent().getLongExtra("alarm_id", -1);

        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);
        etAlarmName = findViewById(R.id.etAlarmName);
        tvChallengeName = findViewById(R.id.tvChallengeName);
        tvReps = findViewById(R.id.tvReps);
        Button btnMinus = findViewById(R.id.btnMinus);
        Button btnPlus = findViewById(R.id.btnPlus);
        Button btnSave = findViewById(R.id.btnSave);
        TextView tvChangeChallenge = findViewById(R.id.tvChangeChallenge);

        tvChangeChallenge.setOnClickListener(v -> showChallengePicker());

        btnMinus.setOnClickListener(v -> {
            if (reps > 1) reps--;
            tvReps.setText(String.valueOf(reps));
        });
        btnPlus.setOnClickListener(v -> {
            reps++;
            tvReps.setText(String.valueOf(reps));
        });

        setupDifficultyPicker();

        if (editingAlarmId != -1) {
            loadAlarmData();
            btnSave.setText("Update Alarm");
        } else {
            // Default select Mon-Fri
            for (int i = 1; i <= 5; i++) selectedDays.add(i);
            updateChallengeUI();
            setupDayPicker();
        }

        btnSave.setOnClickListener(v -> saveAlarm());
    }

    private void loadAlarmData() {
        new Thread(() -> {
            AlarmEntity alarm = com.example.wakelockjava.data.AlarmRepository.getInstance(this).getAlarmSync(editingAlarmId);
            if (alarm != null) {
                runOnUiThread(() -> {
                    timePicker.setHour(alarm.hour);
                    timePicker.setMinute(alarm.minute);
                    etAlarmName.setText(alarm.label);
                    try {
                        selectedChallenge = ChallengeType.valueOf(alarm.challengeType);
                    } catch (Exception ignored) {}
                    reps = alarm.targetCount;
                    tvReps.setText(String.valueOf(reps));
                    selectedDifficulty = alarm.difficulty;
                    
                    // Update difficulty UI
                    MaterialButtonToggleGroup group = findViewById(R.id.difficultyToggleGroup);
                    if ("EASY".equals(selectedDifficulty)) group.check(R.id.btnEasy);
                    else if ("HARD".equals(selectedDifficulty)) group.check(R.id.btnHard);
                    else group.check(R.id.btnMedium);

                    // Update days
                    selectedDays.clear();
                    if (alarm.repeatDays != null && !alarm.repeatDays.isEmpty()) {
                        for (String d : alarm.repeatDays.split(",")) {
                            try {
                                selectedDays.add(Integer.parseInt(d));
                            } catch (Exception ignored) {}
                        }
                    }
                    
                    setupDayPicker();
                    updateChallengeUI();
                });
            }
        }).start();
    }

    private void setupDayPicker() {
        int[] dayButtons = {R.id.btnDayM, R.id.btnDayT, R.id.btnDayW, R.id.btnDayTh, R.id.btnDayF, R.id.btnDayS, R.id.btnDaySu};
        for (int i = 0; i < dayButtons.length; i++) {
            final int dayIndex = i + 1; // 1=Mon, 7=Sun
            TextView btn = findViewById(dayButtons[i]);
            btn.setOnClickListener(v -> {
                if (selectedDays.contains(dayIndex)) {
                    selectedDays.remove(dayIndex);
                    btn.getBackground().setTint(ContextCompat.getColor(this, R.color.surface_variant));
                    btn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                } else {
                    selectedDays.add(dayIndex);
                    btn.getBackground().setTint(ContextCompat.getColor(this, R.color.primary));
                    btn.setTextColor(ContextCompat.getColor(this, R.color.on_primary));
                }
            });
            
            // Highlight based on current selectedDays
            if (selectedDays.contains(dayIndex)) {
                btn.getBackground().setTint(ContextCompat.getColor(this, R.color.primary));
                btn.setTextColor(ContextCompat.getColor(this, R.color.on_primary));
            } else {
                btn.getBackground().setTint(ContextCompat.getColor(this, R.color.surface_variant));
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            }
        }
    }

    private void setupDifficultyPicker() {
        MaterialButtonToggleGroup group = findViewById(R.id.difficultyToggleGroup);
        group.addOnButtonCheckedListener((group1, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnEasy) selectedDifficulty = "EASY";
                else if (checkedId == R.id.btnMedium) selectedDifficulty = "MEDIUM";
                else if (checkedId == R.id.btnHard) selectedDifficulty = "HARD";
            }
        });
    }

    private void showChallengePicker() {
        ChallengeType[] options = ChallengeType.values();
        String[] names = new String[options.length];
        for (int i = 0; i < options.length; i++) names[i] = options[i].displayName;

        new AlertDialog.Builder(this)
                .setTitle("Choose challenge")
                .setItems(names, (dialog, which) -> {
                    selectedChallenge = options[which];
                    reps = selectedChallenge.defaultTarget > 0 ? selectedChallenge.defaultTarget : 10;
                    tvReps.setText(String.valueOf(reps));
                    updateChallengeUI();
                })
                .show();
    }

    private void updateChallengeUI() {
        tvChallengeName.setText(selectedChallenge.displayName);
        findViewById(R.id.repsContainer).setVisibility(
                selectedChallenge.hasCount() ? android.view.View.VISIBLE : android.view.View.GONE);
    }


    private void saveAlarm() {
        AlarmEntity alarm = new AlarmEntity();
        alarm.id = editingAlarmId != -1 ? editingAlarmId : System.currentTimeMillis();
        alarm.hour = timePicker.getHour();
        alarm.minute = timePicker.getMinute();

        alarm.label =
                etAlarmName.getText()
                        .toString()
                        .trim();

        if (alarm.label.isEmpty()) {
            alarm.label = "Morning Routine";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            alarm.repeatDays = selectedDays.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        } else {
            StringBuilder sb = new StringBuilder();
            for (Integer day : selectedDays) {
                if (sb.length() > 0) sb.append(",");
                sb.append(day);
            }
            alarm.repeatDays = sb.toString();
        }

        alarm.isEnabled = true;

        alarm.challengeType =
                selectedChallenge.name();

        alarm.targetCount =
                selectedChallenge.hasCount()
                        ? reps
                        : 0;

        alarm.difficulty = selectedDifficulty;

        AlarmScheduler scheduler =
                new AlarmScheduler(this);

        if (!scheduler.canScheduleExactAlarms()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                Intent intent =
                        new Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse(
                                        "package:" + getPackageName()
                                )
                        );

                startActivity(intent);
            }

            return;
        }

        viewModel.addAlarm(alarm);

        scheduler.schedule(alarm);

        finish();
    }
}