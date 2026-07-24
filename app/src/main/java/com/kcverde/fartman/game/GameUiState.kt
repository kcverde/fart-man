package com.kcverde.fartman.game

import androidx.compose.runtime.Immutable

/**
 * The whole of a round, in one immutable value.
 *
 * This replaced eight separate `MutableStateFlow`s, which let the screen
 * observe a half-applied round: a letter could land in `guessedLetters` a frame
 * before `incorrectCount` caught up. Derived values live here as computed
 * properties so the UI and the tests agree on what "won" means.
 */
@Immutable
data class GameUiState(
  val phase: GamePhase = GamePhase.SETUP,
  val creatorName: String = GameRules.DEFAULT_CREATOR_NAME,
  val guesserName: String = GameRules.DEFAULT_GUESSER_NAME,
  val secretWord: String = "",
  val hint: String = "",
  val guessedLetters: Set<Char> = emptySet(),
  val incorrectCount: Int = 0,
  val gaveUp: Boolean = false,
  val soundEnabled: Boolean = true,
) {
  val mistakesRemaining: Int
    get() = (GameRules.MAX_INCORRECT - incorrectCount).coerceAtLeast(0)

  /** 0f at full health, 1f at detonation. Drives the bloat meter. */
  val bloatFraction: Float
    get() = incorrectCount.toFloat() / GameRules.MAX_INCORRECT

  val isWordComplete: Boolean
    get() = secretWord.isNotEmpty() && secretWord.all { it in guessedLetters }

  val canStartRound: Boolean
    get() = secretWord.length >= GameRules.MIN_WORD_LENGTH

  fun isRevealed(letter: Char): Boolean = letter in guessedLetters

  /** Letters that turned out to be in the word. Used to color the keyboard. */
  fun wasCorrect(letter: Char): Boolean = letter in guessedLetters && letter in secretWord

  /** Clears the round but keeps the players and their sound preference. */
  fun clearedForNewRound(): GameUiState =
    copy(
      phase = GamePhase.SETUP,
      secretWord = "",
      hint = "",
      guessedLetters = emptySet(),
      incorrectCount = 0,
      gaveUp = false,
    )
}
