package com.khatabook.clone

import com.khatabook.clone.ui.common.BACKSPACE
import com.khatabook.clone.ui.common.addTo
import com.khatabook.clone.ui.common.applyKey
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The keypad is the only way to type an amount, so its rules have to hold:
 * nothing it produces should ever fail the server's positive-number check.
 */
class AmountKeypadTest {

    @Test
    fun `digits append`() {
        assertEquals("5", applyKey("", "5"))
        assertEquals("52", applyKey("5", "2"))
    }

    @Test
    fun `a leading zero is replaced rather than kept`() {
        assertEquals("7", applyKey("0", "7"))
    }

    @Test
    fun `only one decimal point is allowed`() {
        assertEquals("12.", applyKey("12", "."))
        assertEquals("12.", applyKey("12.", "."))
    }

    @Test
    fun `a decimal point on an empty field starts at zero`() {
        assertEquals("0.", applyKey("", "."))
    }

    @Test
    fun `paise stop at two places`() {
        assertEquals("10.55", applyKey("10.5", "5"))
        assertEquals("10.55", applyKey("10.55", "9"))
    }

    @Test
    fun `rupees stop at eight digits`() {
        assertEquals("12345678", applyKey("12345678", "9"))
    }

    @Test
    fun `backspace removes the last character and empties cleanly`() {
        assertEquals("12", applyKey("123", BACKSPACE))
        assertEquals("", applyKey("1", BACKSPACE))
        assertEquals("", applyKey("", BACKSPACE))
    }

    @Test
    fun `quick add works from empty and from a value`() {
        assertEquals("500", addTo("", 500))
        assertEquals("600", addTo("100", 500))
    }

    @Test
    fun `quick add keeps paise when the running value has them`() {
        assertEquals("600.50", addTo("100.50", 500))
    }

    @Test
    fun `quick add never leaves a trailing decimal zero`() {
        assertEquals("1000", addTo("500", 500))
    }
}
