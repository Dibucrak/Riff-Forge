package com.riffforge.feature_chords.domain.util

import com.riffforge.feature_chords.domain.model.Chord

object ChordLibrary {
    val roots = listOf("C", "D", "E", "F", "G", "A", "B")
    val types = listOf("Mayor", "Menor", "7")

    val chords = listOf(
        // C
        Chord("C", "Mayor", listOf(-1, 3, 2, 0, 1, 0), listOf(0, 3, 2, 0, 1, 0)),
        Chord("C", "Menor", listOf(-1, 3, 5, 5, 4, 3), listOf(0, 1, 3, 4, 2, 1), startingFret = 3),
        Chord("C", "7", listOf(-1, 3, 2, 3, 1, 0), listOf(0, 3, 2, 4, 1, 0)),

        // D
        Chord("D", "Mayor", listOf(-1, -1, 0, 2, 3, 2), listOf(0, 0, 0, 1, 3, 2)),
        Chord("D", "Menor", listOf(-1, -1, 0, 2, 3, 1), listOf(0, 0, 0, 2, 3, 1)),
        Chord("D", "7", listOf(-1, -1, 0, 2, 1, 2), listOf(0, 0, 0, 2, 1, 3)),

        // E
        Chord("E", "Mayor", listOf(0, 2, 2, 1, 0, 0), listOf(0, 2, 3, 1, 0, 0)),
        Chord("E", "Menor", listOf(0, 2, 2, 0, 0, 0), listOf(0, 2, 3, 0, 0, 0)),
        Chord("E", "7", listOf(0, 2, 0, 1, 0, 0), listOf(0, 2, 0, 1, 0, 0)),

        // F
        Chord("F", "Mayor", listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1)),
        Chord("F", "Menor", listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1)),
        Chord("F", "7", listOf(1, 3, 1, 2, 1, 1), listOf(1, 3, 1, 2, 1, 1)),

        // G
        Chord("G", "Mayor", listOf(3, 2, 0, 0, 3, 3), listOf(2, 1, 0, 0, 3, 4)),
        Chord("G", "Menor", listOf(3, 5, 5, 3, 3, 3), listOf(1, 3, 4, 1, 1, 1), startingFret = 3),
        Chord("G", "7", listOf(3, 2, 0, 0, 0, 1), listOf(3, 2, 0, 0, 0, 1)),

        // A
        Chord("A", "Mayor", listOf(-1, 0, 2, 2, 2, 0), listOf(0, 0, 1, 2, 3, 0)),
        Chord("A", "Menor", listOf(-1, 0, 2, 2, 1, 0), listOf(0, 0, 2, 3, 1, 0)),
        Chord("A", "7", listOf(-1, 0, 2, 0, 2, 0), listOf(0, 0, 2, 0, 3, 0)),

        // B
        Chord("B", "Mayor", listOf(-1, 2, 4, 4, 4, 2), listOf(0, 1, 2, 3, 4, 1)),
        Chord("B", "Menor", listOf(-1, 2, 4, 4, 3, 2), listOf(0, 1, 3, 4, 2, 1)),
        Chord("B", "7", listOf(-1, 2, 1, 2, 0, 2), listOf(0, 2, 1, 3, 0, 4))
    )
}