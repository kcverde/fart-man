package com.kcverde.fartman.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kcverde.fartman.data.DataStoreSettings
import com.kcverde.fartman.data.GameDatabase
import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.data.GameRepository
import com.kcverde.fartman.data.SettingsStore
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FartManViewModel(
  private val repository: GameRepository,
  private val settings: SettingsStore,
  private val soundPlayer: SoundPlayer,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val _uiState = MutableStateFlow(savedStateHandle.restoreRound())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  /**
   * Fires when a guess is wrong, so the screen can rumble and buzz.
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
    // The mute setting lives in storage, not in the saved round, so it applies
    // to a cold start as well as a restored one.
    viewModelScope.launch {
      val enabled = settings.soundEnabled.first()
      soundPlayer.isMuted = !enabled
      update { it.copy(soundEnabled = enabled) }
    }
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
    it.copy(secretWord = word.filter(Char::isLetter).uppercase().take(GameRules.MAX_WORD_LENGTH))
  }

  fun updateHint(hint: String) = update { it.copy(hint = hint.take(GameRules.MAX_HINT_LENGTH)) }

  fun toggleSound() {
    val enabled = !_uiState.value.soundEnabled
    soundPlayer.isMuted = !enabled
    update { it.copy(soundEnabled = enabled) }
    viewModelScope.launch { settings.setSoundEnabled(enabled) }
  }

  fun startRound() {
    val state = _uiState.value
    if (state.phase != GamePhase.SETUP || !state.canStartRound) return
    update { it.copy(phase = GamePhase.PASSING, guessedLetters = emptySet(), incorrectCount = 0) }
    soundPlayer.playCorrectBubble()
  }

  fun startGuessing() {
    if (_uiState.value.phase != GamePhase.PASSING) return
    update { it.copy(phase = GamePhase.ACTIVE) }
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
        set(next)
        soundPlayer.playCorrectBubble()
      }
      return
    }

    val next = state.copy(guessedLetters = guessed, incorrectCount = state.incorrectCount + 1)
    _shakeEvents.tryEmit(Unit)
    if (next.incorrectCount >= GameRules.MAX_INCORRECT) {
      finish(next, won = false)
    } else {
      set(next)
      soundPlayer.playIncorrectPuff()
    }
  }

  /**
   * Ends the round immediately.
   *
   * The old Give Up button dispatched `guessLetter('?')`, which merely added a
   * single wrong guess, so it took six presses from full health to do anything.
   */
  fun giveUp() {
    val state = _uiState.value
    if (state.phase != GamePhase.ACTIVE) return
    finish(state.copy(incorrectCount = GameRules.MAX_INCORRECT, gaveUp = true), won = false)
  }

  private fun finish(state: GameUiState, won: Boolean) {
    set(state.copy(phase = if (won) GamePhase.VICTORY else GamePhase.DEFEAT))
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

  // --- Plumbing ------------------------------------------------------------

  private inline fun update(transform: (GameUiState) -> GameUiState) = set(transform(_uiState.value))

  /**
   * The single write path, so nothing can change the round without also
   * recording it for process death.
   */
  private fun set(state: GameUiState) {
    _uiState.value = state
    savedStateHandle.saveRound(state)
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
          settings = DataStoreSettings(app),
          soundPlayer = AndroidSoundPlayer(app),
          savedStateHandle = createSavedStateHandle(),
        )
      }
    }
  }
}

// --- Saved state -----------------------------------------------------------
// Stored as primitives rather than a Parcelable so GameUiState can stay in the
// Android-free game package. soundEnabled is deliberately absent: it comes from
// SettingsStore, which is durable rather than per-process.

private const val KEY_PHASE = "phase"
private const val KEY_CREATOR = "creator"
private const val KEY_GUESSER = "guesser"
private const val KEY_WORD = "word"
private const val KEY_HINT = "hint"
private const val KEY_GUESSED = "guessed"
private const val KEY_INCORRECT = "incorrect"
private const val KEY_GAVE_UP = "gaveUp"

private fun SavedStateHandle.saveRound(state: GameUiState) {
  this[KEY_PHASE] = state.phase.name
  this[KEY_CREATOR] = state.creatorName
  this[KEY_GUESSER] = state.guesserName
  this[KEY_WORD] = state.secretWord
  this[KEY_HINT] = state.hint
  this[KEY_GUESSED] = state.guessedLetters.joinToString(separator = "")
  this[KEY_INCORRECT] = state.incorrectCount
  this[KEY_GAVE_UP] = state.gaveUp
}

private fun SavedStateHandle.restoreRound(): GameUiState {
  val phase = get<String>(KEY_PHASE)?.let(GamePhase::valueOf) ?: return GameUiState()
  val default = GameUiState()
  return GameUiState(
    phase = phase,
    creatorName = get<String>(KEY_CREATOR) ?: default.creatorName,
    guesserName = get<String>(KEY_GUESSER) ?: default.guesserName,
    secretWord = get<String>(KEY_WORD).orEmpty(),
    hint = get<String>(KEY_HINT).orEmpty(),
    guessedLetters = get<String>(KEY_GUESSED).orEmpty().toSet(),
    incorrectCount = get<Int>(KEY_INCORRECT) ?: 0,
    gaveUp = get<Boolean>(KEY_GAVE_UP) ?: false,
  )
}
