package com.riffforge

import androidx.compose.ui.window.ComposeUIViewController
import com.riffforge.di.iosModules
import com.riffforge.feature_auth.IOSAuthManager
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatformTools
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual fun currentDailyDateInfo(): DailyDateInfo {
    val calendar = NSCalendar.currentCalendar
    val now = NSDate()
    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
        locale = NSLocale("en_US_POSIX")
    }
    val comps = calendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitWeekday,
        fromDate = now
    )
    val isoDate = formatter.stringFromDate(now)
    val weekday = comps.weekday.toInt() // 1=Sun, 2=Mon .. 7=Sat
    val dayOfWeekIndex = if (weekday == 1) 6 else weekday - 2
    val daySeed = comps.year.toInt() * 1000 + comps.month.toInt() * 32 + comps.day.toInt()

    val yesterdayDate = calendar.dateByAddingUnit(NSCalendarUnitDay, value = -1, toDate = now, options = 0u) ?: now
    val yesterdayIsoDate = formatter.stringFromDate(yesterdayDate)

    val weekIsoDates = List(7) { idx ->
        val delta = (idx - dayOfWeekIndex).toLong()
        val d = calendar.dateByAddingUnit(NSCalendarUnitDay, value = delta, toDate = now, options = 0u) ?: now
        formatter.stringFromDate(d)
    }

    return DailyDateInfo(
        dayOfWeekIndex = dayOfWeekIndex,
        daySeed = daySeed,
        isoDate = isoDate,
        yesterdayIsoDate = yesterdayIsoDate,
        weekIsoDates = weekIsoDates
    )
}

private val authManager: IOSAuthManager by lazy {
    KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<IOSAuthManager>()
        ?: IOSAuthManager()
}

fun MainViewController() = ComposeUIViewController(configure = { enforceStrictPlistSanityCheck = false }) {
    App(authManager = authManager)
}

// Expose the auth manager to Swift for bridge setup
fun getAuthManager(): IOSAuthManager = authManager

/**
 * Initialize Koin with iOS modules. Call this from SceneDelegate/AppDelegate.
 */
fun initKoin() {
    if (KoinPlatformTools.defaultContext().getOrNull() == null) {
        startKoin {
            modules(iosModules)
        }
    }
}

