package com.kcverde.fartman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcverde.fartman.R

/**
 * Mute/unmute, styled to sit on whichever surface hosts it.
 *
 * The content description names the *action* rather than the current state, so
 * TalkBack announces "Mute game sound" while sound is on.
 */
@Composable
fun SoundToggleButton(
  soundEnabled: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
  tint: Color = MaterialTheme.colorScheme.primary,
  buttonSize: Dp = 40.dp,
  iconSize: Dp = 20.dp,
) {
  IconButton(
    onClick = onToggle,
    modifier =
      modifier.size(buttonSize).background(containerColor, CircleShape).testTag(SOUND_TOGGLE_TAG),
  ) {
    Icon(
      painter =
        painterResource(if (soundEnabled) R.drawable.ic_volume_up else R.drawable.ic_volume_off),
      contentDescription =
        stringResource(if (soundEnabled) R.string.sound_mute else R.string.sound_unmute),
      tint = tint,
      modifier = Modifier.size(iconSize),
    )
  }
}

const val SOUND_TOGGLE_TAG = "sound_toggle"
