package com.riffforge.core.domain.util

import com.riffforge.shared.domain.util.ChordTransposer as SharedChordTransposer

object NotationConverter {
    fun formatNote(note: String, useSpanish: Boolean): String {
        return SharedChordTransposer.getNoteDisplayName(note, useSpanish)
    }

    fun formatChord(chord: String, useSpanish: Boolean): String {
        return SharedChordTransposer.convertChordDisplayName(chord, useSpanish)
    }

    fun convertLyricsText(content: String, useSpanish: Boolean): String {
        return SharedChordTransposer.convertNoteSystem(content, useSpanish)
    }
}

object ChordTransposer {
    fun transposeChord(chord: String, semitones: Int, preferFlats: Boolean = false): String {
        return SharedChordTransposer.transposeChord(chord, semitones, preferFlats)
    }

    fun transposeText(content: String, semitones: Int, preferFlats: Boolean = false): String {
        return SharedChordTransposer.transposeContent(content, semitones, preferFlats)
    }
}
