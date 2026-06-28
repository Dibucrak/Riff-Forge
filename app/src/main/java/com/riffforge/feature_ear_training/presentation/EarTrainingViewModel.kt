package com.riffforge.feature_ear_training.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riffforge.feature_ear_training.domain.model.EarTrainingQuestion
import com.riffforge.feature_ear_training.domain.model.QuestionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class EarTrainingViewModel @Inject constructor() : ViewModel() {

    private val _state = mutableStateOf(EarTrainingState())
    val state: State<EarTrainingState> = _state

    private val mockQuestions = listOf(
        EarTrainingQuestion(
            id = "1", type = QuestionType.CHORD, correctAnswer = "Mayor",
            options = listOf("Mayor", "Menor", "Aumentado", "Disminuido")
        ),
        EarTrainingQuestion(
            id = "2", type = QuestionType.INTERVAL, correctAnswer = "Quinta Justa",
            options = listOf("Tercera Menor", "Quinta Justa", "Octava", "Tritono")
        ),
        EarTrainingQuestion(
            id = "3", type = QuestionType.CHORD, correctAnswer = "Menor 7",
            options = listOf("Dominante 7", "Mayor 7", "Menor 7", "Semidisminuido")
        ),
        EarTrainingQuestion(
            id = "4", type = QuestionType.INTERVAL, correctAnswer = "Tercera Mayor",
            options = listOf("Segunda Mayor", "Tercera Menor", "Tercera Mayor", "Cuarta Justa")
        )
    )

    init {
        loadNextQuestion()
    }

    fun onEvent(event: EarTrainingEvent) {
        when (event) {
            is EarTrainingEvent.PlayAudio -> {
                viewModelScope.launch {
                    _state.value = state.value.copy(isPlaying = true)
                    delay(2000)
                    _state.value = state.value.copy(isPlaying = false)
                }
            }
            is EarTrainingEvent.SubmitAnswer -> {
                if (state.value.hasAnsweredCurrent) return

                val isCorrect = event.answer == state.value.currentQuestion?.correctAnswer
                val newScore = if (isCorrect) state.value.score + 1 else state.value.score

                _state.value = state.value.copy(
                    hasAnsweredCurrent = true,
                    wasAnswerCorrect = isCorrect,
                    selectedAnswer = event.answer,
                    score = newScore,
                    totalAnswered = state.value.totalAnswered + 1
                )
            }
            is EarTrainingEvent.NextQuestion -> {
                loadNextQuestion()
            }
        }
    }

    private fun loadNextQuestion() {
        val randomQuestion = mockQuestions[Random.nextInt(mockQuestions.size)]
        _state.value = state.value.copy(
            currentQuestion = randomQuestion,
            hasAnsweredCurrent = false,
            wasAnswerCorrect = false,
            selectedAnswer = "",
            isPlaying = false
        )
    }
}