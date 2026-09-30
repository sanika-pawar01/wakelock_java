package com.example.wakelockjava.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.data.AlarmRepository;
import com.example.wakelockjava.data.StreakData;

import java.util.concurrent.Executors;

public class ProgressActivity extends AppCompatActivity {
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentTheme = new com.example.wakelockjava.data.PreferencesManager(this).getThemeMode();
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView tvHeadline = findViewById(R.id.tvStreakHeadline);
        TextView tvLongest = findViewById(R.id.tvLongest);
        TextView tvConsistency = findViewById(R.id.tvConsistency);

        AlarmRepository repository = AlarmRepository.getInstance(this);
        Executors.newSingleThreadExecutor().execute(() -> {
            StreakData data = repository.computeStreakDataSync();
            runOnUiThread(() -> {
                tvHeadline.setText(data.currentStreak + " days in motion.");
                tvLongest.setText(data.longestStreak + "d");
                tvConsistency.setText(data.consistencyPercent + "%");
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String theme = new com.example.wakelockjava.data.PreferencesManager(this).getThemeMode();
        if (!theme.equals(currentTheme)) {
            recreate();
        }
    }
}