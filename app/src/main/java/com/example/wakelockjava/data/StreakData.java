package com.example.wakelockjava.data;

import java.util.List;

public class StreakData {
    public int currentStreak;
    public int longestStreak;
    public int consistencyPercent;
    public List<Boolean> last7Days;

    public StreakData(int currentStreak, int longestStreak, int consistencyPercent, List<Boolean> last7Days) {
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.consistencyPercent = consistencyPercent;
        this.last7Days = last7Days;
    }
}