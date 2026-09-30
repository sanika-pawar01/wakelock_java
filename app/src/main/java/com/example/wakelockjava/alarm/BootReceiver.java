package com.example.wakelockjava.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.wakelockjava.data.AlarmEntity;
import com.example.wakelockjava.data.AlarmRepository;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        PendingResult pendingResult = goAsync();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AlarmRepository repository = AlarmRepository.getInstance(context);
                AlarmScheduler scheduler = new AlarmScheduler(context);
                List<AlarmEntity> enabledAlarms = repository.getAllEnabledSync();
                for (AlarmEntity alarm : enabledAlarms) {
                    scheduler.schedule(alarm);
                }
            } finally {
                pendingResult.finish();
            }
        });
    }
}