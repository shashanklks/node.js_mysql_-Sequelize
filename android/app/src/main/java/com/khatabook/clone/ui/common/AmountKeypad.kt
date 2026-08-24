package com.khatabook.clone.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khatabook.clone.ui.theme.KhataTheme

/**
 * A ledger is written standing at a counter, one thumb free, so the amount
 * screen carries its own keypad instead of borrowing the system keyboard:
 * bigger targets, no layout jump when the IME opens, and round-number shortcuts
 * for the amounts shopkeepers actually type.
 */
private val quickAdds = listOf(50, 100, 500, 1000)

@Composable
fun AmountKeypad(
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current

    fun press(key: String) {
        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onValueChange(applyKey(value, key))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            quickAdds.forEach { step ->
                QuickAddChip(
                    step = step,
                    accent = accent,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onValueChange(addTo(value, step))
                    },
                )
            }
        }

        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf(".", "0", BACKSPACE),
        ).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    KeypadKey(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = { press(key) },
                    )
                }
            }
        }
    }
}

const val BACKSPACE = "⌫"

@Composable
private fun QuickAddChip(
    step: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = KhataTheme.colors
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = if (palette.isDark) 0.18f else 0.09f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "+$step",
            style = MaterialTheme.typography.labelLarge,
            color = accent,
        )
    }
}

@Composable
private fun KeypadKey(key: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val palette = KhataTheme.colors
    Box(
        modifier = modifier
            .height(58.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = if (key == BACKSPACE) "Delete" else key },
        contentAlignment = Alignment.Center,
    ) {
        if (key == BACKSPACE) {
            Icon(
                Icons.AutoMirrored.Filled.Backspace,
                contentDescription = null,
                tint = palette.textSecondary,
            )
        } else {
            Text(
                text = key,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = palette.textPrimary,
            )
        }
    }
}

/* ---------------------------------------------------------------- */
/* Pure input rules, kept out of the composables so they can be read  */
/* (and tested) on their own.                                        */
/* ---------------------------------------------------------------- */

private const val MAX_RUPEE_DIGITS = 8

internal fun applyKey(current: String, key: String): String = when (key) {
    BACKSPACE -> current.dropLast(1)
    "." -> if (current.contains('.')) current else if (current.isEmpty()) "0." else "$current."
    else -> {
        val parts = current.split('.')
        val atPaiseLimit = parts.size == 2 && parts[1].length >= 2
        val atRupeeLimit = parts.size == 1 && current.length >= MAX_RUPEE_DIGITS
        when {
            atPaiseLimit || atRupeeLimit -> current
            current == "0" -> key
            else -> current + key
        }
    }
}

internal fun addTo(current: String, step: Int): String {
    val total = (current.toDoubleOrNull() ?: 0.0) + step
    // Keep it looking like something someone typed: no trailing ".0".
    return if (total % 1.0 == 0.0) total.toLong().toString() else String.format("%.2f", total)
}
