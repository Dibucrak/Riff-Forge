package com.riffforge.feature_tuner.domain

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

@SuppressLint("MissingPermission")
actual fun observeMicrophonePitch(targetFrequencyProvider: () -> Float?): Flow<PitchResult?> = flow {
    val sampleRate = 44100
    val minBuffer = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    ).coerceAtLeast(4096)

    var audioRecord: AudioRecord? = null
    try {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuffer
        )

        if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
            return@flow
        }

        audioRecord.startRecording()
        val shortBuffer = ShortArray(2048)
        val floatBuffer = FloatArray(2048)

        while (currentCoroutineContext().isActive) {
            val read = audioRecord.read(shortBuffer, 0, shortBuffer.size)
            if (read > 0) {
                for (i in 0 until read) {
                    floatBuffer[i] = shortBuffer[i] / 32768f
                }
                val freq = PitchDetectionMath.detectFrequency(floatBuffer, sampleRate)
                val result = PitchDetectionMath.analyzeFrequency(freq, targetFrequencyProvider())
                emit(result)
            }
            delay(35)
        }
    } catch (_: Exception) {
        // Permission not granted or microphone unavailable
    } finally {
        runCatching {
            audioRecord?.stop()
            audioRecord?.release()
        }
    }
}.flowOn(Dispatchers.Default)
