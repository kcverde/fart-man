package com.kcverde.fartman.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcverde.fartman.R
import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.game.GameRules
import com.kcverde.fartman.game.GameUiState
import com.kcverde.fartman.ui.components.GameTextField
import com.kcverde.fartman.ui.components.MatchHistoryCard
import com.kcverde.fartman.ui.components.SoundToggleButton

const val SUBMIT_SETUP_TAG = "submit_setup"
const val SECRET_WORD_INPUT_TAG = "secret_word_input"

/** Where the creator names the players and sets the trap. */
@Composable
fun SetupScreen(
  state: GameUiState,
  history: List<GameRecord>,
  onCreatorNameChange: (String) -> Unit,
  onGuesserNameChange: (String) -> Unit,
  onSecretWordChange: (String) -> Unit,
  onHintChange: (String) -> Unit,
  onStartRound: () -> Unit,
  onToggleSound: () -> Unit,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    AppHeader(soundEnabled = state.soundEnabled, onToggleSound = onToggleSound)

    Spacer(modifier = Modifier.height(24.dp))

    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        SectionLabel(stringResource(R.string.setup_section_players))

        GameTextField(
          value = state.creatorName,
          onValueChange = onCreatorNameChange,
          label = stringResource(R.string.setup_creator_label),
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          modifier = Modifier.testTag("creator_name_input"),
        )

        Spacer(modifier = Modifier.height(10.dp))

        GameTextField(
          value = state.guesserName,
          onValueChange = onGuesserNameChange,
          label = stringResource(R.string.setup_guesser_label),
          leadingIcon = { Icon(Icons.Default.Face, contentDescription = null) },
          modifier = Modifier.testTag("guesser_name_input"),
        )

        Spacer(modifier = Modifier.height(20.dp))

        SectionLabel(stringResource(R.string.setup_section_word))

        GameTextField(
          value = state.secretWord,
          onValueChange = onSecretWordChange,
          label = stringResource(R.string.setup_secret_word_label),
          supportingText = {
            Text(
              stringResource(
                R.string.setup_word_counter,
                state.secretWord.length,
                GameRules.MAX_WORD_LENGTH,
              )
            )
          },
          modifier = Modifier.testTag(SECRET_WORD_INPUT_TAG),
        )

        Spacer(modifier = Modifier.height(10.dp))

        GameTextField(
          value = state.hint,
          onValueChange = onHintChange,
          label = stringResource(R.string.setup_hint_label),
          leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
          imeAction = ImeAction.Done,
          modifier = Modifier.testTag("hint_input"),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onStartRound,
          enabled = state.canStartRound,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag(SUBMIT_SETUP_TAG),
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text =
              when {
                state.secretWord.isEmpty() -> stringResource(R.string.setup_submit_empty)
                !state.canStartRound ->
                  stringResource(R.string.setup_submit_too_short, GameRules.MIN_WORD_LENGTH)
                else -> stringResource(R.string.setup_submit_ready)
              },
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    MatchHistoryCard(history = history, onClearHistory = onClearHistory)
  }
}

@Composable
private fun AppHeader(soundEnabled: Boolean, onToggleSound: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier.size(40.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = stringResource(R.string.app_initial),
          color = MaterialTheme.colorScheme.onPrimaryContainer,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = stringResource(R.string.app_name),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = stringResource(R.string.app_tagline),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    SoundToggleButton(soundEnabled = soundEnabled, onToggle = onToggleSound)
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(bottom = 12.dp),
  )
}
