package com.riffforge.feature_tuner.domain.analyzer

import com.riffforge.feature_tuner.domain.model.PitchResult
import kotlinx.coroutines.flow.Flow

interface PitchAnalyzer {
    fun startAnalyzing(): Flow<PitchResult?>
    fun stopAnalyzing()
}