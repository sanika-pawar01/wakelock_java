package com.example.wakelockjava.ui;

import android.app.Activity;
import com.example.wakelockjava.R;
import com.example.wakelockjava.data.PreferencesManager;

public class ThemeHelper {
    public static void applyTheme(Activity activity) {
        PreferencesManager prefs = new PreferencesManager(activity);
        String theme = prefs.getThemeMode();
        
        int themeResId;
        switch (theme) {
            case "DARK":
                themeResId = R.style.Theme_WakeLock_Dark;
                break;
            case "DREAMY":
                themeResId = R.style.Theme_WakeLock_Dreamy;
                break;
            case "MINIMAL":
            case "FOCUS":
                themeResId = R.style.Theme_WakeLock_Focus;
                break;
            case "SUNRISE":
            case "LIGHT":
                themeResId = R.style.Theme_WakeLock_Light;
                break;
            case "CALM":
            default:
                themeResId = R.style.Theme_WakeLock_Calm;
                break;
        }
        activity.setTheme(themeResId);
    }
}