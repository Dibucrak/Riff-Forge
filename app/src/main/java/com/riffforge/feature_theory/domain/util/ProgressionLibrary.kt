package com.riffforge.feature_theory.domain.util

import com.riffforge.feature_theory.domain.model.Progression

object ProgressionLibrary {
    val genres = listOf("Rock", "Metal", "Blues", "Pop", "Jazz")

    val progressions = listOf(
        // Rock
        Progression("1", "Rock", "Clásico de Arena", "I - bVII - IV - I", listOf("E", "D", "A", "E"), "Progresión potente y épica, muy utilizada en himnos de estadios y hard rock."),
        Progression("2", "Rock", "Cadencia Andaluza (Rock)", "i - bVII - bVI - V", listOf("Am", "G", "F", "E"), "Descenso dramático que aporta un toque neoclásico o hispano a los solos de rock."),

        // Metal
        Progression("3", "Metal", "Tensión Frigia", "i - bII - i - vii°", listOf("Em", "F", "Em", "D#dim"), "Genera una atmósfera muy oscura y opresiva, ideal para riffs pesados y cortantes."),
        Progression("4", "Metal", "Doom Descent", "i - bVI - iv - V", listOf("Dm", "Bb", "Gm", "A"), "Una base lenta y pesada, perfecta para subgéneros como el Doom o el Gothic Metal."),

        // Blues
        Progression("5", "Blues", "12-Bar Blues", "I7 - IV7 - I7 - V7", listOf("E7", "A7", "E7", "B7"), "La estructura fundamental del blues tradicional de 12 compases."),
        Progression("6", "Blues", "Turnaround Clásico", "V7 - IV7 - I7 - V7", listOf("B7", "A7", "E7", "B7"), "El remate típico al final de un ciclo de blues para volver a empezar con fuerza."),

        // Pop
        Progression("7", "Pop", "La Rueda Mágica", "I - V - vi - IV", listOf("C", "G", "Am", "F"), "La progresión más exitosa de la historia del pop; la base de cientos de hits mundiales."),
        Progression("8", "Pop", "Sentimental", "vi - IV - I - V", listOf("Am", "F", "C", "G"), "Variación melancólica de la rueda pop, comenzando en el acorde menor relativo."),

        // Jazz
        Progression("9", "Jazz", "El ii-V-I", "ii7 - V7 - Imaj7", listOf("Dm7", "G7", "Cmaj7"), "El bloque de construcción más importante en el jazz y el swing."),
        Progression("10", "Jazz", "Turnaround de Jazz", "Imaj7 - vi7 - ii7 - V7", listOf("Cmaj7", "Am7", "Dm7", "G7"), "Ciclo rítmico que proporciona una base perfecta para la improvisación modal.")
    )
}