package com.example.wakelockjava.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.alarm.ActiveAlarmSession;
import com.example.wakelockjava.alarm.AlarmScheduler;
import com.example.wakelockjava.alarm.AlarmService;
import com.example.wakelockjava.alarm.AlarmSoundPlayer;
import com.example.wakelockjava.data.AlarmEntity;
import com.example.wakelockjava.data.PreferencesManager;
import com.example.wakelockjava.model.ChallengeType;

import java.util.Locale;

public class AlarmActivity extends AppCompatActivity {

    private String challengeType;
    private int target;
    private String difficulty;
    private long alarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            android.app.KeyguardManager keyguardManager = (android.app.KeyguardManager) getSystemService(android.content.Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        setContentView(R.layout.activity_alarm);

        TextView tvTime = findViewById(R.id.tvAlarmTime);
        
        // Clock pulse animation
        android.view.animation.Animation pulse = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        pulse.setDuration(1000);
        pulse.setRepeatMode(android.view.animation.Animation.REVERSE);
        pulse.setRepeatCount(android.view.animation.Animation.INFINITE);
        tvTime.startAnimation(pulse);

        TextView tvLabel = findViewById(R.id.tvAlarmLabel);
        TextView tvChallenge = findViewById(R.id.tvChallenge);
        Button btnStart = findViewById(R.id.btnStartChallenge);
        Button btnSnooze = findViewById(R.id.btnSnooze);

        int hour = getIntent().getIntExtra("alarm_hour", 6);
        int minute = getIntent().getIntExtra("alarm_minute", 0);

        alarmId = getIntent().getLongExtra("alarm_id", -1);
        String label = getIntent().getStringExtra("alarm_label");
        challengeType = getIntent().getStringExtra("challenge_type");
        target = getIntent().getIntExtra("challenge_target", 10);
        difficulty = getIntent().getStringExtra("challenge_difficulty");

        tvTime.setText(
                String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hour,
                        minute
                )
        );

        tvLabel.setText(
                label != null ? label : "Morning Routine"
        );

        String challengeText = getChallengeText();

        tvChallenge.setText(challengeText);

        btnStart.setOnClickListener(v -> startChallenge());
        btnSnooze.setOnClickListener(v -> snoozeAlarm(hour, minute, label));
    }

    private void snoozeAlarm(int hour, int minute, String label) {
        PreferencesManager prefs = new PreferencesManager(this);
        int snoozeMinutes = prefs.getSnoozeMinutes();
        
        stopAlarmService();
        
        AlarmEntity snoozeAlarm = new AlarmEntity();
        snoozeAlarm.id = System.currentTimeMillis();
        
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.add(java.util.Calendar.MINUTE, snoozeMinutes);
        
        snoozeAlarm.hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        snoozeAlarm.minute = calendar.get(java.util.Calendar.MINUTE);
        snoozeAlarm.label = label + " (Snoozed)";
        snoozeAlarm.challengeType = challengeType;
        snoozeAlarm.targetCount = target;
        snoozeAlarm.difficulty = difficulty;
        snoozeAlarm.isEnabled = true;
        
        new AlarmScheduler(this).schedule(snoozeAlarm);
        
        finish();
    }

    private void stopAlarmService() {
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);
        ActiveAlarmSession.stopCurrent();
    }

    private String getChallengeText() {

        if (challengeType == null) {
            return "Complete your challenge";
        }

        try {

            ChallengeType type =
                    ChallengeType.valueOf(challengeType);

            if (type.hasCount()) {
                return target + " " + type.displayName;
            }

            return type.displayName;

        } catch (Exception e) {
            return "Complete your challenge";
        }
    }

    private void startChallenge() {

        Intent intent = null;

        if (challengeType == null) {
            challengeType = ChallengeType.SQUATS.name();
        }

        switch (challengeType) {

            case "SQUATS":
                intent = new Intent(this, SquatChallengeActivity.class);
                break;

            case "PUSHUPS":
                intent = new Intent(this, PushUpChallengeActivity.class);
                break;

            case "WATER":
                intent = new Intent(this, DrinkWaterActivity.class);
                break;

            case "MAKE_BED":
                intent = new Intent(this, MakeBedActivity.class);
                break;

            case "MATH":
                intent = new Intent(this, MathChallengeActivity.class);
                break;
        }

        if (intent == null) {
            intent = new Intent(this, CompletionActivity.class);
        }

        intent.putExtra("target", target);
        intent.putExtra("difficulty", difficulty);
        intent.putExtra("alarm_id", alarmId);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Don't allow simply backing out of the alarm.
        // Complete the challenge instead.
    }
}