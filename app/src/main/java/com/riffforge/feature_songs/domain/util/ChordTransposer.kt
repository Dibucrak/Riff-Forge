package com.riffforge.feature_songs.domain.util

object ChordTransposer {

    private val sharpNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val flatNotes = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    // Spanish note names (Cifrado hispano): Do, Re, Mi, Fa, Sol, La, Si
    private val spanishNotesSharps = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    private val spanishNotesFlats = listOf("Do", "Reb", "Re", "Mib", "Mi", "Fa", "Solb", "Sol", "Lab", "La", "Sib", "Si")

    // Spanish chord type names
    private val spanishChordTypes = mapOf(
        "m" to "m",
        "maj" to "maj",
        "min" to "m",
        "dim" to "dim",
        "aug" to "aug",
        "sus" to "sus"
    )

    private val chordRegex = Regex("""\b([A-G](?:#|b)?)(m|maj|min|dim|aug|sus)?(\d)?(?:/([A-G](?:#|b)?))?\b""")

    /**
     * Convert an American note name to Spanish notation.
     * C→Do, D→Re, E→Mi, F→Fa, G→Sol, A→La, B→Si
     */
    fun toSpanishNote(note: String): String {
        val index = sharpNotes.indexOf(note)
        if (index == -1) {
            val flatIndex = flatNotes.indexOf(note)
            if (flatIndex == -1) return note
            return if (note.contains("b")) spanishNotesFlats[flatIndex] else spanishNotesSharps[flatIndex]
        }
        return if (note.contains("#")) spanishNotesSharps[index] else spanishNotesSharps[index]
    }

    /**
     * Convert a Spanish note name back to American notation.
     * Do→C, Re→D, Mi→E, Fa→F, Sol→G, La→A, Si→B
     */
    fun fromSpanishNote(note: String): String {
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
     * Convert chord content from American to Spanish notation (or vice versa).
     */
    fun convertNoteSystem(content: String, toSpanish: Boolean): String {
        if (!toSpanish) {
            // Convert Spanish → American
            val lines = content.split("\n")
            return lines.map { line ->
                if (isChordLine(line)) {
                    convertLineSystem(line, toSpanish = false)
                } else {
                    line
                }
            }.joinToString("\n")
        }
        // Convert American → Spanish
        val lines = content.split("\n")
        return lines.map { line ->
            if (isChordLine(line)) {
                convertLineSystem(line, toSpanish = true)
            } else {
                line
            }
        }.joinToString("\n")
    }

    private fun convertLineSystem(line: String, toSpanish: Boolean): String {
        return chordRegex.replace(line) { matchResult ->
            val rootNote = matchResult.groups[1]?.value ?: ""
            val modifier = matchResult.groups[2]?.value ?: ""
            val extension = matchResult.groups[3]?.value ?: ""
            val bassNote = matchResult.groups[4]?.value

            val convertedRoot = if (toSpanish) toSpanishNote(rootNote) else fromSpanishNote(rootNote)
            val convertedBass = bassNote?.let { if (toSpanish) toSpanishNote(it) else fromSpanishNote(it) }

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

    /**
     * Get the display name for a note root, respecting the Spanish notation preference.
     */
    fun getNoteDisplayName(note: String, useSpanish: Boolean): String {
        return if (useSpanish) toSpanishNote(note) else note
    }

    fun transposeContent(content: String, semitones: Int, preferFlats: Boolean = false): String {
        if (semitones == 0) return content

        val lines = content.split("\n")
        val transposedLines = lines.map { line ->
            if (isChordLine(line)) {
                transposeLine(line, semitones, preferFlats)
            } else {
                line
            }
        }
        return transposedLines.joinToString("\n")
    }

    private fun isChordLine(line: String): Boolean {
        if (line.trim().isEmpty()) return false

        val words = line.trim().split(Regex("""\s+"""))
        var chordCount = 0

        for (word in words) {
            if (chordRegex.matches(word)) {
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
        var index = sharpNotes.indexOf(note)
        if (index == -1) {
            index = flatNotes.indexOf(note)
        }

        if (index == -1) return note

        val newIndex = (index + semitones + 120) % 12

        return if (preferFlats) {
            flatNotes[newIndex]
        } else {
            sharpNotes[newIndex]
        }
    }
}