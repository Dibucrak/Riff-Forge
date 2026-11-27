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

        val (formula, baseScales, explanation) = when (dailyChord.type) {
            "Mayor" -> Triple(
                "1 - 3 - 5",
                listOf("Escala Mayor (Jónica)", "Pentatónica Mayor", "Modo Lidio"),
                "Transmite una sensación de alegría, resolución y estabilidad. Es el pilar armónico de la música occidental."
            )
            "Mayor (Cejilla)" -> Triple(
                "1 - 3 - 5",
                listOf("Escala Mayor (Jónica)", "Pentatónica Mayor", "Modo Mixolidio"),
                "Acorde mayor transportable con cejilla. Permite trasladar la misma digitación a cualquier traste del mástil."
            )
            "Menor" -> Triple(
                "1 - b3 - 5",
                listOf("Escala Menor (Eólica)", "Pentatónica Menor", "Modo Dórico"),
                "Posee una sonoridad melancólica, introspectiva o profunda. Ideal para baladas, rock y progresiones emotivas."
            )
            "7" -> Triple(
                "1 - 3 - 5 - b7",
                listOf("Modo Mixolidio", "Pentatónica Menor de Blues", "Mixolidia b9 b13"),
                "Conocido como acorde dominante, genera una fuerte tensión armónica que pide resolver hacia el acorde tónico."
            )
            "Maj7" -> Triple(
                "1 - 3 - 5 - 7",
                listOf("Escala Mayor (Jónica)", "Modo Lidio", "Pentatónica Mayor"),
                "Sonido cálido, abierto y sofisticado, muy utilizado en jazz, bossa nova, neo-soul y baladas modernas."
            )
            "Min7" -> Triple(
                "1 - b3 - 5 - b7",
                listOf("Modo Dórico", "Pentatónica Menor", "Escala Menor (Eólica)"),
                "Suaviza la tristeza de la tríada menor añadiendo una séptima menor, logrando un carácter fluido y versátil."
            )
            "Sus4" -> Triple(
                "1 - 4 - 5",
                listOf("Modo Mixolidio", "Escala Mayor (Jónica)", "Pentatónica Mayor"),
                "Suspende la tercera reemplazándola por la cuarta justa, creando expectación antes de resolver al acorde mayor."
            )
            "Sus2" -> Triple(
                "1 - 2 - 5",
                listOf("Escala Mayor (Jónica)", "Modo Dórico", "Pentatónica Mayor"),
                "Sonido amplio y cristalino sin tercera definida; funciona tanto en contextos mayores como menores."
            )
            "Dim" -> Triple(
                "1 - b3 - b5",
                listOf("Escala Disminuida (Semitono-Tono)", "Modo Locrio", "Menor Armónica"),
                "Acorde de gran tensión simétrica, perfecto como acorde de paso cromático para conectar grados de una tonalidad."
            )
            "Aug" -> Triple(
                "1 - 3 - #5",
                listOf("Escala de Tonos Enteros", "Lidia Aumentada", "Menor Melódica"),
                "La quinta aumentada aporta un color enigmático e inestable, ideal para enlaces ascendentes hacia el IV o VI grado."
            )
            else -> Triple(
                "1 - 3 - 5",
                listOf("Pentatónica", "Escala Cromática"),
                "Un acorde con características sonoras únicas."
            )
        }

        val info = ChordOfTheDayInfo(
            chord = dailyChord,
            formula = formula,
            notesIncluded = "Tónica: ${dailyChord.root} · Dificultad: ${dailyChord.difficulty}",
            compatibleScales = baseScales,
            theoreticalExplanation = explanation
        )

        _state.value = state.value.copy(
            chordOfTheDay = info,
            isLoading = false
        )
    }
}