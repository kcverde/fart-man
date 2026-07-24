package com.kcverde.fartman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcverde.fartman.R
import com.kcverde.fartman.game.GameUiState
import com.kcverde.fartman.ui.theme.extendedColors

/**
 * A to Z.
 *
 * A wrapping [FlowRow] rather than the original `LazyVerticalGrid`: there are
 * only ever 26 keys, so laziness bought nothing and cost a nested scroll
 * container inside an already scrolling column.
 *
 * Each key states its outcome in its content description, since correct and
 * incorrect are otherwise distinguished by color alone.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterKeyboard(state: GameUiState, onGuess: (Char) -> Unit, modifier: Modifier = Modifier) {
  FlowRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(KEY_SPACING, Alignment.CenterHorizontally),
    verticalArrangement = Arrangement.spacedBy(KEY_SPACING),
    maxItemsInEachRow = KEYS_PER_ROW,
  ) {
    ('A'..'Z').forEach { letter ->
      val guessed = letter in state.guessedLetters
      val correct = state.wasCorrect(letter)

      val background =
        when {
          !guessed -> MaterialTheme.colorScheme.surfaceContainerLowest
          correct -> MaterialTheme.extendedColors.successContainer
          else -> MaterialTheme.colorScheme.errorContainer
        }
      val foreground =
        when {
          !guessed -> MaterialTheme.colorScheme.onSurface
          correct -> MaterialTheme.extendedColors.onSuccessContainer
          else -> MaterialTheme.colorScheme.onErrorContainer
        }

      val label =
        when {
          !guessed -> stringResource(R.string.key_unguessed, letter.toString())
          correct -> stringResource(R.string.key_correct, letter.toString())
          else -> stringResource(R.string.key_incorrect, letter.toString())
        }

      Box(
        modifier =
          Modifier.size(KEY_SIZE)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .clickable(enabled = !guessed, role = Role.Button) { onGuess(letter) }
            .testTag(keyTag(letter))
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = letter.toString(),
          color = foreground,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

fun keyTag(letter: Char): String = "key_$letter"

private val KEY_SIZE = 42.dp
private val KEY_SPACING = 6.dp
private const val KEYS_PER_ROW = 7
