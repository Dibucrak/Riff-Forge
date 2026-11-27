package com.riffforge.feature_songs.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.riffforge.feature_admin.presentation.AdminScreen
import com.riffforge.feature_cloud_backup.presentation.CloudBackupScreen
import com.riffforge.feature_profile.presentation.ProfileScreen
import com.riffforge.feature_setlists.presentation.SetlistDetailScreen
import com.riffforge.feature_song_editor.presentation.AddEditSongScreen
import com.riffforge.feature_songs.presentation.song_viewer.SongViewerScreen

/** Voyager Screen wrappers for Songs, Setlist, Editor, Profile, and Admin sub-screens. */

data class SongViewerToolScreen(
    val songId: String? = null,
    val setlistId: String? = null
) : Screen {
    // Backwards-compatible constructor if called with navigator
    constructor(navigator: Navigator?, songId: String? = null, setlistId: String? = null) : this(songId, setlistId)

    override val key: ScreenKey = "SongViewer_${songId ?: "default"}_${setlistId ?: "none"}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SongViewerScreen(
            songId = songId,
            setlistId = setlistId,
            onNavigateUp = { navigator.pop() },
            onEditClick = { editSongId ->
                navigator.push(AddEditSongToolScreen(songId = editSongId))
            },
            onNavigateToSong = { targetSongId, targetSetlistId ->
                navigator.replace(
                    SongViewerToolScreen(
                        songId = targetSongId,
                        setlistId = targetSetlistId
                    )
                )
            }
        )
    }
}

data class SetlistDetailToolScreen(
    val setlistId: String? = null
) : Screen {
    constructor(navigator: Navigator?, setlistId: String? = null) : this(setlistId)

    override val key: ScreenKey = "SetlistDetail_${setlistId ?: "default"}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        SetlistDetailScreen(
            setlistId = setlistId,
            onNavigateUp = { navigator.pop() },
            onSongClick = { songId, activeSetlistId ->
                navigator.push(
                    SongViewerToolScreen(
                        songId = songId,
                        setlistId = activeSetlistId
                    )
                )
            }
        )
    }
}

data class AddEditSongToolScreen(
    val songId: String? = null
) : Screen {
    constructor(navigator: Navigator?, songId: String? = null) : this(songId)

    override val key: ScreenKey = "AddEditSong_${songId ?: "new"}"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AddEditSongScreen(
            songId = songId,
            onNavigateUp = { navigator.pop() }
        )
    }
}

class ProfileToolScreen() : Screen {
    constructor(navigator: Navigator?) : this()

    override val key: ScreenKey = "ProfileScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ProfileScreen(
            navigator = navigator,
            onNavigateUp = { navigator.pop() },
            onNavigateToAdmin = {
                navigator.push(AdminPanelToolScreen())
            },
            onNavigateToCloudBackup = {
                navigator.push(ProfileCloudBackupToolScreen())
            }
        )
    }
}

class AdminPanelToolScreen() : Screen {
    constructor(navigator: Navigator?) : this()

    override val key: ScreenKey = "AdminPanelScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        AdminScreen(
            onNavigateUp = { navigator.pop() }
        )
    }
}

class ProfileCloudBackupToolScreen() : Screen {
    constructor(navigator: Navigator?) : this()

    override val key: ScreenKey = "ProfileCloudBackupScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CloudBackupScreen(
            onNavigateUp = { navigator.pop() }
        )
    }
}
