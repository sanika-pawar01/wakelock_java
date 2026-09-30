package com.example.wakelockjava.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Replaces Kotlin DataStore — simpler SharedPreferences wrapper for Java. */
public class PreferencesManager {

    private static final String PREFS_NAME = "wakelock_settings";
    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingComplete() {
        return prefs.getBoolean("onboarding_complete", false);
    }

    public void setOnboardingComplete(boolean complete) {
        prefs.edit().putBoolean("onboarding_complete", complete).apply();
    }

    public String getThemeMode() {
        return prefs.getString("theme_mode", "CALM");
    }

    public void setThemeMode(String mode) {
        prefs.edit().putString("theme_mode", mode).apply();
    }

    public boolean isVibrationEnabled() {
        return prefs.getBoolean("vibration_enabled", true);
    }

    public void setVibrationEnabled(boolean enabled) {
        prefs.edit().putBoolean("vibration_enabled", enabled).apply();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean("notifications_enabled", true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply();
    }

    public boolean isDarkTheme() {
        return prefs.getBoolean("dark_theme", false);
    }

    public void setDarkTheme(boolean enabled) {
        prefs.edit().putBoolean("dark_theme", enabled).apply();
    }

    public String getAlarmSoundUri() {
        return prefs.getString("alarm_sound_uri", null);
    }

    public void setAlarmSoundUri(String uri) {
        prefs.edit().putString("alarm_sound_uri", uri).apply();
    }

    public String getDefaultChallenge() {
        return prefs.getString("default_challenge", "SQUATS");
    }

    public int getDefaultTarget() {
        return prefs.getInt("default_target", 10);
    }

    public void setDefaultChallenge(String id, int target) {
        prefs.edit().putString("default_challenge", id).putInt("default_target", target).apply();
    }

    public int getSnoozeMinutes() {
        return prefs.getInt("snooze_minutes", 10);
    }

    public void setSnoozeMinutes(int minutes) {
        prefs.edit().putInt("snooze_minutes", minutes).apply();
    }
}