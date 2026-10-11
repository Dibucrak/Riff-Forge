package com.riffforge.feature_metronome.presentation

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.darwin.UInt32

actual fun playMetronomeClick(isAccent: Boolean, beat: Int) {
    runCatching {
        // Tink.caf (1057) = short high-pitched "tink" for the first beat accent
        // Tock.caf (1306) = short keyboard click for regular beats
        // Both are short, crisp, and sound like a real metronome
        val soundId: UInt32 = if (isAccent) 1057u else 1306u
        AudioServicesPlaySystemSound(soundId)
    }
    // Silently fail if audio is not available on this device
}
