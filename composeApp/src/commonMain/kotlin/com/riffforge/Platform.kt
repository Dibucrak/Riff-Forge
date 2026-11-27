package com.riffforge

data class DailyDateInfo(
    val dayOfWeekIndex: Int, // 0 = Lunes .. 6 = Domingo
    val daySeed: Int,
    val isoDate: String,
    val yesterdayIsoDate: String,
    val weekIsoDates: List<String>
)

expect fun currentDailyDateInfo(): DailyDateInfo
