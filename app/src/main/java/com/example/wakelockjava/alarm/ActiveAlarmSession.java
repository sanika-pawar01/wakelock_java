package com.example.wakelockjava.alarm;

/** Simple static holder so challenge Activities can call stop() on the alarm's sound player. */
public class ActiveAlarmSession {
    private static AlarmSoundPlayer currentPlayer;

    public static void set(AlarmSoundPlayer player) {
        currentPlayer = player;
    }

    public static void stopCurrent() {
        if (currentPlayer != null) {
            currentPlayer.stop();
            currentPlayer = null;
        }
    }
}