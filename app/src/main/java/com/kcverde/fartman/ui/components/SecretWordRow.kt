package com.kcverde.fartman.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcverde.fartman.R

const val SECRET_WORD_TAG = "secret_word"

/**
 * The dashes, one per letter, filling in as they're guessed.
 *
 * Wraps: the original laid these out in a plain `Row`, so anything past about
 * ten letters ran off the side of the screen with no way to see it.
 *
 * The individual slots are hidden from accessibility and the whole row carries
 * one description instead, so TalkBack reads "C blank blank K E" rather than
 * announcing sixteen separate unlabeled boxes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecretWordRow(word: String, guessedLetters: Set<Char>, modifier: Modifier = Modifier) {
  val blank = stringResource(R.string.blank_letter)
  val spelled =
    remember(word, guessedLetters, blank) {
      word
        .map { char -> if (char in guessedLetters) char.toString() else blank }
        .joinToString(separator = " ")
    }
  val spokenProgress = stringResource(R.string.secret_word_progress, spelled)

  FlowRow(
    modifier =
      modifier.fillMaxWidth().testTag(SECRET_WORD_TAG).semantics {
        contentDescription = spokenProgress
      },
    // Spacing matters here: without a gap the underlines of adjacent letters
    // butt together and read as one long bar instead of separate slots.
    horizontalArrangement = Arrangement.spacedBy(SLOT_GAP, Alignment.CenterHorizontally),
    verticalArrangement = Arrangement.spacedBy(SLOT_GAP),
  ) {
    word.forEach { char ->
      val revealed = char in guessedLetters

      // A letter drops in rather than blinking on, so a guess that fills three
      // slots at once reads as one event.
      val reveal by
        animateFloatAsState(
          targetValue = if (revealed) 1f else 0f,
          animationSpec = tween(REVEAL_MILLIS),
          label = "reveal",
        )
      val underlineColor by
        animateColorAsState(
          targetValue =
            if (revealed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant,
          animationSpec = tween(REVEAL_MILLIS),
          label = "underline",
        )

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.size(width = SLOT_WIDTH, height = SLOT_HEIGHT).clearAndSetSemantics {},
      ) {
        Text(
          // Unrevealed slots never render the character at all, animated or
          // otherwise, so it cannot be recovered from the screen.
          text = if (revealed) char.toString() else " ",
          fontSize = 26.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier =
            Modifier.graphicsLayer {
              alpha = reveal
              val scale = REVEAL_START_SCALE + (1f - REVEAL_START_SCALE) * reveal
              scaleX = scale
              scaleY = scale
            },
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
          modifier =
            Modifier.fillMaxWidth().height(4.dp).background(underlineColor, RoundedCornerShape(2.dp))
        )
      }
    }
  }
}

private val SLOT_WIDTH = 26.dp
private val SLOT_HEIGHT = 46.dp
private val SLOT_GAP = 8.dp
private const val REVEAL_MILLIS = 260
private const val REVEAL_START_SCALE = 0.5f
