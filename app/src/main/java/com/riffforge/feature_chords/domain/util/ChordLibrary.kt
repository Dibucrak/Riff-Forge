package com.riffforge.feature_chords.domain.util

import com.riffforge.feature_chords.domain.model.Chord

object ChordLibrary {
    val roots = listOf("C", "D", "E", "F", "G", "A", "B")
    val types = listOf("Mayor", "Menor", "7", "Maj7", "Min7")

    val chords = listOf(
        // C (Do)
        Chord(id = "C_maj", root = "C", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(-1, 3, 2, 0, 1, 0), isBarre = false),
        Chord(id = "C_min", root = "C", type = "Menor", difficulty = "Media", stringFrets = listOf(-1, 3, 5, 5, 4, 3), isBarre = true, barreFret = 3),
        Chord(id = "C_7", root = "C", type = "7", difficulty = "Fácil", stringFrets = listOf(-1, 3, 2, 3, 1, 0), isBarre = false),
        Chord(id = "C_maj7", root = "C", type = "Maj7", difficulty = "Media", stringFrets = listOf(-1, 3, 2, 0, 0, 0), isBarre = false),
        Chord(id = "C_min7", root = "C", type = "Min7", difficulty = "Media", stringFrets = listOf(-1, 3, 5, 3, 4, 3), isBarre = true, barreFret = 3),

        // D (Re)
        Chord(id = "D_maj", root = "D", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 3, 2), isBarre = false),
        Chord(id = "D_min", root = "D", type = "Menor", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 3, 1), isBarre = false),
        Chord(id = "D_7", root = "D", type = "7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 1, 2), isBarre = false),
        Chord(id = "D_maj7", root = "D", type = "Maj7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 2, 2), isBarre = true, barreFret = 2),
        Chord(id = "D_min7", root = "D", type = "Min7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 1, 1), isBarre = true, barreFret = 1),

        // E (Mi)
        Chord(id = "E_maj", root = "E", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(0, 2, 2, 1, 0, 0), isBarre = false),
        Chord(id = "E_min", root = "E", type = "Menor", difficulty = "Fácil", stringFrets = listOf(0, 2, 2, 0, 0, 0), isBarre = false),
        Chord(id = "E_7", root = "E", type = "7", difficulty = "Fácil", stringFrets = listOf(0, 2, 0, 1, 0, 0), isBarre = false),
        Chord(id = "E_maj7", root = "E", type = "Maj7", difficulty = "Media", stringFrets = listOf(0, 2, 1, 1, 0, 0), isBarre = false),
        Chord(id = "E_min7", root = "E", type = "Min7", difficulty = "Fácil", stringFrets = listOf(0, 2, 0, 0, 0, 0), isBarre = false),

        // F (Fa)
        Chord(id = "F_maj", root = "F", type = "Mayor", difficulty = "Media", stringFrets = listOf(1, 3, 3, 2, 1, 1), isBarre = true, barreFret = 1),
        Chord(id = "F_min", root = "F", type = "Menor", difficulty = "Difícil", stringFrets = listOf(1, 3, 3, 1, 1, 1), isBarre = true, barreFret = 1),
        Chord(id = "F_7", root = "F", type = "7", difficulty = "Media", stringFrets = listOf(1, 3, 1, 2, 1, 1), isBarre = true, barreFret = 1),
        Chord(id = "F_maj7", root = "F", type = "Maj7", difficulty = "Media", stringFrets = listOf(-1, -1, 3, 2, 1, 0), isBarre = false),
        Chord(id = "F_min7", root = "F", type = "Min7", difficulty = "Difícil", stringFrets = listOf(1, 3, 1, 1, 1, 1), isBarre = true, barreFret = 1),

        // G (Sol)
        Chord(id = "G_maj", root = "G", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(3, 2, 0, 0, 0, 3), isBarre = false),
        Chord(id = "G_min", root = "G", type = "Menor", difficulty = "Difícil", stringFrets = listOf(3, 5, 5, 3, 3, 3), isBarre = true, barreFret = 3),
        Chord(id = "G_7", root = "G", type = "7", difficulty = "Fácil", stringFrets = listOf(3, 2, 0, 0, 0, 1), isBarre = false),
        Chord(id = "G_maj7", root = "G", type = "Maj7", difficulty = "Media", stringFrets = listOf(3, -1, 0, 0, 0, 2), isBarre = false),
        Chord(id = "G_min7", root = "G", type = "Min7", difficulty = "Media", stringFrets = listOf(3, 5, 3, 3, 3, 3), isBarre = true, barreFret = 3),

        // A (La)
        Chord(id = "A_maj", root = "A", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 2, 2, 0), isBarre = false),
        Chord(id = "A_min", root = "A", type = "Menor", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 2, 1, 0), isBarre = false),
        Chord(id = "A_7", root = "A", type = "7", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 0, 2, 0), isBarre = false),
        Chord(id = "A_maj7", root = "A", type = "Maj7", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 1, 2, 0), isBarre = false),
        Chord(id = "A_min7", root = "A", type = "Min7", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 0, 1, 0), isBarre = false),

        // B (Si)
        Chord(id = "B_maj", root = "B", type = "Mayor", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 4, 4, 2), isBarre = true, barreFret = 2),
        Chord(id = "B_min", root = "B", type = "Menor", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 4, 3, 2), isBarre = true, barreFret = 2),
        Chord(id = "B_7", root = "B", type = "7", difficulty = "Media", stringFrets = listOf(-1, 2, 1, 2, 0, 2), isBarre = false),
        Chord(id = "B_maj7", root = "B", type = "Maj7", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 3, 4, 2), isBarre = true, barreFret = 2),
        Chord(id = "B_min7", root = "B", type = "Min7", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 2, 3, 2), isBarre = true, barreFret = 2)
    )
}