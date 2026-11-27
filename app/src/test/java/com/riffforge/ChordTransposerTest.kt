package com.riffforge

import com.riffforge.feature_songs.domain.util.ChordTransposer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordTransposerTest {

    @Test
    fun `sharp chords transpose without leaving orphaned sharps`() {
        assertEquals("[G]", ChordTransposer.transposeContent("[F#]", 1))
        assertEquals("[D]", ChordTransposer.transposeContent("[C#]", 1))
        assertEquals("[D#/G]", ChordTransposer.transposeContent("[D/F#]", 1))
        assertEquals("[D#m7]", ChordTransposer.transposeContent("[C#m7]", 2))
    }

    @Test
    fun `section headers in brackets are not transposed`() {
        assertEquals("[Coro]\n[Estribillo]\n[G]", ChordTransposer.transposeContent("[Coro]\n[Estribillo]\n[F#]", 1))
    }

    @Test
    fun `chord line detection handles sharps and enharmonics`() {
        assertTrue(ChordTransposer.isChordLine("C# F# G#"))
        assertFalse(ChordTransposer.isChordLine("Caminando solo en la noche"))
    }

    @Test
    fun `enharmonic and spanish conversions work roundtrip`() {
        assertEquals("Mi#dim", ChordTransposer.convertChordDisplayName("E#dim", useSpanish = true))
        val spanish = ChordTransposer.convertNoteSystem("[D/F#] Hola [Am]", toSpanish = true)
        assertEquals("[Re/Fa#] Hola [Lam]", spanish)
        val backToAmerican = ChordTransposer.convertNoteSystem(spanish, toSpanish = false)
        assertEquals("[D/F#] Hola [Am]", backToAmerican)
    }
}
