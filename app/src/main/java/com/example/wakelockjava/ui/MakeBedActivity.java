package com.example.wakelockjava.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.alarm.ActiveAlarmSession;

public class MakeBedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_make_bed);

        Button btnDone =
                findViewById(R.id.btnDone);

        btnDone.setOnClickListener(v -> {

            Intent intent = new Intent(this, CompletionActivity.class);
            intent.putExtra("alarm_id", getIntent().getLongExtra("alarm_id", -1));
            startActivity(intent);

            finish();
        });
    }
}