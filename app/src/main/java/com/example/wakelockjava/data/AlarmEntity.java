package com.example.wakelockjava.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alarms")
public class AlarmEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public int hour;
    public int minute;
    public String label;
    public String repeatDays;   // comma-separated: "1,2,3,4,5"
    public boolean isEnabled;
    public String challengeType; // "SQUATS", "PUSHUPS", "WATER", "MAKE_BED", "MATH"
    public int targetCount;
    public String difficulty; // "EASY", "MEDIUM", "HARD"
}