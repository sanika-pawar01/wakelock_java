package com.example.wakelockjava.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wakelockjava.R;
import com.example.wakelockjava.data.PreferencesManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 2000; // slightly longer for animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logoContainer = findViewById(R.id.logoContainer);
        logoContainer.setAlpha(0f);
        logoContainer.setScaleX(0.8f);
        logoContainer.setScaleY(0.8f);

        logoContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1200)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            PreferencesManager prefs = new PreferencesManager(this);
            Intent intent;

            if (prefs.isOnboardingComplete()) {
                intent = new Intent(this, HomeActivity.class);
            } else {
                intent = new Intent(this, OnboardingActivity.class);
            }

            startActivity(intent);
            finish();

        }, SPLASH_TIME);
    }
}