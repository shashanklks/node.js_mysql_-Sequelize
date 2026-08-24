@file:OptIn(ExperimentalMaterial3Api::class)

package com.khatabook.clone.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.khatabook.clone.ui.theme.KhataTheme
import com.khatabook.clone.ui.theme.avatarInkFor

/** The ink-navy gradient every screen header sits on. */
@Composable
fun HeaderSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val palette = KhataTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(palette.headerTop, palette.headerBottom)))
    ) {
        content()
    }
}

@Composable
fun KhataTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    val palette = KhataTheme.colors
    HeaderSurface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = palette.onHeader,
                    )
                }
            } else {
                Spacer(Modifier.size(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = palette.onHeader,
                    maxLines = 1,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.onHeaderMuted,
                        maxLines = 1,
                    )
                }
            }
            actions()
            Spacer(Modifier.size(4.dp))
        }
    }
}

/**
 * Round monogram. The ink is derived from the name, so a party keeps the same
 * colour every time you see it and the list can be scanned by colour.
 */
@Composable
fun Avatar(name: String, size: Int = 44, modifier: Modifier = Modifier) {
    val ink = avatarInkFor(name)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(ink.copy(alpha = if (KhataTheme.colors.isDark) 0.26f else 0.13f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials(name),
            color = if (KhataTheme.colors.isDark) ink.lighten() else ink,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

private fun Color.lighten(factor: Float = 0.45f) = Color(
    red = red + (1f - red) * factor,
    green = green + (1f - green) * factor,
    blue = blue + (1f - blue) * factor,
    alpha = alpha,
)

/** The standard white (or night) card the whole app is built from. */
@Composable
fun KhataCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val palette = KhataTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = palette.surface,
        shadowElevation = if (palette.isDark) 0.dp else 2.dp,
        tonalElevation = 0.dp,
        onClick = onClick ?: {},
        enabled = onClick != null,
    ) {
        content()
    }
}

/**
 * The get/give split as one bar. Two numbers side by side make you do the
 * comparison yourself; the bar does it for you before you read a digit.
 */
@Composable
fun ProportionBar(
    get: Double,
    give: Double,
    modifier: Modifier = Modifier,
    height: Int = 8,
) {
    val palette = KhataTheme.colors
    val total = get + give
    val target = if (total <= 0.0) 0.5f else (get / total).toFloat()
    val share by animateFloatAsState(target, tween(600), label = "share")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(height.dp)),
    ) {
        if (total <= 0.0) {
            Box(Modifier.fillMaxSize().background(palette.line))
        } else {
            Box(
                Modifier
                    .weight(share.coerceAtLeast(0.001f))
                    .fillMaxHeight()
                    .background(palette.get)
            )
            Box(
                Modifier
                    .weight((1f - share).coerceAtLeast(0.001f))
                    .fillMaxHeight()
                    .background(palette.give)
            )
        }
    }
}

/** Pill-shaped segmented control — replaces the underline tabs. */
@Composable
fun SegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = KhataTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.surfaceAlt)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val bg by animateColorAsState(
                if (selected) palette.surface else Color.Transparent,
                tween(180),
                label = "segbg",
            )
            val fg by animateColorAsState(
                if (selected) palette.textPrimary else palette.textSecondary,
                tween(180),
                label = "segfg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(bg)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(text = label, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}

@Composable
fun Pill(
    text: String,
    background: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(text = text, color = contentColor, style = MaterialTheme.typography.labelSmall)
    }
}

/** A small all-caps heading used to group cards on a screen. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = KhataTheme.colors.textFaint,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = KhataTheme.colors.brand, strokeWidth = 3.dp)
    }
}

/**
 * An empty state that offers the next step rather than only reporting absence.
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    action: @Composable () -> Unit = {},
) {
    val palette = KhataTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(palette.surfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icon, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = palette.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        action()
    }
}

@Composable
fun RowDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(KhataTheme.colors.line),
    )
}

@Composable
fun ScreenMessage(text: String, modifier: Modifier = Modifier, action: @Composable () -> Unit = {}) {
    val palette = KhataTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = text,
            color = palette.give,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        action()
    }
}
