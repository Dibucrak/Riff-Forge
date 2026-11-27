package com.riffforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.FirebaseApp
import com.riffforge.di.androidModules
import org.koin.core.context.startKoin

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase if not already configured
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            // Firebase not configured — will fall back to NoOpAuthManager
            e.printStackTrace()
        }
        if (org.koin.mp.KoinPlatformTools.defaultContext().getOrNull() == null) {
            startKoin {
                modules(androidModules)
            }
        }
        setContent {
            App()
        }
    }
}
