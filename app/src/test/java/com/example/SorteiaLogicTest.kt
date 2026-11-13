package com.example

import com.example.domain.WordListParser
import com.example.ui.SorteiaUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SorteiaLogicTest {

    @Test
    fun `parseWords splits by newlines commas and semicolons and deduplicates`() {
        val raw = "  Maçã, banana;\nMAÇÃ\n  Pão   de queijo ; Uva , "
        val parsed = WordListParser.parseWords(raw)
        assertEquals(listOf("Maçã", "banana", "Pão de queijo", "Uva"), parsed)
    }

    @Test
    fun `parseWords returns empty list for blank input`() {
        assertTrue(WordListParser.parseWords("   \n , ; ").isEmpty())
    }

    @Test
    fun `avoidRepeats filters out drawn history case insensitively`() {
        val state = SorteiaUiState(
            words = listOf("Sol", "Lua", "Estrela"),
            drawnHistory = listOf("lua"),
            avoidRepeats = true,
        )
        assertEquals(listOf("Sol", "Estrela"), state.availableWords)
        assertFalse(state.allDrawn)
        assertTrue(state.canDraw)
    }

    @Test
    fun `allDrawn becomes true when every word has been drawn in no-repeat mode`() {
        val state = SorteiaUiState(
            words = listOf("Sol", "Lua"),
            drawnHistory = listOf("Sol", "Lua"),
            avoidRepeats = true,
        )
        assertTrue(state.availableWords.isEmpty())
        assertTrue(state.allDrawn)
        assertFalse(state.canDraw)
    }
}
