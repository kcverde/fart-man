package com.kcverde.fartman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.kcverde.fartman.data.GameDatabase
import com.kcverde.fartman.data.GameRepository
import com.kcverde.fartman.ui.FartManViewModel
import com.kcverde.fartman.ui.FartManViewModelFactory
import com.kcverde.fartman.ui.FartSoundPlayer
import com.kcverde.fartman.ui.MainScreen
import com.kcverde.fartman.ui.theme.FartManTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize sounds
    FartSoundPlayer.init(this)

    // Initialize Room database & repository
    val database = GameDatabase.getDatabase(this)
    val repository = GameRepository(database.gameDao())
    
    // Construct the viewmodel factory
    val factory = FartManViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[FartManViewModel::class.java]

    setContent {
      FartManTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          MainScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

