package com.khatabook.clone.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.khatabook.clone.ui.theme.KhataTheme
import kotlin.math.max

/** One day of the cashbook: what went out and what came in. */
data class DayFlow(val label: String, val gave: Double, val got: Double)

/**
 * Paired bars per day — given below the line is not a thing a shopkeeper thinks
 * about, so both bars grow upward and colour carries the direction instead.
 * Drawn on a Canvas rather than composed from Boxes so the baseline, the
 * rounded caps and the zero-day ticks stay pixel-consistent.
 */
@Composable
fun DailyFlowChart(
    days: List<DayFlow>,
    modifier: Modifier = Modifier,
    height: Int = 116,
) {
    val palette = KhataTheme.colors
    val peak = max(
        days.maxOfOrNull { max(it.gave, it.got) } ?: 0.0,
        1.0,
    ).toFloat()

    val progress by animateFloatAsState(
        targetValue = if (days.isEmpty()) 0f else 1f,
        animationSpec = tween(700),
        label = "chart",
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) {
            if (days.isEmpty()) return@Canvas

            val slot = size.width / days.size
            val barW = (slot * 0.30f).coerceAtMost(14f.dp.toPx())
            val gap = slot * 0.10f
            val baseline = size.height
            val usable = size.height - 6f

            days.forEachIndexed { index, day ->
                val centre = slot * index + slot / 2f
                val gaveH = (day.gave / peak).toFloat() * usable * progress
                val gotH = (day.got / peak).toFloat() * usable * progress

                fun bar(x: Float, h: Float, color: Color) {
                    // Zero days still get a faint tick, so gaps in trading read
                    // as "nothing happened" rather than as missing data.
                    val drawn = if (h < 2f) 2f else h
                    drawRoundRect(
                        color = if (h < 2f) palette.line else color,
                        topLeft = Offset(x, baseline - drawn),
                        size = Size(barW, drawn),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                    )
                }

                bar(centre - barW - gap / 2f, gaveH, palette.give)
                bar(centre + gap / 2f, gotH, palette.get)
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEach { day ->
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.textFaint,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun ChartLegend(modifier: Modifier = Modifier) {
    val palette = KhataTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot("You gave", palette.give)
        LegendDot("You got", palette.get)
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = KhataTheme.colors.textSecondary,
        )
    }
}
