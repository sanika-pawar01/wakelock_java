package com.example.wakelockjava.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.wakelockjava.R;
import com.example.wakelockjava.data.PreferencesManager;
import com.example.wakelockjava.ui.AlarmActivity;

public class AlarmService extends Service {

    private static final String CHANNEL_ID = "wakelock_alarm_service_channel";
    private static final int NOTIFICATION_ID = 9002;

    private AlarmSoundPlayer soundPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        soundPlayer = new AlarmSoundPlayer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        createNotificationChannel();

        int hour = intent.getIntExtra("alarm_hour", 6);
        int minute = intent.getIntExtra("alarm_minute", 0);
        String label = intent.getStringExtra("alarm_label");
        String challengeType = intent.getStringExtra("challenge_type");
        int target = intent.getIntExtra("challenge_target", 10);
        String difficulty = intent.getStringExtra("challenge_difficulty");

        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.putExtras(intent);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("WakeLock Alarm")
                .setContentText(label != null ? label : "Time to wake up.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        PreferencesManager prefs = new PreferencesManager(this);
        soundPlayer.start(prefs.getAlarmSoundUri(), prefs.isVibrationEnabled());
        ActiveAlarmSession.set(soundPlayer);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "WakeLock Alarm Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            
            channel.setSound(null, null); // We use AlarmSoundPlayer for sound

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (soundPlayer != null) {
            soundPlayer.stop();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}