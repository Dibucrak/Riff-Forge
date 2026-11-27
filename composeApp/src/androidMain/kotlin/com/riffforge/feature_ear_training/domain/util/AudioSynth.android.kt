package com.riffforge.feature_ear_training.domain.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicReference

private val activeKmpTrack = AtomicReference<AudioTrack?>(null)

actual fun playPcmSamples(samples: ShortArray, sampleRate: Int) {
    if (samples.isEmpty()) return
    Thread {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val format = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        val audioTrack = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        }.getOrNull() ?: return@Thread

        activeKmpTrack.getAndSet(audioTrack)?.let { prev ->
            runCatching {
                if (prev.playState == AudioTrack.PLAYSTATE_PLAYING) prev.stop()
                prev.release()
            }
        }

        try {
            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            val durationMs = (samples.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 100L)
        } catch (_: Exception) {
        } finally {
            activeKmpTrack.compareAndSet(audioTrack, null)
            runCatching {
                if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) audioTrack.stop()
                audioTrack.release()
            }
        }
    }.start()
}
