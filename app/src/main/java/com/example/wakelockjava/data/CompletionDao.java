package com.example.wakelockjava.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CompletionDao {

    @Insert
    void insert(CompletionEntity completion);

    @Query("SELECT * FROM completions ORDER BY dateEpochDay DESC")
    LiveData<List<CompletionEntity>> getAll();

    @Query("SELECT * FROM completions ORDER BY dateEpochDay DESC")
    List<CompletionEntity> getAllSync();
}