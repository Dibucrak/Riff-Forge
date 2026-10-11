package com.riffforge.feature_theory.presentation.circle_of_fifths

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.riffforge.feature_theory.domain.model.KeySignature
import com.riffforge.feature_theory.domain.util.MusicTheoryData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircleOfFifthsScreen(
    onNavigateUp: () -> Unit
) {
    val keys = MusicTheoryData.circleOfFifths
    var selectedKey by remember { mutableStateOf(keys[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Círculo de Quintas") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Chord info card at the top
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tonalidad: ${selectedKey.majorName} / ${selectedKey.minorName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Alt: ${selectedKey.accidentals}",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Acordes en la escala:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val chunkedChords = selectedKey.chordsInKey.chunked(4)
                        chunkedChords.forEach { rowChords ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowChords.forEach { chord ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(chord, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Circle visualization
            item {
                val circleSize = 300.dp
                val radiusOuter = 120.dp
                val radiusInner = 75.dp
                val outerNodeSize = 48.dp
                val innerNodeSize = 40.dp

                Box(
                    modifier = Modifier
                        .size(circleSize)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    keys.forEachIndexed { index, keySignature ->
                        val angleDeg = (index * 30.0) - 90.0
                        val angleRad = angleDeg * kotlin.math.PI / 180.0

                        val outerX = (kotlin.math.cos(angleRad) * radiusOuter.value).dp
                        val outerY = (kotlin.math.sin(angleRad) * radiusOuter.value).dp

                        val innerX = (kotlin.math.cos(angleRad) * radiusInner.value).dp
                        val innerY = (kotlin.math.sin(angleRad) * radiusInner.value).dp

                        val isSelected = selectedKey.id == keySignature.id

                        Box(
                            modifier = Modifier
                                .size(outerNodeSize)
                                .align(Alignment.Center)
                                .offset(x = outerX, y = outerY)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedKey = keySignature },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keySignature.majorName,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(innerNodeSize)
                                .align(Alignment.Center)
                                .offset(x = innerX, y = innerY)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                .clickable { selectedKey = keySignature },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = keySignature.minorName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
