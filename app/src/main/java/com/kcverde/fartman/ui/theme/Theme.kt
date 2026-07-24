package com.kcverde.fartman.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = PurpleContainerLight,
    onPrimaryContainer = OnPurpleContainerLight,
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = Pink40,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = ErrorLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    onPrimary = OnPurpleDark,
    primaryContainer = PurpleContainerDark,
    onPrimaryContainer = OnPurpleContainerDark,
    secondary = PurpleGrey80,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnPurpleContainerDark,
    tertiary = Pink80,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    outline = OutlineDark,
    outlineVariant = SurfaceVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
  )

/**
 * Game colors that Material 3 defines no role for.
 *
 * Reach for these through [MaterialTheme.extendedColors] so light and dark stay
 * in step; don't inline a hex literal in a composable.
 */
@Immutable
data class ExtendedColors(
  val success: Color,
  val onSuccess: Color,
  val successContainer: Color,
  val onSuccessContainer: Color,
  val warning: Color,
  val gasCloud: Color,
  val backgroundGradient: List<Color>,
)

private val LightExtendedColors =
  ExtendedColors(
    success = SuccessLight,
    onSuccess = Color.White,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    warning = WarningLight,
    gasCloud = GasCloud,
    backgroundGradient = BackgroundGradientLight,
  )

private val DarkExtendedColors =
  ExtendedColors(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    warning = WarningDark,
    gasCloud = GasCloud,
    backgroundGradient = BackgroundGradientDark,
  )

private val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val MaterialTheme.extendedColors: ExtendedColors
  @Composable @ReadOnlyComposable get() = LocalExtendedColors.current

/**
 * Dynamic color is deliberately off: the lavender-and-green palette is part of
 * the game's identity, and recoloring from the user's wallpaper would break the
 * meaning of the green/amber/red bloat meter.
 */
@Composable
fun FartManTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  CompositionLocalProvider(
    LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors
  ) {
    MaterialTheme(
      colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
      typography = Typography,
      content = content,
    )
  }
}
