package com.example.wakelockjava.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.alarm.ActiveAlarmSession;

import java.util.Random;

public class MathChallengeActivity extends AppCompatActivity {

    private int answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_math_challenge);

        TextView tvQuestion =
                findViewById(R.id.tvQuestion);

        EditText etAnswer =
                findViewById(R.id.etAnswer);

        Button btnSubmit =
                findViewById(R.id.btnSubmit);

        TextView tvStatus =
                findViewById(R.id.tvStatus);

        generateQuestion(tvQuestion);

        btnSubmit.setOnClickListener(v -> {

            String input =
                    etAnswer.getText()
                            .toString()
                            .trim();

            if (input.isEmpty()) {
                tvStatus.setText("Enter an answer.");
                return;
            }

            int userAnswer;

            try {
                userAnswer =
                        Integer.parseInt(input);
            } catch (NumberFormatException e) {
                tvStatus.setText("Enter a valid number.");
                return;
            }

            if (userAnswer == answer) {

                Intent intent = new Intent(this, CompletionActivity.class);
                intent.putExtra("alarm_id", getIntent().getLongExtra("alarm_id", -1));
                startActivity(intent);

                finish();

            } else {

                tvStatus.setText(
                        "Wrong. Try again."
                );
            }
        });
    }

    private void generateQuestion(TextView tvQuestion) {

        Random random = new Random();

        int a = random.nextInt(15) + 5;
        int b = random.nextInt(10) + 2;

        answer = a + b;

        tvQuestion.setText(
                a + " + " + b + " = ?"
        );
    }
}