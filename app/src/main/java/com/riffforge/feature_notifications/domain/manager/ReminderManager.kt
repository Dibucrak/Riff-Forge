package com.riffforge.feature_notifications.domain.manager

interface ReminderManager {
    fun scheduleDailyReminder()
    fun cancelReminder()
}