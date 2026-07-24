package com.kcverde.fartman.ui.theme

import androidx.compose.ui.graphics.Color

// Fart Man's palette is Material 3 baseline purple with a gaseous green accent.
// These were previously ~100 hex literals inlined throughout the UI.

// --- Light ---------------------------------------------------------------
val Purple40 = Color(0xFF6750A4)
val PurpleContainerLight = Color(0xFFEADDFF)
val OnPurpleContainerLight = Color(0xFF21005D)
val PurpleGrey40 = Color(0xFF625B71)
val SecondaryContainerLight = Color(0xFFE8DEF8)
val OnSecondaryContainerLight = Color(0xFF1D192B)
val Pink40 = Color(0xFF7D5260)

val SurfaceLight = Color(0xFFFEF7FF)
val SurfaceContainerLight = Color(0xFFF3EDF7)
val SurfaceContainerLowLight = Color(0xFFF7F2FA)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFE7E0EC)
val OnSurfaceLight = Color(0xFF1D1B20)
val OnSurfaceVariantLight = Color(0xFF49454F)
val OutlineLight = Color(0xFF79747E)
val OutlineVariantLight = Color(0xFFCAC4D0)

val ErrorLight = Color(0xFFB3261E)
val ErrorContainerLight = Color(0xFFFEECEB)
val OnErrorContainerLight = Color(0xFF8C2E24)

// --- Dark ----------------------------------------------------------------
val Purple80 = Color(0xFFD0BCFF)
val OnPurpleDark = Color(0xFF381E72)
val PurpleContainerDark = Color(0xFF4F378B)
val OnPurpleContainerDark = Color(0xFFEADDFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val Pink80 = Color(0xFFEFB8C8)

val SurfaceDark = Color(0xFF141218)
val SurfaceContainerDark = Color(0xFF211F26)
val SurfaceContainerLowDark = Color(0xFF1D1B20)
val SurfaceContainerLowestDark = Color(0xFF0F0D13)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceDark = Color(0xFFE6E0E9)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)
val OutlineDark = Color(0xFF938F99)

val ErrorDark = Color(0xFFF2B8B5)
val OnErrorDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)

// --- Extended roles ------------------------------------------------------
// Material 3 defines no "success" or "warning" role, but the game needs both:
// correct guesses read green, and the bloat meter goes amber before it goes
// red. See ExtendedColors in Theme.kt.
val SuccessLight = Color(0xFF386A20)
val SuccessContainerLight = Color(0xFFE2F1D8)
val OnSuccessContainerLight = Color(0xFF2A5907)
val WarningLight = Color(0xFFE6B51E)

val SuccessDark = Color(0xFFA6D388)
val OnSuccessDark = Color(0xFF0C3900)
val SuccessContainerDark = Color(0xFF27510B)
val OnSuccessContainerDark = Color(0xFFC2EFA3)
val WarningDark = Color(0xFFEFC94C)

/** The soft green haze radiating from Fart Man. Deliberately translucent. */
val GasCloud = Color(0x3B8CE85F)

val BackgroundGradientLight = listOf(SurfaceLight, Color(0xFFEDF7ED))
val BackgroundGradientDark = listOf(SurfaceDark, Color(0xFF161C16))
