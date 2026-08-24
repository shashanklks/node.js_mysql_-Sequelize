package com.khatabook.clone.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun schemeFor(palette: KhataPalette) = if (palette.isDark) {
    darkColorScheme(
        primary = palette.brand,
        onPrimary = palette.onHeader,
        secondary = palette.accent,
        background = palette.screen,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceAlt,
        onSurfaceVariant = palette.textSecondary,
        error = palette.give,
        onError = palette.onHeader,
        outline = palette.line,
        outlineVariant = palette.line,
    )
} else {
    lightColorScheme(
        primary = palette.brand,
        onPrimary = palette.onHeader,
        secondary = palette.accent,
        background = palette.screen,
        onBackground = palette.textPrimary,
        surface = palette.surface,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceAlt,
        onSurfaceVariant = palette.textSecondary,
        error = palette.give,
        onError = palette.onHeader,
        outline = palette.line,
        outlineVariant = palette.line,
    )
}

@Composable
fun KhatabookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // The header runs under the status bar in both themes, so the bar
            // icons stay light regardless of the system setting.
            window.statusBarColor = palette.headerTop.toArgb()
            window.navigationBarColor = palette.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = !palette.isDark
        }
    }

    CompositionLocalProvider(LocalKhataPalette provides palette) {
        MaterialTheme(
            colorScheme = schemeFor(palette),
            typography = KhatabookTypography,
            shapes = KhatabookShapes,
            content = content,
        )
    }
}
