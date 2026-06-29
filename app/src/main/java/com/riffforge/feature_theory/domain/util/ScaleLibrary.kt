package com.riffforge.feature_theory.domain.util

import com.riffforge.feature_theory.domain.model.Scale

object ScaleLibrary {
    val roots = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    val scales = listOf(
        Scale("Mayor (Jónica)", listOf(0, 2, 4, 5, 7, 9, 11), "La escala fundamental. Alegre y brillante."),
        Scale("Menor Natural (Eólica)", listOf(0, 2, 3, 5, 7, 8, 10), "Melancólica y triste. Muy usada en rock y baladas."),
        Scale("Pentatónica Menor", listOf(0, 3, 5, 7, 10), "La reina del Rock, Blues y Metal. Cinco notas esenciales."),
        Scale("Pentatónica Mayor", listOf(0, 2, 4, 7, 9), "Sonido abierto y alegre, ideal para country y rock sureño."),
        Scale("Blues", listOf(0, 3, 5, 6, 7, 10), "Pentatónica menor con el añadido de la 'Blue note' (tritono)."),
        Scale("Dórica", listOf(0, 2, 3, 5, 7, 9, 10), "Modo menor con sexta mayor. Sonido jazzero, funk y rock progresivo."),
        Scale("Frigia", listOf(0, 1, 3, 5, 7, 8, 10), "Modo menor con segunda menor. Sonido español, oscuro y metalero."),
        Scale("Lidia", listOf(0, 2, 4, 6, 7, 9, 11), "Modo mayor con cuarta aumentada. Sonido flotante y onírico."),
        Scale("Mixolidia", listOf(0, 2, 4, 5, 7, 9, 10), "Modo mayor con séptima menor. Base del rock clásico y blues mayor."),
        Scale("Menor Armónica", listOf(0, 2, 3, 5, 7, 8, 11), "Sonido neoclásico y oriental, muy utilizado por shredders.")
    )
}