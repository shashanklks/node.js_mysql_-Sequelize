package com.khatabook.clone.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colours. Every screen reads these instead of raw values, which is
 * what lets the whole app flip to dark without touching a single screen.
 */
@Immutable
data class KhataPalette(
    val headerTop: Color,
    val headerBottom: Color,
    val onHeader: Color,
    val onHeaderMuted: Color,
    val brand: Color,
    val accent: Color,
    val get: Color,
    val getSoft: Color,
    val give: Color,
    val giveSoft: Color,
    val screen: Color,
    val surface: Color,
    val surfaceAlt: Color,
    val line: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textFaint: Color,
    val avatarBg: Color,
    val isDark: Boolean,
) {
    /** Green when they owe you, red when you owe them, grey once settled. */
    fun money(amount: Double): Color = when {
        amount > 0 -> get
        amount < 0 -> give
        else -> textSecondary
    }

    fun moneySoft(amount: Double): Color = when {
        amount > 0 -> getSoft
        amount < 0 -> giveSoft
        else -> surfaceAlt
    }
}

val LightPalette = KhataPalette(
    headerTop = InkNavy,
    headerBottom = InkNavyDeep,
    onHeader = OnHeader,
    onHeaderMuted = OnHeaderMutedLight,
    brand = InkNavy,
    accent = AmberLight,
    get = GreenGetLight,
    getSoft = GreenGetSoftLight,
    give = RedGiveLight,
    giveSoft = RedGiveSoftLight,
    screen = ScreenLight,
    surface = SurfaceLight,
    surfaceAlt = SurfaceAltLight,
    line = LineLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textFaint = TextFaintLight,
    avatarBg = AvatarBgLight,
    isDark = false,
)

val DarkPalette = KhataPalette(
    headerTop = InkNavyNight,
    headerBottom = InkNavyNightDeep,
    onHeader = OnHeader,
    onHeaderMuted = OnHeaderMutedDark,
    brand = InkNavyLift,
    accent = AmberDark,
    get = GreenGetDark,
    getSoft = GreenGetSoftDark,
    give = RedGiveDark,
    giveSoft = RedGiveSoftDark,
    screen = ScreenDark,
    surface = SurfaceDark,
    surfaceAlt = SurfaceAltDark,
    line = LineDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textFaint = TextFaintDark,
    avatarBg = AvatarBgDark,
    isDark = true,
)

val LocalKhataPalette = staticCompositionLocalOf { LightPalette }

/** `KhataTheme.colors.get` reads better at the call site than a local lookup. */
object KhataTheme {
    val colors: KhataPalette
        @androidx.compose.runtime.Composable
        @androidx.compose.runtime.ReadOnlyComposable
        get() = LocalKhataPalette.current
}

/**
 * A stable accent per party, so the same shop always gets the same colour chip
 * and the list becomes scannable by colour as well as by name.
 */
private val avatarInks = listOf(
    Color(0xFF2E5EAA),
    Color(0xFF0E7C55),
    Color(0xFF9C4221),
    Color(0xFF6B3FA0),
    Color(0xFF116E7C),
    Color(0xFFA23B52),
)

fun avatarInkFor(name: String): Color {
    if (name.isBlank()) return avatarInks[0]
    val hash = name.sumOf { it.code }
    return avatarInks[hash % avatarInks.size]
}
