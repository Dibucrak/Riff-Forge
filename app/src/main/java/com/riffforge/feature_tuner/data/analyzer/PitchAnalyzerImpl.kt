package com.riffforge.feature_tuner.data.analyzer

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.riffforge.feature_tuner.domain.analyzer.PitchAnalyzer
import com.riffforge.feature_tuner.domain.model.PitchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

class PitchAnalyzerImpl : PitchAnalyzer {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    @SuppressLint("MissingPermission")
    override fun startAnalyzing(): Flow<PitchResult?> = flow {
        isRecording = true
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()

        val audioBuffer = ShortArray(bufferSize)

        while (isRecording && currentCoroutineContext().isActive) {
            val readResult = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
            if (readResult > 0) {
                val frequency = calculatePitchAutocorrelation(audioBuffer)
                if (frequency > 60f && frequency < 2000f) {
                    emit(createPitchResult(frequency))
                } else {
                    emit(null)
                }
            }
            delay(30)
        }
    }.flowOn(Dispatchers.IO)

    override fun stopAnalyzing() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    private fun calculatePitchAutocorrelation(audioBuffer: ShortArray): Float {
        var maxCorrelation = 0f
        var bestPeriod = -1

        for (period in 20 until audioBuffer.size / 2) {
            var correlation = 0f
            for (i in 0 until audioBuffer.size - period) {
                correlation += audioBuffer[i] * audioBuffer[i + period]
            }
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation
                bestPeriod = period
            }
        }

        return if (bestPeriod > 0) sampleRate.toFloat() / bestPeriod else 0f
    }

    private fun createPitchResult(frequency: Float): PitchResult {
        val a4 = 440.0
        val noteNumber = (12 * log2(frequency / a4) + 69).roundToInt()

        val exactFrequency = a4 * 2.0.pow((noteNumber - 69) / 12.0)

        val cents = (1200 * log2(frequency / exactFrequency)).toFloat()

        var index = noteNumber % 12
        if (index < 0) index += 12
        val noteName = noteNames[index]

        return PitchResult(
            frequency = frequency,
            noteName = noteName,
            cents = cents
        )
    }
}