package com.kcverde.fartman.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcverde.fartman.R
import com.kcverde.fartman.game.GameRules
import com.kcverde.fartman.game.GameUiState
import com.kcverde.fartman.ui.components.FartManStage
import com.kcverde.fartman.ui.components.LetterKeyboard
import com.kcverde.fartman.ui.components.SecretWordRow
import com.kcverde.fartman.ui.components.SoundToggleButton
import com.kcverde.fartman.ui.theme.extendedColors

const val GIVE_UP_TAG = "give_up"

/** The guessing screen: bloat meter, Fart Man, the word, and the keyboard. */
@Composable
fun ActiveGameScreen(
  state: GameUiState,
  onGuess: (Char) -> Unit,
  onGiveUp: () -> Unit,
  onToggleSound: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var confirmingGiveUp by remember { mutableStateOf(false) }
  var hintRevealed by remember { mutableStateOf(false) }

  if (confirmingGiveUp) {
    GiveUpDialog(
      creatorName = state.creatorName,
      onConfirm = {
        confirmingGiveUp = false
        onGiveUp()
      },
      onDismiss = { confirmingGiveUp = false },
    )
  }

  Column(
    modifier = modifier.fillMaxSize().padding(top = 16.dp, start = 16.dp, end = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    PlayerBanner(state = state, onToggleSound = onToggleSound)

    Spacer(modifier = Modifier.height(14.dp))

    BloatMeter(state = state)

    Box(
      modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          Modifier.size(240.dp)
            .background(
              Brush.radialGradient(
                listOf(MaterialTheme.extendedColors.gasCloud, Color.Transparent)
              )
            )
      )
      FartManStage(
        incorrectCount = state.incorrectCount,
        isExploded = false,
        modifier = Modifier.size(240.dp),
      )
    }

    SecretWordRow(
      word = state.secretWord,
      guessedLetters = state.guessedLetters,
      modifier = Modifier.padding(bottom = 16.dp),
    )

    if (state.hint.isNotBlank()) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { hintRevealed = !hintRevealed }
            .padding(10.dp),
        contentAlignment = Alignment.Center,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text =
              if (hintRevealed) stringResource(R.string.hint_shown, state.hint)
              else stringResource(R.string.hint_reveal),
            color =
              if (hintRevealed) MaterialTheme.colorScheme.onSurface
              else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = if (hintRevealed) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
          )
        }
      }
      Spacer(modifier = Modifier.height(14.dp))
    }

    LetterKeyboard(state = state, onGuess = onGuess)

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            MaterialTheme.colorScheme.surfaceContainer,
            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
          )
          .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(8.dp).background(MaterialTheme.extendedColors.success, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = stringResource(R.string.turn_status, state.guesserName),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
        )
      }

      Button(
        onClick = { confirmingGiveUp = true },
        shape = RoundedCornerShape(100.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = Modifier.testTag(GIVE_UP_TAG),
      ) {
        Text(stringResource(R.string.give_up), fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun PlayerBanner(state: GameUiState, onToggleSound: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    PlayerLabel(
      role = stringResource(R.string.label_creator),
      name = state.creatorName,
      alignment = Alignment.Start,
    )

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Box(
        modifier =
          Modifier.clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 10.dp, vertical = 4.dp)
      ) {
        Text(
          text = stringResource(R.string.badge_active),
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      SoundToggleButton(
        soundEnabled = state.soundEnabled,
        onToggle = onToggleSound,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        buttonSize = 30.dp,
        iconSize = 16.dp,
      )
    }

    PlayerLabel(
      role = stringResource(R.string.label_guesser),
      name = state.guesserName,
      alignment = Alignment.End,
    )
  }
}

@Composable
private fun PlayerLabel(role: String, name: String, alignment: Alignment.Horizontal) {
  Column(horizontalAlignment = alignment) {
    Text(
      text = role,
      fontSize = 11.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.Bold,
    )
    Text(
      text = name,
      fontSize = 14.sp,
      color = MaterialTheme.colorScheme.onSurface,
      fontWeight = FontWeight.Bold,
    )
  }
}

@Composable
private fun BloatMeter(state: GameUiState) {
  val meterColor =
    when {
      state.mistakesRemaining <= 1 -> MaterialTheme.colorScheme.error
      state.incorrectCount >= GameRules.MAX_INCORRECT / 2 -> MaterialTheme.extendedColors.warning
      else -> MaterialTheme.colorScheme.primary
    }

  Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(
        text = stringResource(R.string.pressure_label),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text =
          pluralStringResource(
            R.plurals.mistakes_remaining,
            state.mistakesRemaining,
            state.mistakesRemaining,
          ),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = meterColor,
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    LinearProgressIndicator(
      progress = { state.bloatFraction },
      color = meterColor,
      trackColor = MaterialTheme.colorScheme.outlineVariant,
      modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
    )
  }
}

@Composable
private fun GiveUpDialog(creatorName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.give_up_title)) },
    text = { Text(stringResource(R.string.give_up_message, creatorName)) },
    confirmButton = {
      TextButton(onClick = onConfirm) { Text(stringResource(R.string.give_up_confirm)) }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.give_up_cancel)) }
    },
  )
}
