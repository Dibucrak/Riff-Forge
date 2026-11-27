package com.riffforge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.riffforge.core.presentation.components.BottomNavigationBar
import com.riffforge.core.presentation.navigation.RiffForgeTab
import com.riffforge.core.presentation.navigation.TabNavigationTracker
import com.riffforge.core.presentation.theme.RiffForgeTheme
import com.riffforge.feature_auth.domain.AuthManager
import com.riffforge.feature_auth.domain.NoOpAuthManager
import com.riffforge.feature_auth.presentation.forgot_password.ForgotPasswordScreen
import com.riffforge.feature_auth.presentation.login.LoginScreen
import com.riffforge.feature_auth.presentation.register.RegisterScreen
import com.riffforge.core.domain.sync.CloudSyncManager
import com.riffforge.core.domain.sync.NoOpCloudSyncManager
import kotlinx.coroutines.flow.collectLatest
import org.koin.mp.KoinPlatformTools

enum class AuthScreen { Login, Register, ForgotPassword }

val LocalAuthManager = staticCompositionLocalOf<AuthManager> { NoOpAuthManager() }
val LocalCloudSyncManager = staticCompositionLocalOf<CloudSyncManager> { NoOpCloudSyncManager() }
val LocalSignOutAction = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun App(authManager: AuthManager? = null) {
    val manager = remember(authManager) {
        authManager ?: runCatching {
            KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<AuthManager>()
        }.getOrNull() ?: NoOpAuthManager()
    }
    val cloudSyncManager = remember(manager) {
        (manager as? CloudSyncManager) ?: runCatching {
            KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<CloudSyncManager>()
        }.getOrNull() ?: NoOpCloudSyncManager()
    }
    var isLoggedIn by remember { mutableStateOf(manager.isAuthenticated()) }
    var authScreen by remember {
        mutableStateOf<AuthScreen?>(if (manager.isAuthenticated()) null else AuthScreen.Login)
    }

    // Listen for auth state changes
    LaunchedEffect(manager) {
        manager.currentUser.collectLatest { user ->
            isLoggedIn = user != null
            if (user == null) {
                authScreen = AuthScreen.Login
            }
        }
    }

    val signOutAction: () -> Unit = {
        isLoggedIn = false
        authScreen = AuthScreen.Login
    }

    CompositionLocalProvider(
        LocalAuthManager provides manager,
        LocalCloudSyncManager provides cloudSyncManager,
        LocalSignOutAction provides signOutAction
    ) {
        RiffForgeTheme {
            when {
                !isLoggedIn && authScreen == AuthScreen.Register -> {
                    RegisterScreen(
                        onNavigateToLogin = { authScreen = AuthScreen.Login },
                        onRegisterSuccess = {
                            isLoggedIn = true
                            authScreen = null
                        },
                        onNavigateUp = { authScreen = AuthScreen.Login },
                        authManager = manager
                    )
                }
                !isLoggedIn && authScreen == AuthScreen.ForgotPassword -> {
                    ForgotPasswordScreen(
                        onNavigateUp = { authScreen = AuthScreen.Login },
                        authManager = manager
                    )
                }
                !isLoggedIn -> {
                    LoginScreen(
                        onNavigateToRegister = { authScreen = AuthScreen.Register },
                        onNavigateToForgotPassword = { authScreen = AuthScreen.ForgotPassword },
                        onLoginSuccess = {
                            isLoggedIn = true
                            authScreen = null
                        },
                        onNavigateUp = { /* Already at root login screen */ },
                        authManager = manager
                    )
                }
                else -> {
                    TabNavigator(tab = RiffForgeTab.Tuner) { tabNavigator ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            CurrentTab()
                            if (TabNavigationTracker.isCurrentTabAtRoot) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                ) {
                                    BottomNavigationBar(tabNavigator = tabNavigator)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
