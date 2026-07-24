package com.kcverde.fartman.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure state maths, no Android involved. */
class GameUiStateTest {

  @Test
  fun `word is complete only when every distinct letter is guessed`() {
    val state = GameUiState(secretWord = "BEANS", guessedLetters = setOf('B', 'E', 'A', 'N'))
    assertFalse(state.isWordComplete)
    assertTrue(state.copy(guessedLetters = state.guessedLetters + 'S').isWordComplete)
  }

  @Test
  fun `repeated letters are revealed by a single guess`() {
    val state = GameUiState(secretWord = "POOT", guessedLetters = setOf('P', 'O', 'T'))
    assertTrue(state.isWordComplete)
  }

  @Test
  fun `an empty word is never complete`() {
    assertFalse(GameUiState(secretWord = "").isWordComplete)
  }

  @Test
  fun `mistakes remaining never goes negative`() {
    val state = GameUiState(incorrectCount = GameRules.MAX_INCORRECT + 3)
    assertEquals(0, state.mistakesRemaining)
  }

  @Test
  fun `bloat runs from zero to one across the allowed mistakes`() {
    assertEquals(0f, GameUiState().bloatFraction, 0.001f)
    assertEquals(
      1f,
      GameUiState(incorrectCount = GameRules.MAX_INCORRECT).bloatFraction,
      0.001f,
    )
  }

  @Test
  fun `a round needs at least the minimum word length to start`() {
    assertFalse(GameUiState(secretWord = "").canStartRound)
    assertFalse(GameUiState(secretWord = "A").canStartRound)
    assertTrue(GameUiState(secretWord = "GO").canStartRound)
  }

  @Test
  fun `wasCorrect distinguishes a hit from a miss among the guesses`() {
    val state = GameUiState(secretWord = "GAS", guessedLetters = setOf('G', 'Z'))
    assertTrue(state.wasCorrect('G'))
    assertFalse(state.wasCorrect('Z'))
    assertFalse("an unguessed letter is not yet correct", state.wasCorrect('A'))
  }

  @Test
  fun `clearing for a new round keeps the players and the sound setting`() {
    val finished =
      GameUiState(
        phase = GamePhase.DEFEAT,
        creatorName = "Ada",
        guesserName = "Bob",
        secretWord = "STINK",
        hint = "smell",
        guessedLetters = setOf('S', 'T'),
        incorrectCount = 6,
        gaveUp = true,
        soundEnabled = false,
      )

    val next = finished.clearedForNewRound()

    assertEquals(GamePhase.SETUP, next.phase)
    assertEquals("Ada", next.creatorName)
    assertEquals("Bob", next.guesserName)
    assertFalse(next.soundEnabled)
    assertEquals("", next.secretWord)
    assertEquals("", next.hint)
    assertEquals(emptySet<Char>(), next.guessedLetters)
    assertEquals(0, next.incorrectCount)
    assertFalse(next.gaveUp)
  }
}
