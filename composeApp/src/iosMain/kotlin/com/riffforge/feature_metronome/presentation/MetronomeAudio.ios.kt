package com.riffforge.feature_metronome.presentation

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

private val cachedPlayers = mutableMapOf<Pair<MetronomeSound, Boolean>, AVAudioPlayer>()

@OptIn(ExperimentalForeignApi::class)
private fun encodeWavMono16(samples: ShortArray, sampleRate: Int): ByteArray {
    val dataSize = samples.size * 2
    val wav = ByteArray(44 + dataSize)

    fun writeAscii(offset: Int, s: String) {
        for (i in s.indices) wav[offset + i] = s[i].code.toByte()
    }

    fun writeIntLE(offset: Int, value: Int) {
        wav[offset] = (value and 0xFF).toByte()
        wav[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        wav[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        wav[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    fun writeShortLE(offset: Int, value: Int) {
        wav[offset] = (value and 0xFF).toByte()
        wav[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    }

    writeAscii(0, "RIFF")
    writeIntLE(4, 36 + dataSize)
    writeAscii(8, "WAVE")
    writeAscii(12, "fmt ")
    writeIntLE(16, 16)
    writeShortLE(20, 1)
    writeShortLE(22, 1)
    writeIntLE(24, sampleRate)
    writeIntLE(28, sampleRate * 2)
    writeShortLE(32, 2)
    writeShortLE(34, 16)
    writeAscii(36, "data")
    writeIntLE(40, dataSize)

    var idx = 44
    for (sample in samples) {
        val s = sample.toInt()
        wav[idx++] = (s and 0xFF).toByte()
        wav[idx++] = ((s ushr 8) and 0xFF).toByte()
    }
    return wav
}

@OptIn(ExperimentalForeignApi::class)
private fun buildClickPlayer(isAccent: Boolean, sound: MetronomeSound): AVAudioPlayer? {
    val samples = MetronomeSynth.generateClickPcm(isAccent, sound)
    val wav = encodeWavMono16(samples, MetronomeSynth.SAMPLE_RATE)
    val nsData = wav.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = wav.size.toULong())
    }
    return AVAudioPlayer(data = nsData, error = null)?.apply {
        prepareToPlay()
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun playMetronomeClick(isAccent: Boolean, beat: Int, sound: MetronomeSound) {
    runCatching {
        val session = AVAudioSession.sharedInstance()
        if (session.category != AVAudioSessionCategoryPlayAndRecord) {
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        }
        session.setActive(true, error = null)

        val key = sound to isAccent
        val player = cachedPlayers[key] ?: buildClickPlayer(isAccent, sound)?.also {
            cachedPlayers[key] = it
        }
        player?.apply {
            currentTime = 0.0
            play()
        }
    }
}
