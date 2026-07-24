package com.kcverde.fartman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.kcverde.fartman.ui.FartManViewModel
import com.kcverde.fartman.ui.MainScreen
import com.kcverde.fartman.ui.theme.FartManTheme

class MainActivity : ComponentActivity() {

  private val viewModel: FartManViewModel by viewModels { FartManViewModel.factory(this) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent { FartManTheme { MainScreen(viewModel) } }
  }
}
