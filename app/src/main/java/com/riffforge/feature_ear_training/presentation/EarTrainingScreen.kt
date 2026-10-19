package com.riffforge.feature_ear_training.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.riffforge.core.presentation.components.GlassBackground
import com.riffforge.core.presentation.components.GlassButton
import com.riffforge.core.presentation.components.GlassButtonAccent
import com.riffforge.core.presentation.components.GlassCard
import com.riffforge.core.presentation.components.GlassCardElevated
import com.riffforge.core.presentation.components.GlassSectionHeader
import com.riffforge.core.presentation.components.GlassStatChip
import com.riffforge.core.presentation.theme.*

/**
 * Liquid Glass Ear Training Screen — frosted glass panels, gradient play button,
 * and glassmorphic answer buttons with color feedback.
 */
@Composable
fun EarTrainingScreen(
    onNavigateUp: () -> Unit,
    isDarkTheme: Boolean = false,
    viewModel: EarTrainingViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val textColor = if (isDarkTheme) TextOnGlassPrimaryDark else TextOnGlassPrimaryLight
    val dimTextColor = if (isDarkTheme) TextOnGlassSecondaryDark else TextOnGlassSecondaryLight
    val glassSurface = if (isDarkTheme) GlassSurfaceDark else GlassSurfaceLight

    GlassBackground(isDarkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // Top bar
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                isDarkTheme = isDarkTheme,
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateUp,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = glassSurface.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Entrenador de Oído",
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Score chip
                GlassStatChip(
                    isDarkTheme = isDarkTheme,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AccentTint
                        )
                    },
                    label = {
                        Text(
                            text = "Score: ${state.score} / ${state.totalAnswered}",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¿Qué escuchas?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Play button — gradient circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(16.dp, CircleShape, spotColor = GlassShadow, ambientColor = GlassShadow)
                        .clip(CircleShape)
                        .background(
                            AccentTint,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.onEvent(EarTrainingEvent.PlayAudio) },
                        modifier = Modifier.size(140.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Audiotrack else Icons.Default.PlayArrow,
                            contentDescription = "Reproducir sonido",
                            modifier = Modifier.size(64.dp),
                            tint = OnAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                if (state.currentQuestion != null) {
                    val chunkedOptions = state.currentQuestion.options.chunked(2)

                    chunkedOptions.forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowOptions.forEach { option ->
                                val isCorrectAnswer = option == state.currentQuestion.correctAnswer
                                val isSelectedAnswer = option == state.selectedAnswer

                                val bgColor = when {
                                    !state.hasAnsweredCurrent -> glassSurface.copy(alpha = 0.5f)
                                    isCorrectAnswer -> SystemGreen.copy(alpha = 0.18f)
                                    isSelectedAnswer && !isCorrectAnswer -> SystemRed.copy(alpha = 0.18f)
                                    else -> glassSurface.copy(alpha = 0.3f)
                                }

                                val txtColor = when {
                                    !state.hasAnsweredCurrent -> textColor
                                    isCorrectAnswer -> SystemGreen
                                    isSelectedAnswer && !isCorrectAnswer -> SystemRed
                                    else -> dimTextColor
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = GlassShadow, ambientColor = GlassShadow)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(bgColor)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { viewModel.onEvent(EarTrainingEvent.SubmitAnswer(option)) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        color = txtColor,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(visible = state.hasAnsweredCurrent) {
                    GlassButtonAccent(
                        onClick = { viewModel.onEvent(EarTrainingEvent.NextQuestion) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Siguiente Ejercicio", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}