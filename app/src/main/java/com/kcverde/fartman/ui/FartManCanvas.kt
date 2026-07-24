package com.kcverde.fartman.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kcverde.fartman.R

@Composable
fun FartManCanvas(
    incorrectCount: Int,
    isExploded: Boolean,
    modifier: Modifier = Modifier
) {
    // Hover translation (floating up and down)
    val infiniteTransition = rememberInfiniteTransition(label = "fartman_idle")
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hover"
    )
    
    // Choose the correct image resource based on incorrect count
    // Stage 1 (index 0), Stage 2 (index 1), ... Stage 6 (index 5 or >= 6 exploded)
    val imageRes = when {
        isExploded || incorrectCount >= 6 -> R.drawable.fart_state_6
        incorrectCount == 5 -> R.drawable.fart_state_5
        incorrectCount == 4 -> R.drawable.fart_state_4
        incorrectCount == 3 -> R.drawable.fart_state_3
        incorrectCount == 2 -> R.drawable.fart_state_2
        else -> R.drawable.fart_state_1
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Fart Man stage ${incorrectCount + 1}",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .graphicsLayer(translationY = hoverY)
        )
    }
}
