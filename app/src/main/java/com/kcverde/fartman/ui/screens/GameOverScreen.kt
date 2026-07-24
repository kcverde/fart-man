package com.kcverde.fartman.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcverde.fartman.R
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.game.GameUiState
import com.kcverde.fartman.ui.components.FartManStage
import com.kcverde.fartman.ui.components.SoundToggleButton
import com.kcverde.fartman.ui.theme.extendedColors

const val REMATCH_SWAP_TAG = "rematch_swap"
const val PLAY_AGAIN_TAG = "play_again"

/** Victory or detonation, plus the two ways to start the next round. */
@Composable
fun GameOverScreen(
  state: GameUiState,
  onPlayAgain: () -> Unit,
  onSwapRoles: () -> Unit,
  onToggleSound: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isWin = state.phase == GamePhase.VICTORY

  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(32.dp),
      colors =
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
      elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
      Box(modifier = Modifier.fillMaxWidth()) {
        SoundToggleButton(
          soundEnabled = state.soundEnabled,
          onToggle = onToggleSound,
          containerColor = MaterialTheme.colorScheme.surface,
          buttonSize = 32.dp,
          iconSize = 16.dp,
          modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
        )

        Column(
          modifier = Modifier.fillMaxWidth().padding(28.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(modifier = Modifier.size(200.dp).padding(bottom = 12.dp)) {
            FartManStage(incorrectCount = state.incorrectCount, isExploded = !isWin)
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = stringResource(if (isWin) R.string.victory_title else R.string.defeat_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color =
              if (isWin) MaterialTheme.extendedColors.success else MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = outcomeNarrative(state, isWin),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
          )

          Spacer(modifier = Modifier.height(20.dp))

          Box(
            modifier =
              Modifier.fillMaxWidth()
                .background(
                  MaterialTheme.colorScheme.surfaceContainerLow,
                  RoundedCornerShape(12.dp),
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = stringResource(R.string.secret_word_was),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = state.secretWord,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
              )
            }
          }

          Spacer(modifier = Modifier.height(28.dp))

          Button(
            onClick = onSwapRoles,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag(REMATCH_SWAP_TAG),
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = stringResource(R.string.rematch_swap),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = onPlayAgain,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag(PLAY_AGAIN_TAG),
          ) {
            Text(
              text = stringResource(R.string.new_game_same_roles),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun outcomeNarrative(state: GameUiState, isWin: Boolean): String =
  when {
    isWin && state.incorrectCount == 0 ->
      stringResource(R.string.victory_body_perfect, state.guesserName)
    isWin ->
      pluralStringResource(
        R.plurals.victory_body,
        state.incorrectCount,
        state.guesserName,
        state.incorrectCount,
      )
    state.gaveUp ->
      stringResource(R.string.defeat_body_gave_up, state.guesserName, state.creatorName)
    else -> stringResource(R.string.defeat_body, state.guesserName, state.creatorName)
  }
