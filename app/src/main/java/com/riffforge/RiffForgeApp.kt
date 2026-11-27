package com.riffforge

import android.app.Application
import com.google.firebase.FirebaseApp
import com.riffforge.core.domain.util.AppSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RiffForgeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppSettings.init(this)
        FirebaseApp.initializeApp(this)
    }
}