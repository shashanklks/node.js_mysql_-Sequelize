package com.khatabook.clone.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.khatabook.clone.ui.theme.KhataTheme

/**
 * Money always renders with tabular figures so that a column of amounts lines
 * up on the decimal instead of shifting as the digits change.
 */
@Composable
fun MoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color? = null,
    weight: FontWeight = FontWeight.Bold,
    signed: Boolean = false,
) {
    val palette = KhataTheme.colors
    val prefix = if (signed && amount > 0) "+" else ""
    Text(
        text = prefix + rupees(amount),
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        fontWeight = weight,
        color = color ?: palette.money(amount),
    )
}

/**
 * Counts up to the value when it changes. Used only on the two hero totals —
 * a number that animates everywhere is noise, but a headline balance settling
 * into place tells you it just refreshed.
 */
@Composable
fun AnimatedMoneyText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color? = null,
    weight: FontWeight = FontWeight.Bold,
) {
    val palette = KhataTheme.colors
    val animated by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 520),
        label = "money",
    )
    Text(
        text = rupees(animated.toDouble()),
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        fontWeight = weight,
        color = color ?: palette.money(amount),
    )
}
