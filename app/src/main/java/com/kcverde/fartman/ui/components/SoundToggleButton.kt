package com.kcverde.fartman.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kcverde.fartman.R

const val SOUND_TOGGLE_TAG = "sound_toggle"

/**
 * Mute/unmute, styled to sit on whichever surface hosts it.
 *
 * Built from a Box rather than an `IconButton` because IconButton applies its
 * own 40dp size token after the caller's modifier, so a smaller circle would
 * still be painted at the token size and overlap its neighbours. Here the outer
 * node reserves the 48dp touch target and takes the click, while the inner box
 * draws the circle at exactly [buttonSize].
 *
 * The content description names the *action*, so TalkBack announces "Mute game
 * sound" while sound is on.
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
  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(CircleShape)
        .clickable(onClick = onToggle, role = Role.Button)
        .testTag(SOUND_TOGGLE_TAG),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier.size(buttonSize).background(containerColor, CircleShape),
      contentAlignment = Alignment.Center,
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
}
