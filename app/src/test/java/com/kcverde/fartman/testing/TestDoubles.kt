package com.kcverde.fartman.testing

import com.kcverde.fartman.data.GameDao
import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.ui.SoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** In-memory stand-in for the Room DAO. */
class FakeGameDao : GameDao {
  private val records = MutableStateFlow<List<GameRecord>>(emptyList())

  /** Everything inserted so far, newest first, as the real query orders it. */
  val saved: List<GameRecord>
    get() = records.value

  override fun getAllHistory(): Flow<List<GameRecord>> = records

  override suspend fun insertRecord(record: GameRecord) {
    records.value = listOf(record) + records.value
  }

  override suspend fun deleteAllHistory() {
    records.value = emptyList()
  }
}

/** Silent [SoundPlayer] that remembers what it was asked to play. */
class RecordingSoundPlayer : SoundPlayer {
  override var isMuted: Boolean = false

  val played = mutableListOf<String>()
  var released = false
    private set

  override fun playCorrectBubble() {
    played += "correct"
  }

  override fun playIncorrectPuff() {
    played += "incorrect"
  }

  override fun playRoundStart() {
    played += "start"
  }

  override fun playVictoryDeflate() {
    played += "victory"
  }

  override fun playMegaFartExplosion() {
    played += "explosion"
  }

  override fun release() {
    released = true
  }
}

/** Swaps `Dispatchers.Main` for a test dispatcher so `viewModelScope` works. */
class MainDispatcherRule(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
  TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}
