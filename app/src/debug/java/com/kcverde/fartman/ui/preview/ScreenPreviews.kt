package com.kcverde.fartman.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.ui.screens.ActiveGameScreen
import com.kcverde.fartman.ui.screens.GameOverScreen
import com.kcverde.fartman.ui.screens.PassingScreen
import com.kcverde.fartman.ui.screens.SetupScreen
import com.kcverde.fartman.ui.theme.FartManTheme

/**
 * Android Studio previews of every screen, mirroring the states in
 * `ScreenshotTest`.
 *
 * These are the fast loop for anything visual: they re-render on edit, where
 * `verifyRoborazziDebug` costs a Gradle run. The goldens remain the thing that
 * actually fails a build — a preview only ever shows you something, it never
 * catches a regression on its own.
 */

/**
 * Light and dark on a Pixel 8, the device the goldens render on.
 *
 * Pinning a device matters here: every screen is `fillMaxSize`, so a preview
 * without one renders as a sliver. `FartManTheme` defaults to
 * `isSystemInDarkTheme()`, which is what makes the night `uiMode` flip it.
 */
@Preview(name = "Light", device = Devices.PIXEL_8)
@Preview(name = "Dark", device = Devices.PIXEL_8, uiMode = Configuration.UI_MODE_NIGHT_YES)
private annotation class LightAndDark

@LightAndDark
@Composable
private fun SetupScreenPreview() = PreviewSurface {
  SetupScreen(
    state = SETUP_ROUND,
    history = SAMPLE_HISTORY,
    onCreatorNameChange = {},
    onGuesserNameChange = {},
    onSecretWordChange = {},
    onHintChange = {},
    onStartRound = {},
    onToggleSound = {},
    onClearHistory = {},
  )
}

@LightAndDark
@Composable
private fun PassingScreenPreview() = PreviewSurface {
  PassingScreen(guesserName = "Bob", onStartGuessing = {})
}

@LightAndDark
@Composable
private fun ActiveGameScreenPreview() = PreviewSurface {
  ActiveGameScreen(state = MID_GAME, onGuess = {}, onGiveUp = {}, onToggleSound = {})
}

/** The long-word case that used to run off the side of the screen. */
@Preview(name = "Long word", device = Devices.PIXEL_8)
@Composable
private fun ActiveGameLongWordPreview() = PreviewSurface {
  ActiveGameScreen(
    state = MID_GAME.copy(secretWord = "FLATULENCEBOMBS", guessedLetters = setOf('E', 'L', 'Z')),
    onGuess = {},
    onGiveUp = {},
    onToggleSound = {},
  )
}

@LightAndDark
@Composable
private fun VictoryPreview() = PreviewSurface {
  GameOverScreen(
    state = MID_GAME.copy(phase = GamePhase.VICTORY, incorrectCount = 2),
    onPlayAgain = {},
    onSwapRoles = {},
    onToggleSound = {},
  )
}

@LightAndDark
@Composable
private fun DefeatPreview() = PreviewSurface {
  GameOverScreen(
    state = MID_GAME.copy(phase = GamePhase.DEFEAT, incorrectCount = 6),
    onPlayAgain = {},
    onSwapRoles = {},
    onToggleSound = {},
  )
}

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
  FartManTheme {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
      content()
    }
  }
}
