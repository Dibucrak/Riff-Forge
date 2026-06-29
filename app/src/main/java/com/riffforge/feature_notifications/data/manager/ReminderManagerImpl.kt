package com.riffforge.feature_notifications.data.manager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.riffforge.feature_notifications.data.worker.PracticeReminderWorker
import com.riffforge.feature_notifications.domain.manager.ReminderManager
import java.util.concurrent.TimeUnit

class ReminderManagerImpl(
    private val context: Context
) : ReminderManager {

    private val workManager = WorkManager.getInstance(context)
    private val WORK_NAME = "DAILY_PRACTICE_REMINDER"

    override fun scheduleDailyReminder() {
        val workRequest = PeriodicWorkRequestBuilder<PracticeReminderWorker>(
            24, TimeUnit.HOURS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun cancelReminder() {
        workManager.cancelUniqueWork(WORK_NAME)
    }
}