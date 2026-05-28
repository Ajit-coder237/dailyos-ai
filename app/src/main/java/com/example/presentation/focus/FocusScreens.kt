package com.example.presentation.focus

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.VolumeMute
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.FocusSessionEntity
import com.example.presentation.MainViewModel
import com.example.presentation.components.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FocusScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit
) {
    val timeRemaining by viewModel.focusTimeRemaining.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val sessionTitle by viewModel.timerSessionTitle.collectAsState()
    val defaultDuration by viewModel.defaultFocusDuration.collectAsState()

    var customTitleInput by remember { mutableStateOf("") }
    var soundEnabled by remember { mutableStateOf(false) }
    var isFullScreenMode by remember { mutableStateOf(false) }

    val quotes = listOf(
        "Focus is a muscle. Do not let small notifications tear it down.",
        "Deep work is not a chore; it is an incredible state of artistic flow.",
        "The successful warrior is the average person, with laser-like focus.",
        "Your focus determines your reality. Stay on the single most critical line.",
        "Solitude and absolute silence are the playgrounds of innovation."
    )
    val randomQuote = remember { quotes.random() }

    val minutesStr = String.format("%02d", (timeRemaining / 1000) / 60)
    val secondsStr = String.format("%02d", (timeRemaining / 1000) % 60)

    if (isFullScreenMode) {
        // Minimalist immersive screen mode
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .clickable { isFullScreenMode = false },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = sessionTitle,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "$minutesStr:$secondsStr",
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-2).sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Tap anywhere to exit minimal focus screen",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Focus Command Station",
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Focus History")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Custom Focus Preset Title Input
            OutlinedTextField(
                value = customTitleInput,
                onValueChange = { 
                    customTitleInput = it
                    viewModel.setFocusSessionParams(if (it.isEmpty()) "Deep Work Session" else it, defaultDuration)
                },
                placeholder = { Text("What are you focusing on today?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("focus_title_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Presets row Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15 to "15m", 25 to "25m", 45 to "45m", 60 to "60m").forEach { (mins, label) ->
                    val isSelected = defaultDuration == mins
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable {
                                viewModel.setFocusSessionParams(
                                    if (customTitleInput.isEmpty()) "Deep Work Session" else customTitleInput,
                                    mins
                                )
                                viewModel.updateDefaultFocusDuration(mins)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Massive countdown timer clock
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$minutesStr:$secondsStr",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sessionTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Motivation Quotes block
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "\"$randomQuote\"",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Controls Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound toggle button (accessory)
                IconButton(onClick = { soundEnabled = !soundEnabled }) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Outlined.VolumeUp else Icons.Outlined.VolumeMute,
                        contentDescription = "Sound toggler",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Primary Start/Pause
                Box(modifier = Modifier.width(160.dp)) {
                    if (isTimerRunning) {
                        PrimaryButton(
                            text = "Pause Session",
                            onClick = { viewModel.pauseFocusTimer() }
                        )
                    } else {
                        PrimaryButton(
                            text = "Start Focus",
                            onClick = { viewModel.startFocusTimer() }
                        )
                    }
                }

                // Stop button
                IconButton(onClick = { viewModel.stopFocusTimer() }) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = ColorCritical
                    )
                }

                // Full screen portal button
                IconButton(onClick = { isFullScreenMode = true }) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Minimal Full Screen",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FocusHistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val history by viewModel.focusSessionHistory.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Focus Block Log",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (history.isEmpty()) {
                EmptyState(
                    title = "No Focus History",
                    subtitle = "Complete a focus block timer to record analytical results here.",
                    icon = Icons.Default.AccessTime
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(history) { session ->
                        FocusSessionRow(session = session)
                    }
                }
            }
        }
    }
}

@Composable
fun FocusSessionRow(session: FocusSessionEntity) {
    val dateText = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(session.completedAt))

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8E24AA).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFF8E24AA),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = session.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateText,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Text(
                text = "${session.durationMinutes} Mins",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
