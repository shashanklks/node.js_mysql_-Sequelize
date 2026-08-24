package com.khatabook.clone.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Raw values only. Screens never reach for these directly — they read the
 * semantic palette in Palette.kt, which is what makes dark mode work.
 *
 * The header colours are ledger-ink navy rather than a generic UI blue, and the
 * amber accent is reserved for highlights that are not money, so green and red
 * keep one meaning everywhere in the app.
 */

// Ink navy — headers and primary actions
val InkNavy = Color(0xFF12305F)
val InkNavyDeep = Color(0xFF0B2244)
val InkNavyLift = Color(0xFF1D4A8F)
val InkNavyNight = Color(0xFF101A2E)
val InkNavyNightDeep = Color(0xFF0A1220)

// Money. Green is owed to you, red is owed by you.
val GreenGetLight = Color(0xFF0E7C55)
val GreenGetSoftLight = Color(0xFFE2F2EA)
val GreenGetDark = Color(0xFF35C48D)
val GreenGetSoftDark = Color(0xFF10352A)

val RedGiveLight = Color(0xFFC7333E)
val RedGiveSoftLight = Color(0xFFFBEAEC)
val RedGiveDark = Color(0xFFFF6F76)
val RedGiveSoftDark = Color(0xFF3A1B1F)

// Amber — streaks, "today" markers, anything that must catch the eye
// without claiming to be money.
val AmberLight = Color(0xFFC97F16)
val AmberDark = Color(0xFFF0B457)

// Grounds and lines. The neutrals carry a slight navy bias so they sit with
// the header rather than beside it.
val ScreenLight = Color(0xFFF1F4F9)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceAltLight = Color(0xFFF7F9FC)
val LineLight = Color(0xFFE3E8F1)

val ScreenDark = Color(0xFF0B1017)
val SurfaceDark = Color(0xFF151C27)
val SurfaceAltDark = Color(0xFF1B2330)
val LineDark = Color(0xFF262F3E)

// Text
val TextPrimaryLight = Color(0xFF101724)
val TextSecondaryLight = Color(0xFF616D80)
val TextFaintLight = Color(0xFF97A0B0)

val TextPrimaryDark = Color(0xFFE9EDF4)
val TextSecondaryDark = Color(0xFF9AA5B8)
val TextFaintDark = Color(0xFF6B7688)

val OnHeader = Color(0xFFFFFFFF)
val OnHeaderMutedLight = Color(0xFFC3D1E8)
val OnHeaderMutedDark = Color(0xFF9FB0CC)

val AvatarBgLight = Color(0xFFE7EDFA)
val AvatarBgDark = Color(0xFF223046)
