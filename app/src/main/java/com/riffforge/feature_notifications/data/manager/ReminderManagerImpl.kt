package com.riffforge.feature_notifications.data.manager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.riffforge.feature_notifications.data.worker.PracticeReminderWorker
import com.riffforge.feature_notifications.domain.manager.ReminderManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderManagerImpl(
    private val context: Context
) : ReminderManager {

    private val workManager = WorkManager.getInstance(context)
    private val workName = "DAILY_PRACTICE_REMINDER"

    override fun scheduleDailyReminder() {
        val now = Calendar.getInstance()
        val nextRun = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        val initialDelayMs = (nextRun.timeInMillis - now.timeInMillis).coerceAtLeast(TimeUnit.MINUTES.toMillis(5))

        val workRequest = PeriodicWorkRequestBuilder<PracticeReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun cancelReminder() {
        workManager.cancelUniqueWork(workName)
    }
}
