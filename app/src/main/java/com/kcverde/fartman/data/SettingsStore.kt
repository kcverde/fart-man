package com.kcverde.fartman.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Preferences that outlive a single session.
 *
 * An interface so the ViewModel can be tested without a real DataStore; see
 * [InMemorySettingsStore].
 */
interface SettingsStore {
  val soundEnabled: Flow<Boolean>

  suspend fun setSoundEnabled(enabled: Boolean)
}

private val Context.preferences: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Backed by Jetpack DataStore, so muting the game survives a restart. */
class DataStoreSettings(context: Context) : SettingsStore {
  private val appContext = context.applicationContext

  override val soundEnabled: Flow<Boolean> =
    appContext.preferences.data.map { it[SOUND_ENABLED] ?: true }

  override suspend fun setSoundEnabled(enabled: Boolean) {
    appContext.preferences.edit { it[SOUND_ENABLED] = enabled }
  }

  private companion object {
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
  }
}

/** Non-persistent implementation for tests and previews. */
class InMemorySettingsStore(initialSoundEnabled: Boolean = true) : SettingsStore {
  private val state = MutableStateFlow(initialSoundEnabled)

  override val soundEnabled: Flow<Boolean> = state

  override suspend fun setSoundEnabled(enabled: Boolean) {
    state.value = enabled
  }
}
