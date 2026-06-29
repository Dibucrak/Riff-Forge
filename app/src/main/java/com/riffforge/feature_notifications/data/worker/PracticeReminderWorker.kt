package com.riffforge.feature_notifications.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.riffforge.feature_notifications.domain.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PracticeReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            NotificationHelper.showPracticeReminder(context)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}