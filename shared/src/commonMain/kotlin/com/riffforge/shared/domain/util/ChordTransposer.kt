package com.riffforge.shared.domain.util

object ChordTransposer {

    private val sharpNotes = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val flatNotes = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    private val chordRegex = Regex("""\b([A-G](?:#|b)?)(m|maj|min|dim|aug|sus)?(\d)?(?:/([A-G](?:#|b)?))?\b""")

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

    private val spanishNotesSharps = listOf("Do", "Do#", "Re", "Re#", "Mi", "Fa", "Fa#", "Sol", "Sol#", "La", "La#", "Si")
    private val spanishNotesFlats = listOf("Do", "Reb", "Re", "Mib", "Mi", "Fa", "Solb", "Sol", "Lab", "La", "Sib", "Si")

    fun toSpanishNote(note: String): String {
        val index = sharpNotes.indexOf(note)
        if (index == -1) {
            val flatIndex = flatNotes.indexOf(note)
            if (flatIndex == -1) return note
            return if (note.contains("b")) spanishNotesFlats[flatIndex] else spanishNotesSharps[flatIndex]
        }
        return spanishNotesSharps[index]
    }

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

    fun getNoteDisplayName(note: String, useSpanish: Boolean): String {
        return if (useSpanish) toSpanishNote(note) else note
    }
}
