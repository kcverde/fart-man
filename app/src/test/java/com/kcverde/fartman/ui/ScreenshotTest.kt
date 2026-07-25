package com.kcverde.fartman.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.ui.preview.MID_GAME
import com.kcverde.fartman.ui.preview.SAMPLE_HISTORY
import com.kcverde.fartman.ui.preview.SETUP_ROUND
import com.kcverde.fartman.ui.screens.ActiveGameScreen
import com.kcverde.fartman.ui.screens.GameOverScreen
import com.kcverde.fartman.ui.screens.PassingScreen
import com.kcverde.fartman.ui.screens.SetupScreen
import com.kcverde.fartman.ui.theme.FartManTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Golden images of every screen in both light and dark.
 *
 * These exist mainly to keep the dark theme honest: the game was built
 * light-only with its palette hardcoded, so dark mode is easy to regress
 * without noticing.
 *
 * Record with `./gradlew recordRoborazziDebug`, check with
 * `./gradlew verifyRoborazziDebug`.
 *
 * The sample rounds come from the debug source set, so these goldens and the
 * Android Studio previews render the same states.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ScreenshotTest {

  @get:Rule val composeRule = createComposeRule()

  @Test
  fun setupScreenLight() = capture("setup_light", dark = false) { Setup() }

  @Test
  fun setupScreenDark() = capture("setup_dark", dark = true) { Setup() }

  @Test
  fun passingScreenLight() =
    capture("passing_light", dark = false) { PassingScreen(guesserName = "Bob", onStartGuessing = {}) }

  @Test
  fun activeGameLight() = capture("active_light", dark = false) { Active() }

  @Test
  fun activeGameDark() = capture("active_dark", dark = true) { Active() }

  /** The long-word case that used to run off the side of the screen. */
  @Test
  fun activeGameWithMaximumLengthWord() =
    capture("active_long_word", dark = false) {
      ActiveGameScreen(
        state = MID_GAME.copy(secretWord = "FLATULENCEBOMBS", guessedLetters = setOf('E', 'L', 'Z')),
        onGuess = {},
        onGiveUp = {},
        onToggleSound = {},
      )
    }

  @Test
  fun victoryLight() =
    capture("victory_light", dark = false) {
      GameOverScreen(
        state = MID_GAME.copy(phase = GamePhase.VICTORY, incorrectCount = 2),
        onPlayAgain = {},
        onSwapRoles = {},
        onToggleSound = {},
      )
    }

  @Test
  fun defeatDark() =
    capture("defeat_dark", dark = true) {
      GameOverScreen(
        state = MID_GAME.copy(phase = GamePhase.DEFEAT, incorrectCount = 6),
        onPlayAgain = {},
        onSwapRoles = {},
        onToggleSound = {},
      )
    }

  @Composable
  private fun Setup() {
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

  @Composable
  private fun Active() {
    ActiveGameScreen(state = MID_GAME, onGuess = {}, onGiveUp = {}, onToggleSound = {})
  }

  private fun capture(name: String, dark: Boolean, content: @Composable () -> Unit) {
    // Fart Man bobs on an infinite transition, which never lets the test clock
    // go idle. Drive it manually and stop at a fixed point so the goldens are
    // reproducible.
    composeRule.mainClock.autoAdvance = false
    composeRule.setContent {
      FartManTheme(darkTheme = dark) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
          content()
        }
      }
    }
    composeRule.mainClock.advanceTimeBy(SETTLE_MILLIS)
    composeRule.onRoot().captureRoboImage("src/test/screenshots/$name.png")
  }

  private companion object {
    const val SETTLE_MILLIS = 500L
  }
}
