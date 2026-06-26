package com.riffforge.feature_theory.domain.util

import com.riffforge.feature_theory.domain.model.KeySignature

object MusicTheoryData {
    val circleOfFifths = listOf(
        KeySignature(0, "C", "Am", "0", listOf("C", "Dm", "Em", "F", "G", "Am", "Bdim")),
        KeySignature(1, "G", "Em", "1#", listOf("G", "Am", "Bm", "C", "D", "Em", "F#dim")),
        KeySignature(2, "D", "Bm", "2#", listOf("D", "Em", "F#m", "G", "A", "Bm", "C#dim")),
        KeySignature(3, "A", "F#m", "3#", listOf("A", "Bm", "C#m", "D", "E", "F#m", "G#dim")),
        KeySignature(4, "E", "C#m", "4#", listOf("E", "F#m", "G#m", "A", "B", "C#m", "D#dim")),
        KeySignature(5, "B", "G#m", "5#", listOf("B", "C#m", "D#m", "E", "F#", "G#m", "A#dim")),
        KeySignature(6, "F# / Gb", "D#m / Ebm", "6# / 6b", listOf("F#", "G#m", "A#m", "B", "C#", "D#m", "E#dim")),
        KeySignature(7, "Db", "Bbm", "5b", listOf("Db", "Ebm", "Fm", "Gb", "Ab", "Bbm", "Cdim")),
        KeySignature(8, "Ab", "Fm", "4b", listOf("Ab", "Bbm", "Cm", "Db", "Eb", "Fm", "Gdim")),
        KeySignature(9, "Eb", "Cm", "3b", listOf("Eb", "Fm", "Gm", "Ab", "Bb", "Cm", "Ddim")),
        KeySignature(10, "Bb", "Gm", "2b", listOf("Bb", "Cm", "Dm", "Eb", "F", "Gm", "Adim")),
        KeySignature(11, "F", "Dm", "1b", listOf("F", "Gm", "Am", "Bb", "C", "Dm", "Edim"))
    )
}