package com.example.wakelockjava.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.wakelockjava.alarm.AlarmReceiver;

import com.example.wakelockjava.data.AlarmEntity;

import java.util.Calendar;

public class AlarmScheduler {

    private final Context context;
    private final AlarmManager alarmManager;

    public AlarmScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    /** Returns true if scheduled successfully, false if permission missing. */
    public boolean schedule(AlarmEntity alarm) {
        if (!canScheduleExactAlarms()) return false;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("alarm_id", alarm.id);
        intent.putExtra("alarm_hour", alarm.hour);
        intent.putExtra("alarm_minute", alarm.minute);
        intent.putExtra("alarm_label", alarm.label);
        intent.putExtra("challenge_type", alarm.challengeType);
        intent.putExtra("challenge_target", alarm.targetCount);
        intent.putExtra("challenge_difficulty", alarm.difficulty);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = nextTriggerTimeMillis(alarm.hour, alarm.minute, alarm.repeatDays);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        return true;
    }

    public void cancel(AlarmEntity alarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    private long nextTriggerTimeMillis(int hour, int minute, String repeatDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (repeatDays == null || repeatDays.isEmpty()) {
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return calendar.getTimeInMillis();
        }

        // Repeating alarm logic
        java.util.Set<Integer> enabledDays = new java.util.HashSet<>();
        for (String d : repeatDays.split(",")) {
            try {
                int day = Integer.parseInt(d);
                // Calendar.MONDAY is 2, SUNDAY is 1. Our 1-7 is Mon-Sun.
                int calendarDay = (day == 7) ? Calendar.SUNDAY : day + 1;
                enabledDays.add(calendarDay);
            } catch (Exception ignored) {}
        }

        for (int i = 0; i < 8; i++) {
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            if (enabledDays.contains(currentDay) && calendar.getTimeInMillis() > System.currentTimeMillis()) {
                return calendar.getTimeInMillis();
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar.getTimeInMillis();
    }
}