package com.riffforge.feature_metronome.presentation

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

private val cachedTracks = mutableMapOf<Pair<MetronomeSound, Boolean>, AudioTrack>()

private fun buildClickTrack(isAccent: Boolean, sound: MetronomeSound): AudioTrack {
    val pcm = MetronomeSynth.generateClickPcm(isAccent, sound)
    val attrs = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    val format = AudioFormat.Builder()
        .setSampleRate(MetronomeSynth.SAMPLE_RATE)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()
    return AudioTrack.Builder()
        .setAudioAttributes(attrs)
        .setAudioFormat(format)
        .setBufferSizeInBytes(pcm.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
        .apply {
            write(pcm, 0, pcm.size)
        }
}

actual fun playMetronomeClick(isAccent: Boolean, beat: Int, sound: MetronomeSound) {
    runCatching {
        val key = sound to isAccent
        val track = cachedTracks[key] ?: buildClickTrack(isAccent, sound).also {
            cachedTracks[key] = it
        }
        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
            track.stop()
        }
        track.reloadStaticData()
        track.play()
    }
}
