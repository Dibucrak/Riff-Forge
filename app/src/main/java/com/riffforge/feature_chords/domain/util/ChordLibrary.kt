package com.riffforge.feature_chords.domain.util

import com.riffforge.feature_chords.domain.model.Chord

object ChordLibrary {
    val roots = listOf("C", "D", "E", "F", "G", "A", "B")
    val types = listOf(
        "Mayor",
        "Mayor (Cejilla)",
        "Menor",
        "7",
        "Maj7",
        "Min7",
        "Sus4",
        "Sus2",
        "Dim",
        "Aug"
    )

    val chords = listOf(
        // ── C (Do) ─────────────────────────────────────────────────────────────
        Chord(id = "C_maj", root = "C", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(-1, 3, 2, 0, 1, 0), fingers = listOf(0, 3, 2, 0, 1, 0), isBarre = false, startingFret = 1),
        Chord(id = "C_maj_barre", root = "C", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(-1, 3, 5, 5, 5, 3), fingers = listOf(0, 1, 2, 3, 4, 1), isBarre = true, barreFret = 3, startingFret = 3),
        Chord(id = "C_min", root = "C", type = "Menor", difficulty = "Media", stringFrets = listOf(-1, 3, 5, 5, 4, 3), fingers = listOf(0, 1, 3, 4, 2, 1), isBarre = true, barreFret = 3, startingFret = 3),
        Chord(id = "C_7", root = "C", type = "7", difficulty = "Fácil", stringFrets = listOf(-1, 3, 2, 3, 1, 0), fingers = listOf(0, 3, 2, 4, 1, 0), isBarre = false, startingFret = 1),
        Chord(id = "C_maj7", root = "C", type = "Maj7", difficulty = "Media", stringFrets = listOf(-1, 3, 2, 0, 0, 0), fingers = listOf(0, 3, 2, 0, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "C_min7", root = "C", type = "Min7", difficulty = "Media", stringFrets = listOf(-1, 3, 5, 3, 4, 3), fingers = listOf(0, 1, 3, 1, 2, 1), isBarre = true, barreFret = 3, startingFret = 3),
        Chord(id = "C_sus4", root = "C", type = "Sus4", difficulty = "Media", stringFrets = listOf(-1, 3, 3, 0, 1, 1), fingers = listOf(0, 3, 4, 0, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "C_sus2", root = "C", type = "Sus2", difficulty = "Fácil", stringFrets = listOf(-1, 3, 0, 0, 1, 0), fingers = listOf(0, 3, 0, 0, 1, 0), isBarre = false, startingFret = 1),
        Chord(id = "C_dim", root = "C", type = "Dim", difficulty = "Difícil", stringFrets = listOf(-1, 3, 4, 2, 4, -1), fingers = listOf(0, 2, 3, 1, 4, 0), isBarre = false, startingFret = 2),
        Chord(id = "C_aug", root = "C", type = "Aug", difficulty = "Media", stringFrets = listOf(-1, 3, 2, 1, 1, 0), fingers = listOf(0, 4, 3, 1, 2, 0), isBarre = false, startingFret = 1),

        // ── D (Re) ─────────────────────────────────────────────────────────────
        Chord(id = "D_maj", root = "D", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 3, 2), fingers = listOf(0, 0, 0, 1, 3, 2), isBarre = false, startingFret = 1),
        Chord(id = "D_maj_barre", root = "D", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(-1, 5, 7, 7, 7, 5), fingers = listOf(0, 1, 2, 3, 4, 1), isBarre = true, barreFret = 5, startingFret = 5),
        Chord(id = "D_min", root = "D", type = "Menor", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 3, 1), fingers = listOf(0, 0, 0, 2, 3, 1), isBarre = false, startingFret = 1),
        Chord(id = "D_7", root = "D", type = "7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 1, 2), fingers = listOf(0, 0, 0, 2, 1, 3), isBarre = false, startingFret = 1),
        Chord(id = "D_maj7", root = "D", type = "Maj7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 2, 2), fingers = listOf(0, 0, 0, 1, 1, 1), isBarre = true, barreFret = 2, startingFret = 1),
        Chord(id = "D_min7", root = "D", type = "Min7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 1, 1), fingers = listOf(0, 0, 0, 2, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "D_sus4", root = "D", type = "Sus4", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 3, 3), fingers = listOf(0, 0, 0, 1, 2, 3), isBarre = false, startingFret = 1),
        Chord(id = "D_sus2", root = "D", type = "Sus2", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 2, 3, 0), fingers = listOf(0, 0, 0, 1, 2, 0), isBarre = false, startingFret = 1),
        Chord(id = "D_dim", root = "D", type = "Dim", difficulty = "Media", stringFrets = listOf(-1, -1, 0, 1, 3, 1), fingers = listOf(0, 0, 0, 1, 3, 2), isBarre = false, startingFret = 1),
        Chord(id = "D_aug", root = "D", type = "Aug", difficulty = "Media", stringFrets = listOf(-1, -1, 0, 3, 3, 2), fingers = listOf(0, 0, 0, 2, 3, 1), isBarre = false, startingFret = 1),

        // ── E (Mi) ─────────────────────────────────────────────────────────────
        Chord(id = "E_maj", root = "E", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(0, 2, 2, 1, 0, 0), fingers = listOf(0, 2, 3, 1, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_maj_barre", root = "E", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(-1, 7, 9, 9, 9, 7), fingers = listOf(0, 1, 2, 3, 4, 1), isBarre = true, barreFret = 7, startingFret = 7),
        Chord(id = "E_min", root = "E", type = "Menor", difficulty = "Fácil", stringFrets = listOf(0, 2, 2, 0, 0, 0), fingers = listOf(0, 2, 3, 0, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_7", root = "E", type = "7", difficulty = "Fácil", stringFrets = listOf(0, 2, 0, 1, 0, 0), fingers = listOf(0, 2, 0, 1, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_maj7", root = "E", type = "Maj7", difficulty = "Media", stringFrets = listOf(0, 2, 1, 1, 0, 0), fingers = listOf(0, 3, 1, 2, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_min7", root = "E", type = "Min7", difficulty = "Fácil", stringFrets = listOf(0, 2, 0, 0, 0, 0), fingers = listOf(0, 2, 0, 0, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_sus4", root = "E", type = "Sus4", difficulty = "Fácil", stringFrets = listOf(0, 2, 2, 2, 0, 0), fingers = listOf(0, 2, 3, 4, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_sus2", root = "E", type = "Sus2", difficulty = "Media", stringFrets = listOf(0, 2, 4, 4, 0, 0), fingers = listOf(0, 1, 3, 4, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "E_dim", root = "E", type = "Dim", difficulty = "Media", stringFrets = listOf(-1, -1, 2, 3, 2, 3), fingers = listOf(0, 0, 1, 3, 2, 4), isBarre = false, startingFret = 2),
        Chord(id = "E_aug", root = "E", type = "Aug", difficulty = "Media", stringFrets = listOf(0, 2, 2, 1, 1, 0), fingers = listOf(0, 3, 4, 1, 2, 0), isBarre = false, startingFret = 1),

        // ── F (Fa) ─────────────────────────────────────────────────────────────
        Chord(id = "F_maj", root = "F", type = "Mayor", difficulty = "Media", stringFrets = listOf(1, 3, 3, 2, 1, 1), fingers = listOf(1, 3, 4, 2, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "F_maj_barre", root = "F", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(-1, 8, 10, 10, 10, 8), fingers = listOf(0, 1, 2, 3, 4, 1), isBarre = true, barreFret = 8, startingFret = 8),
        Chord(id = "F_min", root = "F", type = "Menor", difficulty = "Difícil", stringFrets = listOf(1, 3, 3, 1, 1, 1), fingers = listOf(1, 3, 4, 1, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "F_7", root = "F", type = "7", difficulty = "Media", stringFrets = listOf(1, 3, 1, 2, 1, 1), fingers = listOf(1, 3, 1, 2, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "F_maj7", root = "F", type = "Maj7", difficulty = "Fácil", stringFrets = listOf(-1, -1, 3, 2, 1, 0), fingers = listOf(0, 0, 3, 2, 1, 0), isBarre = false, startingFret = 1),
        Chord(id = "F_min7", root = "F", type = "Min7", difficulty = "Difícil", stringFrets = listOf(1, 3, 1, 1, 1, 1), fingers = listOf(1, 3, 1, 1, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "F_sus4", root = "F", type = "Sus4", difficulty = "Media", stringFrets = listOf(1, 3, 3, 3, 1, 1), fingers = listOf(1, 2, 3, 4, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "F_sus2", root = "F", type = "Sus2", difficulty = "Media", stringFrets = listOf(-1, -1, 3, 0, 1, 1), fingers = listOf(0, 0, 3, 0, 1, 1), isBarre = true, barreFret = 1, startingFret = 1),
        Chord(id = "F_dim", root = "F", type = "Dim", difficulty = "Fácil", stringFrets = listOf(-1, -1, 0, 1, 0, 1), fingers = listOf(0, 0, 0, 1, 0, 2), isBarre = false, startingFret = 1),
        Chord(id = "F_aug", root = "F", type = "Aug", difficulty = "Media", stringFrets = listOf(-1, -1, 3, 2, 2, 1), fingers = listOf(0, 0, 4, 2, 3, 1), isBarre = false, startingFret = 1),

        // ── G (Sol) ────────────────────────────────────────────────────────────
        Chord(id = "G_maj", root = "G", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(3, 2, 0, 0, 0, 3), fingers = listOf(2, 1, 0, 0, 0, 3), isBarre = false, startingFret = 1),
        Chord(id = "G_maj_barre", root = "G", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(3, 5, 5, 4, 3, 3), fingers = listOf(1, 3, 4, 2, 1, 1), isBarre = true, barreFret = 3, startingFret = 3),
        Chord(id = "G_min", root = "G", type = "Menor", difficulty = "Difícil", stringFrets = listOf(3, 5, 5, 3, 3, 3), fingers = listOf(1, 3, 4, 1, 1, 1), isBarre = true, barreFret = 3, startingFret = 3),
        Chord(id = "G_7", root = "G", type = "7", difficulty = "Fácil", stringFrets = listOf(3, 2, 0, 0, 0, 1), fingers = listOf(3, 2, 0, 0, 0, 1), isBarre = false, startingFret = 1),
        Chord(id = "G_maj7", root = "G", type = "Maj7", difficulty = "Media", stringFrets = listOf(3, -1, 0, 0, 0, 2), fingers = listOf(3, 0, 0, 0, 0, 2), isBarre = false, startingFret = 1),
        Chord(id = "G_min7", root = "G", type = "Min7", difficulty = "Media", stringFrets = listOf(3, 5, 3, 3, 3, 3), fingers = listOf(1, 3, 1, 1, 1, 1), isBarre = true, barreFret = 3, startingFret = 3),
        Chord(id = "G_sus4", root = "G", type = "Sus4", difficulty = "Media", stringFrets = listOf(3, 3, 0, 0, 1, 3), fingers = listOf(2, 3, 0, 0, 1, 4), isBarre = false, startingFret = 1),
        Chord(id = "G_sus2", root = "G", type = "Sus2", difficulty = "Fácil", stringFrets = listOf(3, 0, 0, 0, 3, 3), fingers = listOf(1, 0, 0, 0, 3, 4), isBarre = false, startingFret = 1),
        Chord(id = "G_dim", root = "G", type = "Dim", difficulty = "Media", stringFrets = listOf(-1, -1, 2, 3, 2, 3), fingers = listOf(0, 0, 1, 3, 2, 4), isBarre = false, startingFret = 2),
        Chord(id = "G_aug", root = "G", type = "Aug", difficulty = "Media", stringFrets = listOf(3, 2, 1, 0, 0, 3), fingers = listOf(3, 2, 1, 0, 0, 4), isBarre = false, startingFret = 1),

        // ── A (La) ─────────────────────────────────────────────────────────────
        Chord(id = "A_maj", root = "A", type = "Mayor", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 2, 2, 0), fingers = listOf(0, 0, 1, 2, 3, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_maj_barre", root = "A", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(5, 7, 7, 6, 5, 5), fingers = listOf(1, 3, 4, 2, 1, 1), isBarre = true, barreFret = 5, startingFret = 5),
        Chord(id = "A_min", root = "A", type = "Menor", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 2, 1, 0), fingers = listOf(0, 0, 2, 3, 1, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_7", root = "A", type = "7", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 0, 2, 0), fingers = listOf(0, 0, 1, 0, 2, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_maj7", root = "A", type = "Maj7", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 1, 2, 0), fingers = listOf(0, 0, 2, 1, 3, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_min7", root = "A", type = "Min7", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 0, 1, 0), fingers = listOf(0, 0, 2, 0, 1, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_sus4", root = "A", type = "Sus4", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 2, 3, 0), fingers = listOf(0, 0, 1, 2, 4, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_sus2", root = "A", type = "Sus2", difficulty = "Fácil", stringFrets = listOf(-1, 0, 2, 2, 0, 0), fingers = listOf(0, 0, 1, 2, 0, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_dim", root = "A", type = "Dim", difficulty = "Media", stringFrets = listOf(-1, 0, 1, 2, 1, -1), fingers = listOf(0, 0, 1, 3, 2, 0), isBarre = false, startingFret = 1),
        Chord(id = "A_aug", root = "A", type = "Aug", difficulty = "Media", stringFrets = listOf(-1, 0, 3, 2, 2, 1), fingers = listOf(0, 0, 4, 2, 3, 1), isBarre = false, startingFret = 1),

        // ── B (Si) ─────────────────────────────────────────────────────────────
        Chord(id = "B_maj", root = "B", type = "Mayor", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 4, 4, 2), fingers = listOf(0, 1, 2, 3, 4, 1), isBarre = true, barreFret = 2, startingFret = 2),
        Chord(id = "B_maj_barre", root = "B", type = "Mayor (Cejilla)", difficulty = "Media", stringFrets = listOf(7, 9, 9, 8, 7, 7), fingers = listOf(1, 3, 4, 2, 1, 1), isBarre = true, barreFret = 7, startingFret = 7),
        Chord(id = "B_min", root = "B", type = "Menor", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 4, 3, 2), fingers = listOf(0, 1, 3, 4, 2, 1), isBarre = true, barreFret = 2, startingFret = 2),
        Chord(id = "B_7", root = "B", type = "7", difficulty = "Media", stringFrets = listOf(-1, 2, 1, 2, 0, 2), fingers = listOf(0, 2, 1, 3, 0, 4), isBarre = false, startingFret = 1),
        Chord(id = "B_maj7", root = "B", type = "Maj7", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 3, 4, 2), fingers = listOf(0, 1, 3, 2, 4, 1), isBarre = true, barreFret = 2, startingFret = 2),
        Chord(id = "B_min7", root = "B", type = "Min7", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 2, 3, 2), fingers = listOf(0, 1, 3, 1, 2, 1), isBarre = true, barreFret = 2, startingFret = 2),
        Chord(id = "B_sus4", root = "B", type = "Sus4", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 4, 5, 2), fingers = listOf(0, 1, 2, 3, 4, 1), isBarre = true, barreFret = 2, startingFret = 2),
        Chord(id = "B_sus2", root = "B", type = "Sus2", difficulty = "Media", stringFrets = listOf(-1, 2, 4, 4, 2, 2), fingers = listOf(0, 1, 3, 4, 1, 1), isBarre = true, barreFret = 2, startingFret = 2),
        Chord(id = "B_dim", root = "B", type = "Dim", difficulty = "Media", stringFrets = listOf(-1, 2, 3, 1, 3, -1), fingers = listOf(0, 2, 3, 1, 4, 0), isBarre = false, startingFret = 1),
        Chord(id = "B_aug", root = "B", type = "Aug", difficulty = "Media", stringFrets = listOf(-1, 2, 1, 0, 0, 3), fingers = listOf(0, 2, 1, 0, 0, 3), isBarre = false, startingFret = 1)
    )
}