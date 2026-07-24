package com.kcverde.fartman.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kcverde.fartman.data.GameRecord
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: FartManViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Collect States
    val phase by viewModel.gamePhase.collectAsStateWithLifecycle()
    val creatorName by viewModel.creatorName.collectAsStateWithLifecycle()
    val guesserName by viewModel.guesserName.collectAsStateWithLifecycle()
    val secretWord by viewModel.secretWord.collectAsStateWithLifecycle()
    val hintText by viewModel.hintText.collectAsStateWithLifecycle()
    val guessedLetters by viewModel.guessedLetters.collectAsStateWithLifecycle()
    val incorrectCount by viewModel.incorrectCount.collectAsStateWithLifecycle()
    val history by viewModel.gameHistory.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()

    // Screen Shake variables
    val shakeOffset = remember { Animatable(0f) }
    
    // Listen for shake events from ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.shakeEvent.collectLatest {
            // Rumble shake sequence (left and right)
            for (i in 0..2) {
                shakeOffset.animateTo(25f, animationSpec = tween(50, easing = LinearEasing))
                shakeOffset.animateTo(-25f, animationSpec = tween(50, easing = LinearEasing))
            }
            shakeOffset.animateTo(0f, animationSpec = tween(50, easing = LinearEasing))
        }
    }

    // Immersive UI: Beautiful light tone transitions corresponding with the HTML's light-mode lavender vibe
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFEF7FF), // Primary Immersive background light mauve
            Color(0xFFEDF7ED)  // Gaseous soft mint light (signaling Fart Man's essence)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient),
        color = Color.Transparent
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    // Apply visual shake offset during wrong guesses!
                    .offset(x = shakeOffset.value.dp)
            ) {
                // Smooth crossfade transitions between different pass-and-play game screens
                AnimatedContent(
                    targetState = phase,
                    label = "phase_transitions",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    }
                ) { currentPhase ->
                    when (currentPhase) {
                        GamePhase.SETUP -> {
                            SetupScreen(
                                creatorName = creatorName,
                                guesserName = guesserName,
                                secretWord = secretWord,
                                hintText = hintText,
                                history = history,
                                onUpdateCreator = viewModel::updateCreatorName,
                                onUpdateGuesser = viewModel::updateGuesserName,
                                onUpdateSecretWord = viewModel::updateSecretWord,
                                onUpdateHint = viewModel::updateHintText,
                                onSubmit = viewModel::submitSetup,
                                onClearHistory = viewModel::clearHistory,
                                soundEnabled = soundEnabled,
                                onToggleSound = viewModel::toggleSound
                            )
                        }
                        GamePhase.PASSING -> {
                            PassingScreen(
                                guesserName = guesserName,
                                onStartGuessing = viewModel::startGuessing
                            )
                        }
                        GamePhase.ACTIVE -> {
                            ActiveGameScreen(
                                creatorName = creatorName,
                                guesserName = guesserName,
                                secretWord = secretWord,
                                hintText = hintText,
                                guessedLetters = guessedLetters,
                                incorrectCount = incorrectCount,
                                maxIncorrect = viewModel.maxIncorrect,
                                onGuess = viewModel::guessLetter,
                                onGiveUp = { viewModel.guessLetter('?') }, // Sinks the counter!
                                soundEnabled = soundEnabled,
                                onToggleSound = viewModel::toggleSound
                            )
                        }
                        GamePhase.VICTORY -> {
                            GameOverScreen(
                                isWin = true,
                                secretWord = secretWord,
                                creatorName = creatorName,
                                guesserName = guesserName,
                                incorrectCount = incorrectCount,
                                onPlayAgain = viewModel::resetToSetup,
                                onSwapRematch = viewModel::quickPlayAgain,
                                soundEnabled = soundEnabled,
                                onToggleSound = viewModel::toggleSound
                            )
                        }
                        GamePhase.DEFEAT -> {
                            GameOverScreen(
                                isWin = false,
                                secretWord = secretWord,
                                creatorName = creatorName,
                                guesserName = guesserName,
                                incorrectCount = incorrectCount,
                                onPlayAgain = viewModel::resetToSetup,
                                onSwapRematch = viewModel::quickPlayAgain,
                                soundEnabled = soundEnabled,
                                onToggleSound = viewModel::toggleSound
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 1. SETUP SCREEN ----------------------
@Composable
fun SetupScreen(
    creatorName: String,
    guesserName: String,
    secretWord: String,
    hintText: String,
    history: List<GameRecord>,
    onUpdateCreator: (String) -> Unit,
    onUpdateGuesser: (String) -> Unit,
    onUpdateSecretWord: (String) -> Unit,
    onUpdateHint: (String) -> Unit,
    onSubmit: () -> Unit,
    onClearHistory: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isHistoryExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header containing modern logo circle (HTML equivalent)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEADDFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        color = Color(0xFF21005D),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Fart Man",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF1D1B20)
                    )
                    Text(
                        text = "Pass & Play Gaseous Word Game",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF49454F)
                    )
                }
            }

            // Sound Toggle Icon Button
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3EDF7), CircleShape)
                    .shadow(1.dp, CircleShape)
                    .testTag("sound_mute_toggle")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.Default.Notifications else Icons.Default.Close,
                    contentDescription = "Toggle Game Sound",
                    tint = Color(0xFF6750A4),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CONFIG CARD WITH SUBTLE SHADOW & MATERIAL 3 STYLING
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3EDF7) // Material Lavender Container
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "1. Enter Players",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Creator name TF
                OutlinedTextField(
                    value = creatorName,
                    onValueChange = onUpdateCreator,
                    label = { Text("Word Creator Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1D1B20),
                        unfocusedTextColor = Color(0xFF1D1B20),
                        focusedContainerColor = Color(0xFFF7F2FA),
                        unfocusedContainerColor = Color(0xFFF7F2FA),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedLabelColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("creator_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF49454F)) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Guesser name TF
                OutlinedTextField(
                    value = guesserName,
                    onValueChange = onUpdateGuesser,
                    label = { Text("Guesser Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1D1B20),
                        unfocusedTextColor = Color(0xFF1D1B20),
                        focusedContainerColor = Color(0xFFF7F2FA),
                        unfocusedContainerColor = Color(0xFFF7F2FA),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedLabelColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guesser_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, tint = Color(0xFF49454F)) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "2. Set the Trap",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Secret word (Hideable!)
                OutlinedTextField(
                    value = secretWord,
                    onValueChange = { input ->
                        onUpdateSecretWord(input)
                    },
                    label = { Text("Secret Word (Letters Only)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1D1B20),
                        unfocusedTextColor = Color(0xFF1D1B20),
                        focusedContainerColor = Color(0xFFF7F2FA),
                        unfocusedContainerColor = Color(0xFFF7F2FA),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedLabelColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("secret_word_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Hint
                OutlinedTextField(
                    value = hintText,
                    onValueChange = onUpdateHint,
                    label = { Text("Optional Gas Hint") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1D1B20),
                        unfocusedTextColor = Color(0xFF1D1B20),
                        focusedContainerColor = Color(0xFFF7F2FA),
                        unfocusedContainerColor = Color(0xFFF7F2FA),
                        focusedBorderColor = Color(0xFF6750A4),
                        unfocusedBorderColor = Color(0xFFCAC4D0),
                        focusedLabelColor = Color(0xFF6750A4),
                        unfocusedLabelColor = Color(0xFF49454F)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hint_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF49454F)) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm word action button with primary purple accent
                Button(
                    onClick = onSubmit,
                    enabled = secretWord.isNotBlank() && secretWord.length >= 2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE8DEF8).copy(alpha = 0.5f),
                        disabledContentColor = Color(0xFF1D192B).copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("submit_setup_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (secretWord.isBlank()) "ENTER A WORD" else "LOCK WORD & PASS PHONE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // LEADERBOARD & STATS EXPANSION PANEL Styled to match Immersive theme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3EDF7)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isHistoryExpanded = !isHistoryExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF6750A4)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Historic Gas Records (Room DB)",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20),
                            fontSize = 15.sp
                        )
                    }
                    Icon(
                        imageVector = if (isHistoryExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand history list",
                        tint = Color(0xFF1D1B20)
                    )
                }

                if (isHistoryExpanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    val gamesPlayed = history.size
                    val totalWins = history.count { it.isWin }
                    val totalFarts = gamesPlayed - totalWins
                    val winRate = if (gamesPlayed > 0) (totalWins * 100 / gamesPlayed) else 0

                    // Scoreboard row layout (HTML style box grid)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF7FF), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PLAYS", color = Color(0xFF49454F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$gamesPlayed", color = Color(0xFF1D1B20), fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DEFLATED", color = Color(0xFF386A20), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$totalWins", color = Color(0xFF386A20), fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("FARTS", color = Color(0xFFB3261E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$totalFarts", color = Color(0xFFB3261E), fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WIN RATE", color = Color(0xFF6750A4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$winRate%", color = Color(0xFF6750A4), fontSize = 20.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (history.isEmpty()) {
                        Text(
                            text = "No gaseous logs yet! Run a game to persistent stats history.",
                            color = Color(0xFF49454F),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    } else {
                        // Scrolling columns of past stats styled on light background card rows
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())
                        ) {
                            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            
                            history.forEach { game ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${game.guesserName} vs ${game.creatorName}",
                                            color = Color(0xFF1D1B20),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Word: ${game.secretWord} • Date: ${dateFormat.format(Date(game.timestamp))}",
                                            color = Color(0xFF49454F),
                                            fontSize = 11.sp
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (game.isWin) Color(0xFFE2F1D8) else Color(0xFFFEECEB))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (game.isWin) "Deflated" else "Farted",
                                            color = if (game.isWin) Color(0xFF2A5907) else Color(0xFF8C2E24),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Clear Button
                        OutlinedButton(
                            onClick = onClearHistory,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB3261E)),
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Logs", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 2. PASSING PHONE TRANSITION SCREEN ----------------------
@Composable
fun PassingScreen(
    guesserName: String,
    onStartGuessing: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(26.dp)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large iconic phone handovers
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFEADDFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Pass icon",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(42.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "PASS PHONE!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF1D1B20)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Hand the phone over to:",
                    fontSize = 16.sp,
                    color = Color(0xFF49454F)
                )
                
                Text(
                    text = guesserName.uppercase(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6750A4),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFCAC4D0).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB3261E), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NO PEEKING AT THE SECRET WORD!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB3261E)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onStartGuessing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("start_guessing_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "I AM $guesserName, START GAME",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ---------------------- 3. ACTIVE GAMEPLAY SCREEN ----------------------
@Composable
fun ActiveGameScreen(
    creatorName: String,
    guesserName: String,
    secretWord: String,
    hintText: String,
    guessedLetters: Set<Char>,
    incorrectCount: Int,
    maxIncorrect: Int,
    onGuess: (Char) -> Unit,
    onGiveUp: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit
) {
    var showHint by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Player names modern banner (HTML headers equivalent)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "CREATOR", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                Text(text = creatorName, fontSize = 14.sp, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold)
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEADDFF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color(0xFF21005D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Small Mute Toggle Button next to Active badge
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(28.dp)
                        .background(Color(0xFFEADDFF), CircleShape)
                        .shadow(1.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.Notifications else Icons.Default.Close,
                        contentDescription = "Mute Sound Toggle",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "GUESSER", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Bold)
                Text(text = guesserName, fontSize = 14.sp, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bloat pressure tracker meter
        val progress = incorrectCount.toFloat() / maxIncorrect.toFloat()
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💨 Bloat Pressure Status",
                    fontSize = 12.sp,
                    color = Color(0xFF49454F)
                )
                Text(
                    text = "${maxIncorrect - incorrectCount} Mistakes Left Until Detonation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (incorrectCount >= 4) Color(0xFFB3261E) else Color(0xFF6750A4)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    incorrectCount >= 5 -> Color(0xFFB3261E) // red
                    incorrectCount >= 3 -> Color(0xFFE6B51E) // tensed yellow
                    else -> Color(0xFF6750A4)                // healthy purple
                },
                trackColor = Color(0xFFCAC4D0).copy(alpha = 0.4f)
            )
        }

        // --- CORE ANIMATED CANVAS FRAME ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Soft gas cloud glow under Fart Man (HTML inspired background blur effect)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x3B8CE85F), // gaseous soft green cloud
                                Color.Transparent
                            )
                        )
                    )
            )
            
            FartManCanvas(
                incorrectCount = incorrectCount,
                isExploded = false,
                modifier = Modifier
                    .size(240.dp)
                    .testTag("fart_man_canvas")
            )
        }

        // --- SECRET WORD BLANK LINES (HTML Underline Layout) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("secret_word_dashes"),
            horizontalArrangement = Arrangement.Center
        ) {
            secretWord.forEach { char ->
                val letterDiscovered = guessedLetters.contains(char)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = 30.dp, height = 44.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (letterDiscovered) char.toString() else " ",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    if (letterDiscovered) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
        }

        // Hint Reveal Drawer
        if (hintText.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .background(Color(0xFFF7F2FA), RoundedCornerShape(12.dp))
                    .clickable { showHint = !showHint }
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showHint) "HINT: $hintText" else "TAP TO REVEAL CREATOR HINT",
                        color = if (showHint) Color(0xFF1D1B20) else Color(0xFF49454F),
                        fontSize = 12.sp,
                        fontWeight = if (showHint) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // --- KEYBOARD SECTION ---
        val alphabets = ('A'..'Z').toList()
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 38.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(alphabets) { letter ->
                val hasBeenGuessed = guessedLetters.contains(letter)
                val isCorrect = hasBeenGuessed && secretWord.contains(letter)

                // Render customized keyboard keys inspired by HTML specifications
                Box(
                    modifier = Modifier
                        .height(37.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                !hasBeenGuessed -> Color(0xFFF7F2FA) // standard background
                                isCorrect -> Color(0xFFE2F1D8)      // Correct M3 Green
                                else -> Color(0xFFFEECEB)           // Incorrect M3 Red
                            }
                        )
                        .clickable(enabled = !hasBeenGuessed) {
                            onGuess(letter)
                        }
                        .testTag("key_${letter}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter.toString(),
                        color = when {
                            !hasBeenGuessed -> Color(0xFF1D1B20)
                            isCorrect -> Color(0xFF2A5907).copy(alpha = 0.6f)
                            else -> Color(0xFF8C2E24).copy(alpha = 0.6f)
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // HTML style bottom status footer bar
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF3EDF7), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF386A20), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$guesserName's turn to guess",
                    color = Color(0xFF49454F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Button(
                onClick = onGiveUp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.testTag("give_up_bt")
            ) {
                Text("Give Up", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------- 4. VICTORY / LOSS SCREENS ----------------------
@Composable
fun GameOverScreen(
    isWin: Boolean,
    secretWord: String,
    creatorName: String,
    guesserName: String,
    incorrectCount: Int,
    onPlayAgain: () -> Unit,
    onSwapRematch: () -> Unit,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Sound Toggle at Top Right inside the game-over card
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp)
                        .background(Color(0xFFFEF7FF), CircleShape)
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.Notifications else Icons.Default.Close,
                        contentDescription = "Mute Sound Toggle",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Large Custom Canvas to show victory state or explosion state!
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 12.dp)
                ) {
                    FartManCanvas(
                        incorrectCount = incorrectCount,
                        isExploded = !isWin,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title Banner using M3 colors corresponding to win/loss
                Text(
                    text = if (isWin) "🎉 SAFE DEFLATION!" else "💨 MEGA-FART EXPLOSION!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (isWin) Color(0xFF386A20) else Color(0xFFB3261E),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Detailed narrative text
                Text(
                    text = if (isWin) {
                        "Congratulations $guesserName! You guessed the word correctly with $incorrectCount mistake(s). Fart Man deflated safely back to normal size!"
                    } else {
                        "Oh no! $guesserName squeezed too hard, and Fart Man erupted into a giant gas cloud! $creatorName wins this battle!"
                    },
                    color = Color(0xFF49454F),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Show secret word details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF7F2FA), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "THE SECRET WORD WAS",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = secretWord,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6750A4),
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Choice Action Buttons
                // 1. Play again by swapping players! (Super relevant for pass and play)
                Button(
                    onClick = onSwapRematch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("rematch_swap_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SWAP ROLES & REMATCH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Play again (Keep same roles but change secret word)
                OutlinedButton(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6750A4)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("reset_setup_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "NEW GAME (SAME ROLES)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
}
