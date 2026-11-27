package com.riffforge.feature_songs.domain.util

object ChordTransposer {

    private val sharpNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val flatNotes = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    private val enharmonicSemitones = mapOf(
        "C" to 0, "B#" to 0,
        "C#" to 1, "Db" to 1,
        "D" to 2,
        "D#" to 3, "Eb" to 3,
        "E" to 4, "Fb" to 4,
        "F" to 5, "E#" to 5,
        "F#" to 6, "Gb" to 6,
        "G" to 7,
        "G#" to 8, "Ab" to 8,
        "A" to 9,
        "A#" to 10, "Bb" to 10,
        "B" to 11, "Cb" to 11
    )

    private val enharmonicToSpanish = mapOf(
        "E#" to "Mi#",
        "B#" to "Si#",
        "Fb" to "Fab",
        "Cb" to "Dob"
    )

    private val spanishToAmerican = mapOf(
        "Do" to "C", "Do#" to "C#", "Reb" to "Db",
        "Re" to "D", "Re#" to "D#", "Mib" to "Eb",
        "Mi" to "E", "Mi#" to "E#", "Fab" to "Fb",
        "Fa" to "F", "Fa#" to "F#", "Solb" to "Gb",
        "Sol" to "G", "Sol#" to "G#", "Lab" to "Ab",
        "La" to "A", "La#" to "A#", "Sib" to "Bb",
        "Si" to "B", "Si#" to "B#", "Dob" to "Cb"
    )

    // Spanish note names (Cifrado hispano): Do, Re, Mi, Fa, Sol, La, Si
    private val spanishNotesSharps = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    private val spanishNotesFlats = listOf("Do", "Reb", "Re", "Mib", "Mi", "Fa", "Solb", "Sol", "Lab", "La", "Sib", "Si")

    val chordRegex = Regex(
        """(?<![A-Za-z])([A-G](?:#|b)?)(m7b5|maj7|min7|dim7|aug7|sus2|sus4|add9|maj|min|dim|aug|sus|m)?(\d+)?(?:/([A-G](?:#|b)?))?(?![A-Za-z0-9#b])"""
    )
    val spanishChordRegex = Regex(
        """(?<![A-Za-z])(Sol|Do|Re|Mi|Fa|La|Si)(#|b)?(m7b5|maj7|min7|dim7|aug7|sus2|sus4|add9|maj|min|dim|aug|sus|m)?(\d+)?(?:/(Sol|Do|Re|Mi|Fa|La|Si)(#|b)?)?(?![A-Za-z0-9#b])"""
    )
    private val bracketedChordRegex = Regex("""\[([^\]]+)\]""")

    private fun isValidChordList(text: String, regex: Regex): Boolean {
        val tokens = text.trim().split(Regex("""\s+""")).filter { it.isNotEmpty() }
        return tokens.isNotEmpty() && tokens.all { regex.matches(it) }
    }

    /**
     * Convert an American note name to Spanish notation.
     * C→Do, D→Re, E→Mi, F→Fa, G→Sol, A→La, B→Si
     */
    fun toSpanishNote(note: String): String {
        enharmonicToSpanish[note]?.let { return it }
        val index = sharpNotes.indexOf(note)
        if (index == -1) {
            val flatIndex = flatNotes.indexOf(note)
            if (flatIndex == -1) return note
            return if (note.contains("b")) spanishNotesFlats[flatIndex] else spanishNotesSharps[flatIndex]
        }
        return spanishNotesSharps[index]
    }

    /**
     * Convert a Spanish note name back to American notation.
     * Do→C, Re→D, Mi→E, Fa→F, Sol→G, La→A, Si→B
     */
    fun fromSpanishNote(note: String): String {
        spanishToAmerican[note]?.let { return it }
        val index = spanishNotesSharps.indexOf(note)
        if (index != -1) {
            return if (note.contains("#") || note.contains("b")) {
                if (note.contains("b")) flatNotes[index] else sharpNotes[index]
            } else sharpNotes[index]
        }
        val flatIndex = spanishNotesFlats.indexOf(note)
        if (flatIndex != -1) return flatNotes[flatIndex]
        return note
    }

    /**
     * Convert a single chord or key symbol (e.g., "Am", "F#m7b5", "C/G", "Bb") to Spanish or American notation.
     */
    fun convertChordDisplayName(chord: String, useSpanish: Boolean): String {
        if (!useSpanish) return chord
        return convertLineSystem(chord, toSpanish = true)
    }

    /**
     * Convert chord content from American to Spanish notation (or vice versa).
     */
    fun convertNoteSystem(content: String, toSpanish: Boolean): String {
        val lines = content.split("\n")
        val targetRegex = if (toSpanish) chordRegex else spanishChordRegex
        return lines.map { line ->
            when {
                line.contains("[") && line.contains("]") -> {
                    bracketedChordRegex.replace(line) { matchResult ->
                        val innerChord = matchResult.groups[1]?.value ?: ""
                        if (isValidChordList(innerChord, targetRegex)) {
                            "[${convertLineSystem(innerChord, toSpanish = toSpanish)}]"
                        } else {
                            matchResult.value
                        }
                    }
                }
                isChordLine(line, toSpanish) -> convertLineSystem(line, toSpanish = toSpanish)
                else -> line
            }
        }.joinToString("\n")
    }

    private fun convertLineSystem(line: String, toSpanish: Boolean): String {
        if (toSpanish) {
            return chordRegex.replace(line) { matchResult ->
                val rootNote = matchResult.groups[1]?.value ?: ""
                val modifier = matchResult.groups[2]?.value ?: ""
                val extension = matchResult.groups[3]?.value ?: ""
                val bassNote = matchResult.groups[4]?.value

                val convertedRoot = toSpanishNote(rootNote)
                val convertedBass = bassNote?.let { toSpanishNote(it) }

                buildString {
                    append(convertedRoot)
                    append(modifier)
                    append(extension)
                    if (convertedBass != null) {
                        append("/$convertedBass")
                    }
                }
            }
        } else {
            return spanishChordRegex.replace(line) { matchResult ->
                val rootBase = matchResult.groups[1]?.value ?: ""
                val rootAccidental = matchResult.groups[2]?.value ?: ""
                val modifier = matchResult.groups[3]?.value ?: ""
                val extension = matchResult.groups[4]?.value ?: ""
                val bassBase = matchResult.groups[5]?.value
                val bassAccidental = matchResult.groups[6]?.value ?: ""

                val convertedRoot = fromSpanishNote(rootBase + rootAccidental)
                val convertedBass = bassBase?.let { fromSpanishNote(it + bassAccidental) }

                buildString {
                    append(convertedRoot)
                    append(modifier)
                    append(extension)
                    if (convertedBass != null) {
                        append("/$convertedBass")
                    }
                }
            }
        }
    }

    /**
     * Get the display name for a note root, respecting the Spanish notation preference.
     */
    fun getNoteDisplayName(note: String, useSpanish: Boolean): String {
        if (!useSpanish) return note
        val direct = toSpanishNote(note)
        return if (direct != note) direct else convertChordDisplayName(note, true)
    }

    fun transposeContent(content: String, semitones: Int, preferFlats: Boolean = false): String {
        if (semitones == 0) return content

        val lines = content.split("\n")
        val transposedLines = lines.map { line ->
            when {
                line.contains("[") && line.contains("]") -> {
                    bracketedChordRegex.replace(line) { matchResult ->
                        val innerChord = matchResult.groups[1]?.value ?: ""
                        if (isValidChordList(innerChord, chordRegex)) {
                            "[${transposeLine(innerChord, semitones, preferFlats)}]"
                        } else {
                            matchResult.value
                        }
                    }
                }
                isChordLine(line, toSpanish = true) -> transposeLine(line, semitones, preferFlats)
                else -> line
            }
        }
        return transposedLines.joinToString("\n")
    }

    fun isChordLine(line: String, toSpanish: Boolean = true): Boolean {
        if (line.trim().isEmpty()) return false
        val regex = if (toSpanish) chordRegex else spanishChordRegex
        val words = line.trim().split(Regex("""\s+"""))
        var chordCount = 0

        for (word in words) {
            if (regex.matches(word)) {
                chordCount++
            }
        }

        return (chordCount.toFloat() / words.size) >= 0.5f
    }

    private fun transposeLine(line: String, semitones: Int, preferFlats: Boolean): String {
        return chordRegex.replace(line) { matchResult ->
            val rootNote = matchResult.groups[1]?.value ?: ""
            val modifier = matchResult.groups[2]?.value ?: ""
            val extension = matchResult.groups[3]?.value ?: ""
            val bassNote = matchResult.groups[4]?.value

            val transposedRoot = shiftNote(rootNote, semitones, preferFlats)
            val transposedBass = bassNote?.let { shiftNote(it, semitones, preferFlats) }

            buildString {
                append(transposedRoot)
                append(modifier)
                append(extension)
                if (transposedBass != null) {
                    append("/$transposedBass")
                }
            }
        }
    }

    fun shiftNote(note: String, semitones: Int, preferFlats: Boolean): String {
        val match = chordRegex.matchEntire(note.trim())
        val baseNote = match?.groups?.get(1)?.value ?: note.trim()
        val suffix = if (match != null) {
            note.trim().removePrefix(baseNote)
        } else ""

        val index = enharmonicSemitones[baseNote] ?: return note

        val newIndex = ((index + semitones) % 12 + 12) % 12

        val shiftedRoot = if (preferFlats) {
            flatNotes[newIndex]
        } else {
            sharpNotes[newIndex]
        }
        return shiftedRoot + suffix
    }
}