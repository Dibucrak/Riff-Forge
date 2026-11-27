package com.riffforge.feature_tools.presentation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.riffforge.feature_ai_chords.presentation.AiChordsScreen
import com.riffforge.feature_ai_chords.presentation.AiChordsViewModel
import com.riffforge.feature_chords.presentation.ChordDictionaryScreen
import com.riffforge.feature_cloud_backup.presentation.CloudBackupScreen
import com.riffforge.feature_contributions.presentation.CommunityScreen
import com.riffforge.feature_daily_learning.presentation.DailyLearningScreen
import com.riffforge.feature_ear_training.presentation.EarTrainingScreen
import com.riffforge.feature_metronome.presentation.MetronomeScreen
import com.riffforge.feature_theory.presentation.circle_of_fifths.CircleOfFifthsScreen
import com.riffforge.feature_theory.presentation.progressions.ProgressionScreen
import com.riffforge.feature_theory.presentation.scales.ScalesScreen

/** Voyager Screen wrappers for Tools sub-screens. */

class ChordDictionaryToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "ChordDictionaryScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ChordDictionaryScreen(onNavigateUp = { navigator.pop() })
    }
}

class DailyLearningToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "DailyLearningScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        DailyLearningScreen(
            onNavigateUp = { navigator.pop() },
            onNavigateToEarTraining = { navigator.push(EarTrainingToolScreen()) }
        )
    }
}

class MetronomeToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "MetronomeScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        MetronomeScreen(onNavigateUp = { navigator.pop() })
    }
}

class ScalesToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "ScalesScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ScalesScreen(onNavigateUp = { navigator.pop() })
    }
}

class ProgressionsToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "ProgressionsScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        ProgressionScreen(onNavigateUp = { navigator.pop() })
    }
}

class CircleOfFifthsToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "CircleOfFifthsScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CircleOfFifthsScreen(onNavigateUp = { navigator.pop() })
    }
}

class EarTrainingToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "EarTrainingScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        EarTrainingScreen(onNavigateUp = { navigator.pop() })
    }
}

class CommunityToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "CommunityScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CommunityScreen(onNavigateUp = { navigator.pop() })
    }
}

class CloudBackupToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "CloudBackupScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CloudBackupScreen(onNavigateUp = { navigator.pop() })
    }
}

class AiChordsToolScreen() : Screen {
    constructor(navigator: Navigator) : this()
    override val key: ScreenKey = "AiChordsScreen"

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = rememberScreenModel { AiChordsViewModel() }
        AiChordsScreen(
            viewModel = viewModel,
            onNavigateUp = { navigator.pop() }
        )
    }
}
