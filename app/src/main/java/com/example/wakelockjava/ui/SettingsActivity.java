package com.example.wakelockjava.ui;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.materialswitch.MaterialSwitch;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.data.PreferencesManager;

public class SettingsActivity extends AppCompatActivity {

    private static final int RINGTONE_PICKER_REQUEST = 999;
    private PreferencesManager prefs;
    private TextView tvSoundName;
    private TextView tvThemeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        prefs = new PreferencesManager(this);

        MaterialSwitch switchVibration = findViewById(R.id.switchVibration);
        switchVibration.setChecked(prefs.isVibrationEnabled());
        switchVibration.setOnCheckedChangeListener((v, checked) -> prefs.setVibrationEnabled(checked));

        MaterialSwitch switchNotifications = findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(prefs.isNotificationsEnabled());
        switchNotifications.setOnCheckedChangeListener((v, checked) -> prefs.setNotificationsEnabled(checked));

        View themeContainer = findViewById(R.id.themeContainer);
        tvThemeName = findViewById(R.id.tvThemeName);
        tvThemeName.setText(capitalize(prefs.getThemeMode()));
        themeContainer.setOnClickListener(v -> showThemePicker());

        View snoozeContainer = findViewById(R.id.snoozeContainer);
        TextView tvSnoozeValue = findViewById(R.id.tvSnoozeValue);
        tvSnoozeValue.setText(prefs.getSnoozeMinutes() + " min");
        snoozeContainer.setOnClickListener(v -> showSnoozePicker(prefs, tvSnoozeValue));

        View soundContainer = findViewById(R.id.soundContainer);
        tvSoundName = findViewById(R.id.tvSoundName);
        updateSoundName();
        soundContainer.setOnClickListener(v -> pickRingtone());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void showThemePicker() {
        String[] themes = {"CALM", "DARK", "DREAMY", "MINIMAL", "SUNRISE"};
        String[] displayNames = {"Calm Morning", "Dark Focus", "Dreamy Atmosphere", "Strict Minimal", "Bright Sunrise"};
        
        new android.app.AlertDialog.Builder(this)
                .setTitle("Choose app personality")
                .setItems(displayNames, (dialog, which) -> {
                    String selected = themes[which];
                    prefs.setThemeMode(selected);
                    
                    // Re-apply theme and animate restart
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .show();
    }

    private void pickRingtone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                prefs.setAlarmSoundUri(uri.toString());
                updateSoundName();
            }
        }
    }

    private void updateSoundName() {
        String uriStr = prefs.getAlarmSoundUri();
        if (uriStr == null) {
            tvSoundName.setText("Default");
        } else {
            Uri uri = Uri.parse(uriStr);
            android.media.Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
            if (ringtone != null) {
                tvSoundName.setText(ringtone.getTitle(this));
            } else {
                tvSoundName.setText("Selected");
            }
        }
    }

    private void showSnoozePicker(PreferencesManager prefs, TextView tvSnoozeValue) {
        String[] options = {"5 min", "10 min", "15 min", "20 min"};
        int[] values = {5, 10, 15, 20};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Snooze Duration")
                .setItems(options, (dialog, which) -> {
                    prefs.setSnoozeMinutes(values[which]);
                    tvSnoozeValue.setText(options[which]);
                })
                .show();
    }
}