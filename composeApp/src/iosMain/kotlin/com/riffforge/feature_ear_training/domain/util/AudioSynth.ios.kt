package com.riffforge.feature_ear_training.domain.util

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

// Keep a strong reference to the active player so ARC doesn't deallocate it mid-playback
private var activeAudioPlayer: AVAudioPlayer? = null

@OptIn(ExperimentalForeignApi::class)
actual fun playPcmSamples(samples: ShortArray, sampleRate: Int) {
    if (samples.isEmpty()) return
    runCatching {
        val session = AVAudioSession.sharedInstance()
        if (session.category != AVAudioSessionCategoryPlayAndRecord) {
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        }

        val wavBytes = encodeWav16BitMono(samples, sampleRate)
        val nsData = wavBytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = wavBytes.size.toULong())
        }

        activeAudioPlayer?.stop()
        val player = AVAudioPlayer(data = nsData, error = null)
        activeAudioPlayer = player
        player?.prepareToPlay()
        player?.play()
    }
}

private fun encodeWav16BitMono(samples: ShortArray, sampleRate: Int): ByteArray {
    val dataSize = samples.size * 2
    val totalSize = 44 + dataSize
    val out = ByteArray(totalSize)

    fun writeAscii(offset: Int, s: String) {
        for (i in s.indices) {
            out[offset + i] = s[i].code.toByte()
        }
    }

    fun writeIntLE(offset: Int, value: Int) {
        out[offset] = (value and 0xFF).toByte()
        out[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        out[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        out[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    fun writeShortLE(offset: Int, value: Int) {
        out[offset] = (value and 0xFF).toByte()
        out[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    }

    writeAscii(0, "RIFF")
    writeIntLE(4, 36 + dataSize)
    writeAscii(8, "WAVE")
    writeAscii(12, "fmt ")
    writeIntLE(16, 16) // PCM chunk size
    writeShortLE(20, 1) // AudioFormat = 1 (PCM)
    writeShortLE(22, 1) // NumChannels = 1 (Mono)
    writeIntLE(24, sampleRate)
    writeIntLE(28, sampleRate * 2) // ByteRate
    writeShortLE(32, 2) // BlockAlign
    writeShortLE(34, 16) // BitsPerSample
    writeAscii(36, "data")
    writeIntLE(40, dataSize)

    var idx = 44
    for (sample in samples) {
        val v = sample.toInt()
        out[idx++] = (v and 0xFF).toByte()
        out[idx++] = ((v ushr 8) and 0xFF).toByte()
    }
    return out
}
