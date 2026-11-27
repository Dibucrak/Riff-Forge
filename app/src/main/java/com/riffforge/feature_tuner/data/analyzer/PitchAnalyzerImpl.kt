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
import kotlin.math.sqrt

class PitchAnalyzerImpl : PitchAnalyzer {

    private var activeRecord: AudioRecord? = null
    private val sampleRate = 44100

    private val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    @SuppressLint("MissingPermission")
    override fun startAnalyzing(): Flow<PitchResult?> = flow {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuf <= 0) {
            emit(null)
            return@flow
        }
        val bufferSamples = maxOf(minBuf / 2, 4096)
        val bufferBytes = bufferSamples * 2

        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferBytes
            )
        } catch (_: Exception) {
            emit(null)
            return@flow
        }

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            runCatching { record.release() }
            emit(null)
            return@flow
        }

        activeRecord = record
        try {
            record.startRecording()
            val audioBuffer = ShortArray(bufferSamples)

            while (currentCoroutineContext().isActive) {
                val readResult = record.read(audioBuffer, 0, bufferSamples)
                if (readResult > 0) {
                    val frequency = calculatePitchAutocorrelation(audioBuffer, readResult)
                    if (frequency in 60f..1200f) {
                        emit(createPitchResult(frequency))
                    } else {
                        emit(null)
                    }
                }
                delay(30)
            }
        } catch (_: Exception) {
            emit(null)
        } finally {
            runCatching {
                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop()
                }
            }
            runCatching { record.release() }
            if (activeRecord === record) {
                activeRecord = null
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun stopAnalyzing() {
        val record = activeRecord
        activeRecord = null
        runCatching {
            if (record?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop()
            }
        }
        runCatching { record?.release() }
    }

    private fun calculatePitchAutocorrelation(audioBuffer: ShortArray, length: Int): Float {
        if (length < 512) return 0f

        var sumSq = 0.0
        for (i in 0 until length) {
            val s = audioBuffer[i] / 32768.0
            sumSq += s * s
        }
        val rms = sqrt(sumSq / length)
        if (rms < 0.008) return 0f

        val minPeriod = (sampleRate / 1100).coerceAtLeast(24)
        val maxPeriod = (sampleRate / 65).coerceAtMost(length / 2 - 2)
        if (maxPeriod <= minPeriod) return 0f

        val correlations = DoubleArray(maxPeriod + 2)
        var maxCorrelation = 0.0
        var bestPeriod = -1

        for (period in minPeriod..maxPeriod) {
            var correlation = 0.0
            val limit = length - period
            for (i in 0 until limit) {
                correlation += (audioBuffer[i].toDouble() * audioBuffer[i + period].toDouble())
            }
            correlations[period] = correlation
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation
                bestPeriod = period
            }
        }

        if (bestPeriod <= minPeriod || bestPeriod >= maxPeriod) {
            return if (bestPeriod > 0) sampleRate.toFloat() / bestPeriod else 0f
        }

        // Parabolic interpolation around peak for sub-sample accuracy
        val y0 = correlations[bestPeriod - 1]
        val y1 = correlations[bestPeriod]
        val y2 = correlations[bestPeriod + 1]
        val denom = 2.0 * (2.0 * y1 - y0 - y2)
        val delta = if (kotlin.math.abs(denom) > 1e-9) ((y0 - y2) / denom).coerceIn(-0.5, 0.5) else 0.0
        val refinedPeriod = bestPeriod + delta
        return (sampleRate / refinedPeriod).toFloat()
    }

    private fun createPitchResult(frequency: Float): PitchResult {
        val a4 = 440.0
        val noteNumber = (12 * log2(frequency / a4) + 69).roundToInt()

        val exactFrequency = a4 * 2.0.pow((noteNumber - 69) / 12.0)

        val cents = (1200 * log2(frequency / exactFrequency)).toFloat().coerceIn(-50f, 50f)

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