package com.example.wakelockjava.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Manual singleton repository — replaces Hilt DI from the Kotlin version. */
public class AlarmRepository {

    private static volatile AlarmRepository INSTANCE;

    private final AlarmDao alarmDao;
    private final CompletionDao completionDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private AlarmRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        alarmDao = db.alarmDao();
        completionDao = db.completionDao();
    }

    public static AlarmRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AlarmRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AlarmRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public LiveData<List<AlarmEntity>> getAllAlarms() {
        return alarmDao.getAll();
    }

    public void addAlarm(AlarmEntity alarm) {
        executor.execute(() -> alarmDao.insert(alarm));
    }

    public void setEnabled(long id, boolean enabled) {
        executor.execute(() -> {
            AlarmEntity entity = alarmDao.getByIdSync(id);
            if (entity != null) {
                entity.isEnabled = enabled;
                alarmDao.update(entity);
            }
        });
    }

    public void deleteAlarm(AlarmEntity alarm) {
        executor.execute(() -> alarmDao.delete(alarm));
    }

    public List<AlarmEntity> getAllEnabledSync() {
        return alarmDao.getAllEnabledSync();
    }

    public AlarmEntity getAlarmSync(long id) {
        return alarmDao.getByIdSync(id);
    }

    public void recordCompletion() {
        executor.execute(() -> {
            CompletionEntity entity = new CompletionEntity();
            entity.dateEpochDay = LocalDate.now().toEpochDay();
            completionDao.insert(entity);
        });
    }

    public LiveData<List<CompletionEntity>> getAllCompletions() {
        return completionDao.getAll();
    }

    /** Synchronous streak calculation — call from a background thread. */
    public StreakData computeStreakDataSync() {
        List<CompletionEntity> completions = completionDao.getAllSync();
        Set<Long> completedDays = new HashSet<>();
        for (CompletionEntity c : completions) completedDays.add(c.dateEpochDay);

        long today = LocalDate.now().toEpochDay();

        int currentStreak = 0;
        long day = today;
        while (completedDays.contains(day)) {
            currentStreak++;
            day--;
        }

        List<Long> sortedDays = new ArrayList<>(completedDays);
        java.util.Collections.sort(sortedDays);
        int longestStreak = 0;
        int run = 0;
        Long prev = null;
        for (Long d : sortedDays) {
            run = (prev != null && d == prev + 1) ? run + 1 : 1;
            longestStreak = Math.max(longestStreak, run);
            prev = d;
        }

        int last30Count = 0;
        long oldestPossible = today - 29;
        long firstDay = today;
        
        for (Long d : completedDays) {
            if (d >= oldestPossible && d <= today) {
                last30Count++;
            }
            if (d < firstDay) firstDay = d;
        }

        long daysSinceStart = today - firstDay + 1;
        long divisor = Math.min(30, Math.max(1, daysSinceStart));
        int consistencyPercent = (last30Count * 100) / (int) divisor;

        List<Boolean> last7 = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            last7.add(completedDays.contains(today - i));
        }

        return new StreakData(currentStreak, longestStreak, consistencyPercent, last7);
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}