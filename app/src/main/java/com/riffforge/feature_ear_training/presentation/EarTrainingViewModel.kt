package com.riffforge.feature_ear_training.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_ear_training.domain.model.EarTrainingQuestion
import com.riffforge.feature_ear_training.domain.model.QuestionType
import com.riffforge.feature_ear_training.domain.util.AudioSynth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class EarTrainingViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(EarTrainingState())
    val state: State<EarTrainingState> = _state
    private var currentSemitoneShift: Int = 0

    private val mockQuestions = listOf(
        EarTrainingQuestion("1", QuestionType.CHORD, "Mayor", listOf("Mayor", "Menor", "Aumentado", "Disminuido")),
        EarTrainingQuestion("2", QuestionType.CHORD, "Menor", listOf("Mayor", "Menor", "Dominante 7", "Disminuido")),
        EarTrainingQuestion("3", QuestionType.CHORD, "Aumentado", listOf("Mayor", "Aumentado", "Disminuido", "Menor 7")),
        EarTrainingQuestion("4", QuestionType.CHORD, "Disminuido", listOf("Menor", "Aumentado", "Disminuido", "Semidisminuido")),
        EarTrainingQuestion("5", QuestionType.CHORD, "Dominante 7", listOf("Dominante 7", "Mayor 7", "Menor 7", "Semidisminuido")),
        EarTrainingQuestion("6", QuestionType.CHORD, "Mayor 7", listOf("Dominante 7", "Mayor 7", "Menor 7", "Mayor")),
        EarTrainingQuestion("7", QuestionType.CHORD, "Menor 7", listOf("Dominante 7", "Mayor 7", "Menor 7", "Semidisminuido")),
        EarTrainingQuestion("8", QuestionType.CHORD, "Semidisminuido", listOf("Disminuido", "Menor 7", "Semidisminuido", "Dominante 7")),
        EarTrainingQuestion("9", QuestionType.INTERVAL, "Quinta Justa", listOf("Tercera Menor", "Quinta Justa", "Octava", "Tritono")),
        EarTrainingQuestion("10", QuestionType.INTERVAL, "Tercera Mayor", listOf("Segunda Mayor", "Tercera Menor", "Tercera Mayor", "Cuarta Justa")),
        EarTrainingQuestion("11", QuestionType.INTERVAL, "Tercera Menor", listOf("Segunda Mayor", "Tercera Menor", "Tercera Mayor", "Quinta Justa")),
        EarTrainingQuestion("12", QuestionType.INTERVAL, "Cuarta Justa", listOf("Cuarta Justa", "Tritono", "Quinta Justa", "Octava")),
        EarTrainingQuestion("13", QuestionType.INTERVAL, "Tritono", listOf("Cuarta Justa", "Tritono", "Quinta Justa", "Segunda Mayor")),
        EarTrainingQuestion("14", QuestionType.INTERVAL, "Octava", listOf("Quinta Justa", "Cuarta Justa", "Octava", "Tercera Mayor"))
    )

    init { loadNextQuestion() }

    fun onEvent(event: EarTrainingEvent) {
        when (event) {
            is EarTrainingEvent.PlayAudio -> {
                if (state.value.isPlaying) return
                viewModelScope.launch {
                    _state.value = state.value.copy(isPlaying = true)

                    val question = state.value.currentQuestion
                    val correctAnswer = question?.correctAnswer ?: ""
                    AudioSynth.playSoundForQuestion(correctAnswer, currentSemitoneShift)

                    delay(if (question?.type == QuestionType.INTERVAL) 1850L else 1650L)
                    _state.value = state.value.copy(isPlaying = false)
                }
            }
            is EarTrainingEvent.SubmitAnswer -> {
                if (state.value.hasAnsweredCurrent) return
                val isCorrect = event.answer == state.value.currentQuestion?.correctAnswer
                val newScore = if (isCorrect) state.value.score + 1 else state.value.score
                _state.value = state.value.copy(
                    hasAnsweredCurrent = true, wasAnswerCorrect = isCorrect,
                    selectedAnswer = event.answer, score = newScore,
                    totalAnswered = state.value.totalAnswered + 1
                )
            }
            is EarTrainingEvent.NextQuestion -> loadNextQuestion()
        }
    }

    private fun loadNextQuestion() {
        val currentId = _state.value.currentQuestion?.id
        val pool = mockQuestions.filter { it.id != currentId }.ifEmpty { mockQuestions }
        val picked = pool[Random.nextInt(pool.size)]
        val shuffled = picked.copy(options = picked.options.shuffled())
        currentSemitoneShift = Random.nextInt(-3, 6)
        _state.value = state.value.copy(
            currentQuestion = shuffled, hasAnsweredCurrent = false,
            wasAnswerCorrect = false, selectedAnswer = "", isPlaying = false
        )
    }
}