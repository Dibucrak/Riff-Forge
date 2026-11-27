package com.riffforge.feature_ear_training.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.riffforge.core.audio.AudioSynth
import com.riffforge.core.presentation.theme.SystemGreen
import com.riffforge.core.presentation.theme.SystemRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class EarExercise(
    val id: String,
    val isChord: Boolean,
    val name: String,
    val semitones: Int = 0,
    val chordOffsets: List<Int> = emptyList()
)

private val EXERCISE_LIST = listOf(
    EarExercise("i_2m", false, "Segunda Mayor", semitones = 2),
    EarExercise("i_3min", false, "Tercera Menor", semitones = 3),
    EarExercise("i_3maj", false, "Tercera Mayor", semitones = 4),
    EarExercise("i_4j", false, "Cuarta Justa", semitones = 5),
    EarExercise("i_tri", false, "Tritono", semitones = 6),
    EarExercise("i_5j", false, "Quinta Justa", semitones = 7),
    EarExercise("i_8j", false, "Octava Justa", semitones = 12),
    EarExercise("c_maj", true, "Mayor", chordOffsets = listOf(0, 4, 7, 12)),
    EarExercise("c_min", true, "Menor", chordOffsets = listOf(0, 3, 7, 12)),
    EarExercise("c_dom7", true, "Dominante 7", chordOffsets = listOf(0, 4, 7, 10)),
    EarExercise("c_maj7", true, "Mayor 7", chordOffsets = listOf(0, 4, 7, 11)),
    EarExercise("c_dim", true, "Disminuido", chordOffsets = listOf(0, 3, 6, 12))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarTrainingScreen(
    onNavigateUp: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var score by remember { mutableStateOf(0) }
    var totalAnswered by remember { mutableStateOf(0) }
    var isPlayingAudio by remember { mutableStateOf(false) }

    var currentRootMidi by remember { mutableStateOf((55..67).random()) }
    var currentTarget by remember { mutableStateOf(EXERCISE_LIST.random()) }
    var currentOptions by remember {
        val pool = EXERCISE_LIST.filter { it.isChord == currentTarget.isChord && it.name != currentTarget.name }
        mutableStateOf((pool.shuffled().take(3).map { it.name } + currentTarget.name).shuffled())
    }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }

    fun nextQuestion() {
        val candidates = EXERCISE_LIST.filter { it.id != currentTarget.id }
        val target = candidates.random()
        val distractors = EXERCISE_LIST
            .filter { it.isChord == target.isChord && it.name != target.name }
            .shuffled()
            .take(3)
            .map { it.name }
        currentRootMidi = (55..67).random()
        currentTarget = target
        currentOptions = (distractors + target.name).shuffled()
        selectedAnswer = null
    }

    val isAnswered = selectedAnswer != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenamiento de Oído", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // ── Score Header Card ──────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Puntuación",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$score Aciertos",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Total: $totalAnswered",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Audio Playback Card ──────────────────────────────
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentTarget.isChord) "¿Qué acorde escuchas?" else "¿Qué intervalo escuchas?",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isPlayingAudio) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    else MaterialTheme.colorScheme.primary
                                )
                                .clickable(enabled = !isPlayingAudio) {
                                    scope.launch {
                                        isPlayingAudio = true
                                        if (currentTarget.isChord) {
                                            AudioSynth.playChordMidi(currentTarget.chordOffsets.map { currentRootMidi + it })
                                        } else {
                                            AudioSynth.playInterval(currentRootMidi, currentTarget.semitones)
                                        }
                                        delay(if (currentTarget.isChord) 1550 else 1850)
                                        isPlayingAudio = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Escuchar Intervalo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isPlayingAudio) "Reproduciendo..." else "Tocar para escuchar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Options Grid ──────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rows = currentOptions.chunked(2)
                    rows.forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowOptions.forEach { option ->
                                val isThisSelected = selectedAnswer == option
                                val isCorrectOption = currentTarget.name == option

                                val bgColor = when {
                                    !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                                    isCorrectOption -> SystemGreen.copy(alpha = 0.2f)
                                    isThisSelected -> SystemRed.copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }

                                val textColor = when {
                                    !isAnswered -> MaterialTheme.colorScheme.onSurface
                                    isCorrectOption -> SystemGreen
                                    isThisSelected -> SystemRed
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(bgColor)
                                        .clickable(enabled = !isAnswered) {
                                            selectedAnswer = option
                                            totalAnswered++
                                            if (option == currentTarget.name) {
                                                score++
                                            }
                                        }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textColor,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            if (rowOptions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Next Question Button ──────────────────────────────
                AnimatedVisibility(visible = isAnswered) {
                    Button(
                        onClick = { nextQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Siguiente Ejercicio",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
