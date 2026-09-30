package com.example.wakelockjava.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.wakelockjava.data.AlarmEntity;
import com.example.wakelockjava.data.AlarmRepository;
import com.example.wakelockjava.alarm.AlarmScheduler;

import java.util.List;

public class AlarmViewModel extends AndroidViewModel {

    private final AlarmRepository repository;

    public AlarmViewModel(@NonNull Application application) {
        super(application);
        repository = AlarmRepository.getInstance(application);
    }

    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return repository.getAllAlarms();
    }

    public void addAlarm(AlarmEntity alarm) {
        repository.addAlarm(alarm);
    }

    public void setEnabled(long id, boolean enabled) {
        repository.setEnabled(id, enabled);
        
        // Ensure AlarmManager is updated
        new Thread(() -> {
            AlarmEntity alarm = repository.getAlarmSync(id);
            if (alarm != null) {
                AlarmScheduler scheduler = new AlarmScheduler(getApplication());
                if (enabled) {
                    scheduler.schedule(alarm);
                } else {
                    scheduler.cancel(alarm);
                }
            }
        }).start();
    }

    public void deleteAlarm(AlarmEntity alarm) {
        repository.deleteAlarm(alarm);
    }
}