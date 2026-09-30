package com.example.wakelockjava.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "completions")
public class CompletionEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long dateEpochDay;
}