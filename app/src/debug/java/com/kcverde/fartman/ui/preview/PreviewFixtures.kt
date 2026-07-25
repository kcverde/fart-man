package com.kcverde.fartman.ui.preview

import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.game.GameUiState

/**
 * The sample rounds the previews and the screenshot goldens both render.
 *
 * They live in the debug source set because the previews do, and the debug
 * variant's unit tests compile against it. Sharing them is the point: a preview
 * that drifts from the golden it is supposed to mirror is worse than no preview,
 * because it looks authoritative.
 */

/** A round waiting to start, with both players named. */
val SETUP_ROUND =
  GameUiState(creatorName = "Ada", guesserName = "Bob", secretWord = "METHANE")

/** Mid-round: E, A and T are hits; Z and Q are the two misses. */
val MID_GAME =
  GameUiState(
    phase = GamePhase.ACTIVE,
    creatorName = "Ada",
    guesserName = "Bob",
    secretWord = "METHANE",
    hint = "Swamp gas",
    guessedLetters = setOf('E', 'A', 'T', 'Z', 'Q'),
    incorrectCount = 2,
  )

val SAMPLE_HISTORY =
  listOf(
    GameRecord(
      id = 1,
      creatorName = "Ada",
      guesserName = "Bob",
      secretWord = "METHANE",
      isWin = true,
      incorrectGuesses = 2,
      hintString = "Swamp gas",
      timestamp = 1_700_000_000_000L,
    ),
    GameRecord(
      id = 2,
      creatorName = "Bob",
      guesserName = "Ada",
      secretWord = "SULFUR",
      isWin = false,
      incorrectGuesses = 6,
      hintString = "",
      timestamp = 1_700_003_600_000L,
    ),
  )
