package com.khatabook.clone.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KhatabookColors = lightColorScheme(
    primary = Navy,
    onPrimary = TextOnNavy,
    primaryContainer = NavyLight,
    onPrimaryContainer = TextOnNavy,
    secondary = AccentBlue,
    onSecondary = TextOnNavy,
    background = ScreenBg,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = ScreenBg,
    onSurfaceVariant = TextSecondary,
    error = RedGive,
    onError = TextOnNavy,
    outline = Divider,
    outlineVariant = Divider,
)

// The real app ships a single light look, so the theme stays light even when
// the system is in dark mode.
@Composable
fun KhatabookTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Navy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = KhatabookColors,
        typography = KhatabookTypography,
        content = content,
    )
}
