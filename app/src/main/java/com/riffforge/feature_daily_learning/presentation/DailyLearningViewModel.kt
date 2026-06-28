package com.riffforge.feature_daily_learning.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.riffforge.feature_chords.domain.util.ChordLibrary
import com.riffforge.feature_daily_learning.domain.model.ChordOfTheDayInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class DailyLearningViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(DailyLearningState())
    val state: State<DailyLearningState> = _state

    init {
        generateDailyContent()
    }

    private fun generateDailyContent() {
        val calendar = Calendar.getInstance()
        val seed = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR)
        val random = Random(seed)

        val chordIndex = random.nextInt(ChordLibrary.chords.size)
        val dailyChord = ChordLibrary.chords[chordIndex]

        val formula = when (dailyChord.type) {
            "Mayor" -> "1 - 3 - 5"
            "Menor" -> "1 - b3 - 5"
            "7" -> "1 - 3 - 5 - b7"
            else -> "Definición variable"
        }

        val scales = when (dailyChord.type) {
            "Mayor" -> listOf("Escala Mayor (Jónica)", "Pentatónica Mayor", "Modo Lidio")
            "Menor" -> listOf("Escala Menor (Eólica)", "Pentatónica Menor", "Modo Dórico")
            "7" -> listOf("Modo Mixolidio", "Pentatónica Menor de Blues")
            else -> listOf("Escala Cromática")
        }

        val explanation = when (dailyChord.type) {
            "Mayor" -> "Transmite una sensación de alegría, resolución y estabilidad. Es el pilar armónico de la música occidental."
            "Menor" -> "Posee una sonoridad melancólica, introspectiva o triste. Ideal para crear tensión emocional y profundidad."
            "7" -> "Conocido como acorde dominante, genera una fuerte tensión sonora que pide a gritos resolver en la tónica."
            else -> "Un acorde con características sonoras únicas."
        }

        val info = ChordOfTheDayInfo(
            chord = dailyChord,
            formula = formula,
            notesIncluded = "Depende de la tónica (${dailyChord.root})",
            compatibleScales = scales,
            theoreticalExplanation = explanation
        )

        _state.value = state.value.copy(
            chordOfTheDay = info,
            isLoading = false
        )
    }
}