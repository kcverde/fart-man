package com.kcverde.fartman.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kcverde.fartman.data.GameDatabase
import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.data.GameRepository
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.game.GameRules
import com.kcverde.fartman.game.GameUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FartManViewModel(
  private val repository: GameRepository,
  private val soundPlayer: SoundPlayer,
) : ViewModel() {

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  /**
   * Fires when a guess is wrong, so the screen can rumble.
   *
   * Buffered rather than suspending: a plain `MutableSharedFlow` makes `emit`
   * wait for a collector, which stalls the guess while the screen is in the
   * background.
   */
  private val _shakeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  val shakeEvents: SharedFlow<Unit> = _shakeEvents.asSharedFlow()

  val history: StateFlow<List<GameRecord>> =
    repository.history.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
      initialValue = emptyList(),
    )

  init {
    soundPlayer.isMuted = !_uiState.value.soundEnabled
  }

  // --- Setup ---------------------------------------------------------------

  fun updateCreatorName(name: String) = update {
    it.copy(creatorName = name.take(GameRules.MAX_NAME_LENGTH))
  }

  fun updateGuesserName(name: String) = update {
    it.copy(guesserName = name.take(GameRules.MAX_NAME_LENGTH))
  }

  /** Letters only, so the keyboard can always reach every character. */
  fun updateSecretWord(word: String) = update {
    it.copy(
      secretWord = word.filter(Char::isLetter).uppercase().take(GameRules.MAX_WORD_LENGTH)
    )
  }

  fun updateHint(hint: String) = update { it.copy(hint = hint.take(GameRules.MAX_HINT_LENGTH)) }

  fun toggleSound() = update {
    val enabled = !it.soundEnabled
    soundPlayer.isMuted = !enabled
    it.copy(soundEnabled = enabled)
  }

  fun startRound() {
    val state = _uiState.value
    if (state.phase != GamePhase.SETUP || !state.canStartRound) return
    _uiState.value =
      state.copy(phase = GamePhase.PASSING, guessedLetters = emptySet(), incorrectCount = 0)
    soundPlayer.playCorrectBubble()
  }

  fun startGuessing() {
    val state = _uiState.value
    if (state.phase != GamePhase.PASSING) return
    _uiState.value = state.copy(phase = GamePhase.ACTIVE)
    soundPlayer.playRoundStart()
  }

  // --- Playing -------------------------------------------------------------

  fun guessLetter(letter: Char) {
    val state = _uiState.value
    val guess = letter.uppercaseChar()
    if (state.phase != GamePhase.ACTIVE || !guess.isLetter() || guess in state.guessedLetters) {
      return
    }

    val guessed = state.guessedLetters + guess

    if (guess in state.secretWord) {
      val next = state.copy(guessedLetters = guessed)
      if (next.isWordComplete) {
        finish(next, won = true)
      } else {
        _uiState.value = next
        soundPlayer.playCorrectBubble()
      }
      return
    }

    val next = state.copy(guessedLetters = guessed, incorrectCount = state.incorrectCount + 1)
    _shakeEvents.tryEmit(Unit)
    if (next.incorrectCount >= GameRules.MAX_INCORRECT) {
      finish(next, won = false)
    } else {
      _uiState.value = next
      soundPlayer.playIncorrectPuff()
    }
  }

  /**
   * Ends the round immediately.
   *
   * The old Give Up button dispatched `guessLetter('?')`, which merely added a
   * single wrong guess, so it took six taps from full health to do anything.
   */
  fun giveUp() {
    val state = _uiState.value
    if (state.phase != GamePhase.ACTIVE) return
    finish(state.copy(incorrectCount = GameRules.MAX_INCORRECT, gaveUp = true), won = false)
  }

  private fun finish(state: GameUiState, won: Boolean) {
    _uiState.value = state.copy(phase = if (won) GamePhase.VICTORY else GamePhase.DEFEAT)
    if (won) soundPlayer.playVictoryDeflate() else soundPlayer.playMegaFartExplosion()

    viewModelScope.launch {
      repository.insert(
        GameRecord(
          creatorName = state.creatorName.ifBlank { GameRules.DEFAULT_CREATOR_NAME },
          guesserName = state.guesserName.ifBlank { GameRules.DEFAULT_GUESSER_NAME },
          secretWord = state.secretWord,
          isWin = won,
          incorrectGuesses = state.incorrectCount,
          hintString = state.hint,
        )
      )
    }
  }

  // --- Between rounds ------------------------------------------------------

  fun playAgain() = update { it.clearedForNewRound() }

  /** Same two people, opposite chairs. */
  fun swapRolesAndPlayAgain() = update {
    it.clearedForNewRound().copy(creatorName = it.guesserName, guesserName = it.creatorName)
  }

  fun clearHistory() {
    viewModelScope.launch { repository.clearAll() }
  }

  private inline fun update(transform: (GameUiState) -> GameUiState) {
    _uiState.value = transform(_uiState.value)
  }

  override fun onCleared() {
    soundPlayer.release()
  }

  companion object {
    private const val STOP_TIMEOUT_MILLIS = 5_000L

    fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
      initializer {
        val app = context.applicationContext
        FartManViewModel(
          repository = GameRepository(GameDatabase.getDatabase(app).gameDao()),
          soundPlayer = AndroidSoundPlayer(app),
        )
      }
    }
  }
}
