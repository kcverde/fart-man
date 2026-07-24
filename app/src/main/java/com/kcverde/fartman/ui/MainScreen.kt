package com.kcverde.fartman.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.ui.screens.ActiveGameScreen
import com.kcverde.fartman.ui.screens.GameOverScreen
import com.kcverde.fartman.ui.screens.PassingScreen
import com.kcverde.fartman.ui.screens.SetupScreen
import com.kcverde.fartman.ui.theme.extendedColors
import kotlinx.coroutines.flow.collectLatest

/**
 * Routes between the phases of a round and owns the one piece of chrome shared
 * by all of them: the rumble that fires on a wrong guess.
 */
@Composable
fun MainScreen(viewModel: FartManViewModel, modifier: Modifier = Modifier) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val history by viewModel.history.collectAsStateWithLifecycle()

  val shakeOffset = remember { Animatable(0f) }
  LaunchedEffect(viewModel) {
    // collectLatest so a fresh wrong guess restarts the rumble rather than
    // queueing behind the one still playing.
    viewModel.shakeEvents.collectLatest {
      repeat(SHAKE_CYCLES) {
        shakeOffset.animateTo(SHAKE_DISTANCE, tween(SHAKE_STEP_MILLIS, easing = LinearEasing))
        shakeOffset.animateTo(-SHAKE_DISTANCE, tween(SHAKE_STEP_MILLIS, easing = LinearEasing))
      }
      shakeOffset.animateTo(0f, tween(SHAKE_STEP_MILLIS, easing = LinearEasing))
    }
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(MaterialTheme.extendedColors.backgroundGradient))
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .offset(x = shakeOffset.value.dp)
    ) {
      AnimatedContent(
        targetState = state.phase,
        label = "phase",
        transitionSpec = {
          fadeIn(tween(CROSSFADE_MILLIS)) togetherWith fadeOut(tween(CROSSFADE_MILLIS))
        },
      ) { phase ->
        when (phase) {
          GamePhase.SETUP ->
            SetupScreen(
              state = state,
              history = history,
              onCreatorNameChange = viewModel::updateCreatorName,
              onGuesserNameChange = viewModel::updateGuesserName,
              onSecretWordChange = viewModel::updateSecretWord,
              onHintChange = viewModel::updateHint,
              onStartRound = viewModel::startRound,
              onToggleSound = viewModel::toggleSound,
              onClearHistory = viewModel::clearHistory,
            )

          GamePhase.PASSING ->
            PassingScreen(
              guesserName = state.guesserName,
              onStartGuessing = viewModel::startGuessing,
            )

          GamePhase.ACTIVE ->
            ActiveGameScreen(
              state = state,
              onGuess = viewModel::guessLetter,
              onGiveUp = viewModel::giveUp,
              onToggleSound = viewModel::toggleSound,
            )

          GamePhase.VICTORY,
          GamePhase.DEFEAT ->
            GameOverScreen(
              state = state,
              onPlayAgain = viewModel::playAgain,
              onSwapRoles = viewModel::swapRolesAndPlayAgain,
              onToggleSound = viewModel::toggleSound,
            )
        }
      }
    }
  }
}

private const val SHAKE_CYCLES = 3
private const val SHAKE_DISTANCE = 25f
private const val SHAKE_STEP_MILLIS = 50
private const val CROSSFADE_MILLIS = 300
