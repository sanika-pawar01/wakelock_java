package com.example.wakelockjava.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wakelockjava.R;
import com.example.wakelockjava.data.AlarmEntity;
import com.example.wakelockjava.ui.viewmodel.AlarmViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private AlarmViewModel viewModel;
    private AlarmListAdapter adapter;
    private String currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentTheme = new com.example.wakelockjava.data.PreferencesManager(this).getThemeMode();
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        updateGreeting(tvGreeting);

        RecyclerView recyclerView = findViewById(R.id.recyclerAlarms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Add layout animation
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animation);

        adapter = new AlarmListAdapter(new ArrayList<>(), new AlarmListAdapter.Listener() {
            @Override
            public void onToggle(AlarmEntity alarm, boolean enabled) {
                viewModel.setEnabled(alarm.id, enabled);
            }

            @Override
            public void onDelete(AlarmEntity alarm) {
                viewModel.deleteAlarm(alarm);
            }

            @Override
            public void onEdit(AlarmEntity alarm) {
                Intent intent = new Intent(HomeActivity.this, CreateAlarmActivity.class);
                intent.putExtra("alarm_id", alarm.id);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        viewModel.getAllAlarms().observe(this, alarms -> {
            adapter.updateData(alarms);
            findViewById(R.id.tvEmptyState).setVisibility(alarms.isEmpty() ? View.VISIBLE : View.GONE);
            // if (wasEmpty && !alarms.isEmpty()) {
            //     recyclerView.scheduleLayoutAnimation();
            // }
        });

        FloatingActionButton fab = findViewById(R.id.fabAddAlarm);
        fab.setAlpha(0f);
        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(500).setDuration(400).start();
        
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateAlarmActivity.class));
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_alarms);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_progress) {
                startActivity(new Intent(this, ProgressActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return id == R.id.nav_alarms;
        });
    }

    private void updateGreeting(TextView tvGreeting) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12) greeting = "Good morning.";
        else if (hour < 18) greeting = "Good afternoon.";
        else greeting = "Good evening.";
        tvGreeting.setText(greeting);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_alarms);
        
        // Refresh if theme changed
        String theme = new com.example.wakelockjava.data.PreferencesManager(this).getThemeMode();
        if (!theme.equals(currentTheme)) {
            recreate();
        }
    }
}