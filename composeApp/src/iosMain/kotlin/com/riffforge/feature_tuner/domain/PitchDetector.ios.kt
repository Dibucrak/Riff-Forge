package com.riffforge.feature_tuner.domain

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryOptionDefaultToSpeaker
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionModeMeasurement
import platform.AVFAudio.AVAudioSessionRecordPermissionGranted
import platform.AVFAudio.setActive
import kotlin.coroutines.resume

private suspend fun ensureMicPermission(): Boolean {
    val session = AVAudioSession.sharedInstance()
    val current = session.recordPermission
    if (current == AVAudioSessionRecordPermissionGranted) return true
    return suspendCancellableCoroutine { cont ->
        session.requestRecordPermission { granted ->
            if (cont.isActive) cont.resume(granted)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun observeMicrophonePitch(targetFrequencyProvider: () -> Float?): Flow<PitchResult?> = callbackFlow {
    val granted = runCatching { ensureMicPermission() }.getOrDefault(false)
    if (!granted) {
        trySend(null)
        close()
        return@callbackFlow
    }

    val engine = AVAudioEngine()
    val session = AVAudioSession.sharedInstance()

    val started = runCatching {
        session.setCategory(
            AVAudioSessionCategoryPlayAndRecord,
            mode = AVAudioSessionModeMeasurement,
            options = AVAudioSessionCategoryOptionDefaultToSpeaker,
            error = null
        )
        session.setActive(true, error = null)

        val inputNode = engine.inputNode
        val format = inputNode.outputFormatForBus(0u)
        val sampleRate = format.sampleRate.toInt()
        if (sampleRate <= 0 || format.channelCount == 0u) {
            return@runCatching false
        }

        inputNode.removeTapOnBus(0u)
        inputNode.installTapOnBus(
            bus = 0u,
            bufferSize = 4096u,
            format = format
        ) { buffer: AVAudioPCMBuffer?, _ ->
            if (buffer == null) return@installTapOnBus
            val frameLength = buffer.frameLength.toInt()
            val channelData = buffer.floatChannelData ?: return@installTapOnBus
            val firstChannel = channelData[0] ?: return@installTapOnBus

            if (frameLength >= 256) {
                val samples = FloatArray(frameLength)
                for (i in 0 until frameLength) {
                    samples[i] = firstChannel[i]
                }
                val freq = PitchDetectionMath.detectFrequency(samples, sampleRate)
                val result = PitchDetectionMath.analyzeFrequency(freq, targetFrequencyProvider())
                trySend(result)
            }
        }

        engine.prepare()
        engine.startAndReturnError(null)
    }.getOrDefault(false)

    if (!started) {
        trySend(null)
        close()
        return@callbackFlow
    }

    awaitClose {
        runCatching {
            engine.inputNode.removeTapOnBus(0u)
            engine.stop()
        }
    }
}
