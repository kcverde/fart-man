package com.kcverde.fartman.game

/** The tuning knobs for a round of Fart Man, in one place. */
object GameRules {
  /** Wrong guesses before Fart Man detonates. Matches the six drawable stages. */
  const val MAX_INCORRECT = 6

  const val MIN_WORD_LENGTH = 2

  /**
   * Beyond this the dashes stop fitting on a phone even when wrapped, and the
   * guesser can no longer hold the word in their head anyway.
   */
  const val MAX_WORD_LENGTH = 16

  const val MAX_NAME_LENGTH = 15
  const val MAX_HINT_LENGTH = 50

  /** Prefilled so a round can start without typing names. */
  const val DEFAULT_CREATOR_NAME = "Word Master"
  const val DEFAULT_GUESSER_NAME = "Gassy Guesser"
}

enum class GamePhase {
  /** Creator enters the word and hint. */
  SETUP,
  /** Handover screen, so the guesser doesn't see the word. */
  PASSING,
  /** Guesser is picking letters. */
  ACTIVE,
  VICTORY,
  DEFEAT,
}
