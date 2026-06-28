package com.riffforge.feature_ear_training.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.feature_ear_training.domain.model.QuestionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarTrainingScreen(
    onNavigateUp: () -> Unit,
    viewModel: EarTrainingViewModel = hiltViewModel()
) {
    val state = viewModel.state.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenador de Oído", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tipo: ${state.currentQuestion?.type?.name ?: ""}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Text(
                        text = "Score: ${state.score} / ${state.totalAnswered}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "¿Qué escuchas?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            IconButton(
                onClick = { viewModel.onEvent(EarTrainingEvent.PlayAudio) },
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(if (state.isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Audiotrack else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir sonido",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (state.currentQuestion != null) {
                val chunkedOptions = state.currentQuestion.options.chunked(2)

                chunkedOptions.forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowOptions.forEach { option ->

                            val isCorrectAnswer = option == state.currentQuestion.correctAnswer
                            val isSelectedAnswer = option == state.selectedAnswer

                            val containerColor = when {
                                !state.hasAnsweredCurrent -> MaterialTheme.colorScheme.surfaceVariant
                                isCorrectAnswer -> Color(0xFF4CAF50) // Verde
                                isSelectedAnswer && !isCorrectAnswer -> MaterialTheme.colorScheme.error // Rojo
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val contentColor = when {
                                !state.hasAnsweredCurrent -> MaterialTheme.colorScheme.onSurfaceVariant
                                isCorrectAnswer -> Color.White
                                isSelectedAnswer && !isCorrectAnswer -> MaterialTheme.colorScheme.onError
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Button(
                                onClick = { viewModel.onEvent(EarTrainingEvent.SubmitAnswer(option)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = containerColor,
                                    contentColor = contentColor
                                ),
                                enabled = !state.hasAnsweredCurrent
                            ) {
                                Text(
                                    text = option,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = state.hasAnsweredCurrent) {
                Button(
                    onClick = { viewModel.onEvent(EarTrainingEvent.NextQuestion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Siguiente Ejercicio", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}