package com.example.wakelockjava.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    LiveData<List<AlarmEntity>> getAll();

    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    List<AlarmEntity> getAllEnabledSync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlarmEntity alarm);

    @Update
    void update(AlarmEntity alarm);

    @Delete
    void delete(AlarmEntity alarm);

    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    AlarmEntity getByIdSync(long id);
}