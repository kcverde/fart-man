package com.kcverde.fartman.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kcverde.fartman.R
import com.kcverde.fartman.game.GameRules

/**
 * Fart Man himself, bobbing gently and swelling with each wrong guess.
 *
 * Note the stage mapping: zero and one mistakes both show stage 1, which
 * reserves stage 6 for the detonation so players never see the explosion art
 * while they still have a guess left.
 */
@Composable
fun FartManStage(incorrectCount: Int, isExploded: Boolean, modifier: Modifier = Modifier) {
  val hover by
    rememberInfiniteTransition(label = "fartman_idle")
      .animateFloat(
        initialValue = -HOVER_RANGE_PX,
        targetValue = HOVER_RANGE_PX,
        animationSpec =
          infiniteRepeatable(
            animation = tween(HOVER_MILLIS, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
          ),
        label = "hover",
      )

  val stageIndex =
    if (isExploded) STAGES.lastIndex
    else incorrectCount.coerceIn(1, STAGES.size) - 1

  val description =
    if (isExploded) stringResource(R.string.fart_man_exploded)
    else stringResource(R.string.fart_man_stage, stageIndex + 1, GameRules.MAX_INCORRECT)

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Image(
      painter = painterResource(id = STAGES[stageIndex]),
      contentDescription = description,
      modifier = Modifier.fillMaxSize().padding(16.dp).graphicsLayer(translationY = hover),
    )
  }
}

private val STAGES =
  listOf(
    R.drawable.fart_state_1,
    R.drawable.fart_state_2,
    R.drawable.fart_state_3,
    R.drawable.fart_state_4,
    R.drawable.fart_state_5,
    R.drawable.fart_state_6,
  )

private const val HOVER_RANGE_PX = 8f
private const val HOVER_MILLIS = 1500
