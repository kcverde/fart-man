package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.ui.FartManViewModel
import com.example.ui.FartManViewModelFactory
import com.example.ui.FartSoundPlayer
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme

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
      MyApplicationTheme {
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

