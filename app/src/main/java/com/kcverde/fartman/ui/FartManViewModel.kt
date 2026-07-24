package com.kcverde.fartman.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.data.GameRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class GamePhase {
    SETUP,
    PASSING,
    ACTIVE,
    VICTORY,
    DEFEAT
}

class FartManViewModel(private val repository: GameRepository) : ViewModel() {

    // Gameplay states
    private val _gamePhase = MutableStateFlow(GamePhase.SETUP)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _creatorName = MutableStateFlow("Word Master")
    val creatorName: StateFlow<String> = _creatorName.asStateFlow()

    private val _guesserName = MutableStateFlow("Gassy Guesser")
    val guesserName: StateFlow<String> = _guesserName.asStateFlow()

    private val _secretWord = MutableStateFlow("")
    val secretWord: StateFlow<String> = _secretWord.asStateFlow()

    private val _hintText = MutableStateFlow("")
    val hintText: StateFlow<String> = _hintText.asStateFlow()

    private val _guessedLetters = MutableStateFlow<Set<Char>>(emptySet())
    val guessedLetters: StateFlow<Set<Char>> = _guessedLetters.asStateFlow()

    private val _incorrectCount = MutableStateFlow(0)
    val incorrectCount: StateFlow<Int> = _incorrectCount.asStateFlow()

    val maxIncorrect = 6 // 6 stages of bloating before explosion!

    // Sound enabled setting state
    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    init {
        FartSoundPlayer.isMuted = !_soundEnabled.value
    }

    fun toggleSound() {
        val nextVal = !_soundEnabled.value
        _soundEnabled.value = nextVal
        FartSoundPlayer.isMuted = !nextVal
    }

    // Shake event flow (to trigger a rumble shake animation in UI on error)
    private val _shakeEvent = MutableSharedFlow<Unit>()
    val shakeEvent: SharedFlow<Unit> = _shakeEvent.asSharedFlow()

    // History Flow from Room DB
    val gameHistory: StateFlow<List<GameRecord>> = repository.history
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateCreatorName(name: String) {
        _creatorName.value = name.take(15)
    }

    fun updateGuesserName(name: String) {
        _guesserName.value = name.take(15)
    }

    fun updateSecretWord(word: String) {
        // Only accept letters, ignore numbers/special chars to prevent impossible games
        _secretWord.value = word.filter { it.isLetter() }.uppercase()
    }

    fun updateHintText(hint: String) {
        _hintText.value = hint.take(50)
    }

    // Submit setup parameters and proceed to Pass Phone phase
    fun submitSetup() {
        if (_secretWord.value.isNotBlank()) {
            _guessedLetters.value = emptySet()
            _incorrectCount.value = 0
            _gamePhase.value = GamePhase.PASSING
            FartSoundPlayer.playCorrectBubble() // satisfying feedback on lock
        }
    }

    // Pass was accepted, player starts guessing
    fun startGuessing() {
        if (_gamePhase.value == GamePhase.PASSING) {
            _gamePhase.value = GamePhase.ACTIVE
            FartSoundPlayer.playTada() // Tada sound as guessing starts
        }
    }

    // Action of guessing a letter
    fun guessLetter(letter: Char) {
        val uppercaseLetter = letter.uppercaseChar()
        if (_gamePhase.value != GamePhase.ACTIVE || _guessedLetters.value.contains(uppercaseLetter)) {
            return
        }

        // Add to guessed list
        _guessedLetters.value = _guessedLetters.value + uppercaseLetter

        // Check if letter exists in secret word
        if (!_secretWord.value.contains(uppercaseLetter)) {
            // Incorrect guess!
            val newIncorrectCount = _incorrectCount.value + 1
            _incorrectCount.value = newIncorrectCount

            // Trigger physical rumble shake visual effect
            viewModelScope.launch {
                _shakeEvent.emit(Unit)
            }

            // Check if game over (farted!)
            if (newIncorrectCount >= maxIncorrect) {
                gameOver(isWin = false)
            } else {
                FartSoundPlayer.playIncorrectPuff() // play sub-terminal incorrect puff sound
            }
        } else {
            // Correct guess! Check if entire word has been uncovered
            val allLettersGuessed = _secretWord.value.all { _guessedLetters.value.contains(it) }
            if (allLettersGuessed) {
                gameOver(isWin = true)
            } else {
                FartSoundPlayer.playCorrectBubble() // correct bubble sound!
            }
        }
    }

    private fun gameOver(isWin: Boolean) {
        _gamePhase.value = if (isWin) GamePhase.VICTORY else GamePhase.DEFEAT
        
        if (isWin) {
            FartSoundPlayer.playVictoryDeflate()
        } else {
            FartSoundPlayer.playMegaFartExplosion()
        }

        // Save result to Room database
        viewModelScope.launch {
            val record = GameRecord(
                creatorName = _creatorName.value.ifBlank { "Word Master" },
                guesserName = _guesserName.value.ifBlank { "Gassy Guesser" },
                secretWord = _secretWord.value,
                isWin = isWin,
                incorrectGuesses = _incorrectCount.value,
                hintString = _hintText.value
            )
            repository.insert(record)
        }
    }

    // Return to setup for a new game
    fun resetToSetup() {
        _secretWord.value = ""
        _hintText.value = ""
        _guessedLetters.value = emptySet()
        _incorrectCount.value = 0
        _gamePhase.value = GamePhase.SETUP
        FartSoundPlayer.playCorrectBubble()
    }

    fun quickPlayAgain() {
        // Swap creator and guesser for quick rematch!
        val oldCreator = _creatorName.value
        val oldGuesser = _guesserName.value
        _creatorName.value = oldGuesser
        _guesserName.value = oldCreator

        _secretWord.value = ""
        _hintText.value = ""
        _guessedLetters.value = emptySet()
        _incorrectCount.value = 0
        _gamePhase.value = GamePhase.SETUP
        FartSoundPlayer.playCorrectBubble()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

class FartManViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FartManViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FartManViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
