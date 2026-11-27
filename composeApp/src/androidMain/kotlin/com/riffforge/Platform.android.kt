package com.riffforge

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

actual fun currentDailyDateInfo(): DailyDateInfo {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val cal = Calendar.getInstance()
    val daySeed = cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    val isoDate = formatter.format(cal.time)

    val calDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Sun=1, Mon=2 .. Sat=7
    val dayOfWeekIndex = if (calDayOfWeek == Calendar.SUNDAY) 6 else calDayOfWeek - Calendar.MONDAY

    val yesterdayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayIsoDate = formatter.format(yesterdayCal.time)

    val mondayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -dayOfWeekIndex) }
    val weekIsoDates = List(7) { offset ->
        val dayCal = (mondayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }
        formatter.format(dayCal.time)
    }

    return DailyDateInfo(
        dayOfWeekIndex = dayOfWeekIndex,
        daySeed = daySeed,
        isoDate = isoDate,
        yesterdayIsoDate = yesterdayIsoDate,
        weekIsoDates = weekIsoDates
    )
}
