package com.example.wakelockjava.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.alarm.ActiveAlarmSession;
import com.example.wakelockjava.alarm.AlarmScheduler;
import com.example.wakelockjava.data.AlarmEntity;
import com.example.wakelockjava.data.AlarmRepository;

public class CompletionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_completion);

        View successCard = findViewById(R.id.successCard);
        successCard.setScaleX(0f);
        successCard.setScaleY(0f);
        successCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new android.view.animation.OvershootInterpolator())
                .start();

        stopService(new Intent(this, com.example.wakelockjava.alarm.AlarmService.class));
        ActiveAlarmSession.stopCurrent();

        AlarmRepository repository =
                AlarmRepository.getInstance(this);

        repository.recordCompletion();

        rescheduleNextRepeatingAlarm();

        Button btnDone =
                findViewById(R.id.btnDone);

        btnDone.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            this,
                            HomeActivity.class
                    );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(intent);

            finish();
        });
    }

    private void rescheduleNextRepeatingAlarm() {
        long alarmId = getIntent().getLongExtra("alarm_id", -1);
        if (alarmId == -1) return;

        AlarmRepository repository = AlarmRepository.getInstance(this);
        AlarmScheduler scheduler = new AlarmScheduler(this);

        new Thread(() -> {
            AlarmEntity alarm = repository.getAlarmSync(alarmId);
            if (alarm != null && alarm.isEnabled && alarm.repeatDays != null && !alarm.repeatDays.isEmpty()) {
                scheduler.schedule(alarm);
            }
        }).start();
    }
}