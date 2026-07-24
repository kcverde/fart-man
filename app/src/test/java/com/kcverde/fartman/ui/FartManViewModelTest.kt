package com.kcverde.fartman.ui

import androidx.lifecycle.SavedStateHandle
import com.kcverde.fartman.data.GameRepository
import com.kcverde.fartman.data.InMemorySettingsStore
import com.kcverde.fartman.game.GamePhase
import com.kcverde.fartman.game.GameRules
import com.kcverde.fartman.testing.FakeGameDao
import com.kcverde.fartman.testing.MainDispatcherRule
import com.kcverde.fartman.testing.RecordingSoundPlayer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FartManViewModelTest {

  private val dispatcher = UnconfinedTestDispatcher()

  @get:Rule val mainDispatcherRule = MainDispatcherRule(dispatcher)

  private val dao = FakeGameDao()
  private val sound = RecordingSoundPlayer()
  private val settings = InMemorySettingsStore()
  private val savedState = SavedStateHandle()
  private lateinit var viewModel: FartManViewModel

  // Constructed here rather than in a field initializer, which would run before
  // the rule installs the test main dispatcher.
  @Before
  fun setUp() {
    viewModel = FartManViewModel(GameRepository(dao), settings, sound, savedState)
  }

  private val state
    get() = viewModel.uiState.value

  /** Gets a round to the point where letters can be guessed. */
  private fun playRound(word: String, hint: String = "") {
    viewModel.updateSecretWord(word)
    viewModel.updateHint(hint)
    viewModel.startRound()
    viewModel.startGuessing()
  }

  // --- Input handling ------------------------------------------------------

  @Test
  fun `secret word keeps only letters and uppercases them`() {
    viewModel.updateSecretWord("g4s-b0mb!")
    assertEquals("GSBMB", state.secretWord)
  }

  @Test
  fun `secret word is capped so the dashes stay on screen`() {
    viewModel.updateSecretWord("A".repeat(GameRules.MAX_WORD_LENGTH + 10))
    assertEquals(GameRules.MAX_WORD_LENGTH, state.secretWord.length)
  }

  @Test
  fun `names and hints are capped`() {
    viewModel.updateCreatorName("x".repeat(50))
    viewModel.updateGuesserName("y".repeat(50))
    viewModel.updateHint("z".repeat(100))

    assertEquals(GameRules.MAX_NAME_LENGTH, state.creatorName.length)
    assertEquals(GameRules.MAX_NAME_LENGTH, state.guesserName.length)
    assertEquals(GameRules.MAX_HINT_LENGTH, state.hint.length)
  }

  @Test
  fun `a round will not start below the minimum word length`() {
    viewModel.updateSecretWord("A")
    viewModel.startRound()
    assertEquals(GamePhase.SETUP, state.phase)
  }

  @Test
  fun `starting a round goes through the handover before guessing`() {
    viewModel.updateSecretWord("GAS")
    viewModel.startRound()
    assertEquals(GamePhase.PASSING, state.phase)

    viewModel.startGuessing()
    assertEquals(GamePhase.ACTIVE, state.phase)
  }

  // --- Guessing ------------------------------------------------------------

  @Test
  fun `guessing every letter wins and banks the result`() {
    playRound("GAS")

    "GAS".forEach(viewModel::guessLetter)

    assertEquals(GamePhase.VICTORY, state.phase)
    assertEquals(0, state.incorrectCount)
    assertTrue(sound.played.contains("victory"))

    val record = dao.saved.single()
    assertTrue(record.isWin)
    assertEquals("GAS", record.secretWord)
    assertEquals(0, record.incorrectGuesses)
  }

  @Test
  fun `a lowercase guess matches the uppercase word`() {
    playRound("GAS")
    viewModel.guessLetter('g')
    assertTrue(state.isRevealed('G'))
    assertEquals(0, state.incorrectCount)
  }

  @Test
  fun `one guess reveals every copy of a repeated letter`() {
    playRound("POOT")
    listOf('P', 'O', 'T').forEach(viewModel::guessLetter)
    assertEquals(GamePhase.VICTORY, state.phase)
  }

  @Test
  fun `running out of guesses detonates and banks a loss`() {
    playRound("GAS")

    "BCDFHJ".forEach(viewModel::guessLetter)

    assertEquals(GamePhase.DEFEAT, state.phase)
    assertEquals(GameRules.MAX_INCORRECT, state.incorrectCount)
    assertFalse(state.gaveUp)
    assertTrue(sound.played.contains("explosion"))
    assertFalse(dao.saved.single().isWin)
  }

  @Test
  fun `repeating a guess costs nothing`() {
    playRound("GAS")

    repeat(5) { viewModel.guessLetter('Z') }

    assertEquals(1, state.incorrectCount)
    assertEquals(GamePhase.ACTIVE, state.phase)
  }

  @Test
  fun `non-letter guesses are ignored`() {
    playRound("GAS")

    listOf('?', '1', '-', ' ').forEach(viewModel::guessLetter)

    assertEquals(0, state.incorrectCount)
    assertEquals(emptySet<Char>(), state.guessedLetters)
  }

  @Test
  fun `guesses outside the active phase are ignored`() {
    viewModel.updateSecretWord("GAS")
    viewModel.guessLetter('G')
    assertEquals(emptySet<Char>(), state.guessedLetters)

    viewModel.startRound() // PASSING: the guesser has not taken the phone yet
    viewModel.guessLetter('G')
    assertEquals(emptySet<Char>(), state.guessedLetters)
  }

  @Test
  fun `a wrong guess signals a shake`() = runTest(dispatcher) {
    val shakes = mutableListOf<Unit>()
    backgroundScope.launch { viewModel.shakeEvents.collect { shakes += it } }

    playRound("GAS")
    viewModel.guessLetter('Z')

    assertEquals(1, shakes.size)
  }

  // --- Give up -------------------------------------------------------------

  @Test
  fun `giving up ends the round on the first press`() {
    playRound("GAS")

    viewModel.giveUp()

    assertEquals(GamePhase.DEFEAT, state.phase)
    assertTrue(state.gaveUp)
    assertEquals(GameRules.MAX_INCORRECT, state.incorrectCount)
    assertFalse(dao.saved.single().isWin)
  }

  @Test
  fun `giving up outside a live round does nothing`() {
    viewModel.giveUp()
    assertEquals(GamePhase.SETUP, state.phase)
    assertTrue(dao.saved.isEmpty())
  }

  // --- Between rounds ------------------------------------------------------

  @Test
  fun `play again keeps the players and clears the word`() {
    viewModel.updateCreatorName("Ada")
    viewModel.updateGuesserName("Bob")
    playRound("GAS", hint = "smelly")
    viewModel.giveUp()

    viewModel.playAgain()

    assertEquals(GamePhase.SETUP, state.phase)
    assertEquals("Ada", state.creatorName)
    assertEquals("Bob", state.guesserName)
    assertEquals("", state.secretWord)
    assertEquals("", state.hint)
    assertEquals(0, state.incorrectCount)
    assertFalse(state.gaveUp)
  }

  @Test
  fun `swapping roles trades the two players`() {
    viewModel.updateCreatorName("Ada")
    viewModel.updateGuesserName("Bob")
    playRound("GAS")
    viewModel.giveUp()

    viewModel.swapRolesAndPlayAgain()

    assertEquals("Bob", state.creatorName)
    assertEquals("Ada", state.guesserName)
    assertEquals(GamePhase.SETUP, state.phase)
  }

  @Test
  fun `history can be cleared`() = runTest(dispatcher) {
    playRound("GAS")
    viewModel.giveUp()
    assertEquals(1, dao.saved.size)

    viewModel.clearHistory()

    assertTrue(dao.saved.isEmpty())
  }

  // --- Sound ---------------------------------------------------------------

  @Test
  fun `toggling sound mutes the player and survives a new round`() {
    assertTrue(state.soundEnabled)
    assertFalse(sound.isMuted)

    viewModel.toggleSound()

    assertFalse(state.soundEnabled)
    assertTrue(sound.isMuted)

    playRound("GAS")
    viewModel.giveUp()
    viewModel.playAgain()

    assertFalse("the mute setting should outlast the round", state.soundEnabled)
  }

  @Test
  fun `muting is written to storage and reloaded on the next launch`() = runTest(dispatcher) {
    viewModel.toggleSound()
    assertFalse(settings.soundEnabled.first())

    // A brand new ViewModel, as after the process is killed and relaunched.
    val relaunched = FartManViewModel(GameRepository(dao), settings, sound, SavedStateHandle())

    assertFalse(relaunched.uiState.value.soundEnabled)
    assertTrue(sound.isMuted)
  }

  // --- Surviving process death ---------------------------------------------

  @Test
  fun `a round in progress is restored from saved state`() {
    viewModel.updateCreatorName("Ada")
    viewModel.updateGuesserName("Bob")
    playRound("METHANE", hint = "swamp gas")
    listOf('E', 'Z').forEach(viewModel::guessLetter)

    // Same saved state, fresh ViewModel: the process was killed mid-round.
    val restored = FartManViewModel(GameRepository(dao), settings, sound, savedState)
    val state = restored.uiState.value

    assertEquals(GamePhase.ACTIVE, state.phase)
    assertEquals("METHANE", state.secretWord)
    assertEquals("swamp gas", state.hint)
    assertEquals("Ada", state.creatorName)
    assertEquals("Bob", state.guesserName)
    assertEquals(setOf('E', 'Z'), state.guessedLetters)
    assertEquals(1, state.incorrectCount)
  }

  @Test
  fun `a restored round can be played to a finish`() {
    playRound("GAS")
    viewModel.guessLetter('G')

    val restored = FartManViewModel(GameRepository(dao), settings, sound, savedState)
    "AS".forEach(restored::guessLetter)

    assertEquals(GamePhase.VICTORY, restored.uiState.value.phase)
  }

  @Test
  fun `an untouched saved state yields a fresh game`() {
    val fresh = FartManViewModel(GameRepository(dao), settings, sound, SavedStateHandle())
    assertEquals(GamePhase.SETUP, fresh.uiState.value.phase)
    assertEquals("", fresh.uiState.value.secretWord)
  }
}
