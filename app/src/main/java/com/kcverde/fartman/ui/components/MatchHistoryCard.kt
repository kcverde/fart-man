package com.kcverde.fartman.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kcverde.fartman.R
import com.kcverde.fartman.data.GameRecord
import com.kcverde.fartman.ui.theme.extendedColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Collapsible record of past rounds, backed by Room.
 *
 * Only the header toggles the panel. Previously the click handler sat on the
 * whole card, so pressing "Clear Logs" also collapsed it out from under you.
 */
@Composable
fun MatchHistoryCard(
  history: List<GameRecord>,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier.fillMaxWidth().animateContentSize(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.history_title),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Icon(
          imageVector =
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription =
            stringResource(if (expanded) R.string.history_collapse else R.string.history_expand),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }

      if (!expanded) return@Column

      Spacer(modifier = Modifier.height(16.dp))
      Scoreboard(history)
      Spacer(modifier = Modifier.height(16.dp))

      if (history.isEmpty()) {
        Text(
          text = stringResource(R.string.history_empty),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 13.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        )
        return@Column
      }

      val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
      ) {
        history.forEach { game -> HistoryRow(game = game, formattedDate = dateFormat.format(Date(game.timestamp))) }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedButton(
        onClick = onClearHistory,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.align(Alignment.End),
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(stringResource(R.string.history_clear), fontSize = 12.sp)
      }
    }
  }
}

@Composable
private fun Scoreboard(history: List<GameRecord>) {
  val played = history.size
  val wins = history.count { it.isWin }
  val winRate = if (played > 0) wins * 100 / played else 0

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
        .padding(12.dp),
    horizontalArrangement = Arrangement.SpaceAround,
  ) {
    Stat(
      label = stringResource(R.string.stat_plays),
      value = played.toString(),
      color = MaterialTheme.colorScheme.onSurface,
    )
    Stat(
      label = stringResource(R.string.stat_deflated),
      value = wins.toString(),
      color = MaterialTheme.extendedColors.success,
    )
    Stat(
      label = stringResource(R.string.stat_farts),
      value = (played - wins).toString(),
      color = MaterialTheme.colorScheme.error,
    )
    Stat(
      label = stringResource(R.string.stat_win_rate),
      value = stringResource(R.string.stat_win_rate_value, winRate),
      color = MaterialTheme.colorScheme.primary,
    )
  }
}

@Composable
private fun Stat(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    Text(text = value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black)
  }
}

@Composable
private fun HistoryRow(game: GameRecord, formattedDate: String) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(8.dp))
        .padding(10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f, fill = false)) {
      Text(
        text = stringResource(R.string.history_matchup, game.guesserName, game.creatorName),
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
      )
      Text(
        text = stringResource(R.string.history_detail, game.secretWord, formattedDate),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(6.dp))
          .background(
            if (game.isWin) MaterialTheme.extendedColors.successContainer
            else MaterialTheme.colorScheme.errorContainer
          )
          .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = stringResource(if (game.isWin) R.string.result_deflated else R.string.result_farted),
        color =
          if (game.isWin) MaterialTheme.extendedColors.onSuccessContainer
          else MaterialTheme.colorScheme.onErrorContainer,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
