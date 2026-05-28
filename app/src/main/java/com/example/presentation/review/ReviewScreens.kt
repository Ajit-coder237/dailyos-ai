package com.example.presentation.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.MainViewModel
import com.example.presentation.components.*
import com.example.ui.theme.ColorCompleted
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyReviewScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit
) {
    var morningIntention by remember { mutableStateOf("") }
    var eveningGrateful1 by remember { mutableStateOf("") }
    var eveningGrateful2 by remember { mutableStateOf("") }
    var eveningGrateful3 by remember { mutableStateOf("") }
    var moodRating by remember { mutableIntStateOf(3) } // 1 to 5 scale
    var energyLevel by remember { mutableIntStateOf(3) } // 1 to 5 scale
    var notes by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("") }

    val formattedDate = remember { SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date()) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Daily Focus Journal",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Journal History")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Header
            Text(
                text = "Day Log: $formattedDate",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Set morning priorities and review your progress at evening sunset.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Morning Intention Plan
            Text("AM: Morning Intention Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = morningIntention,
                onValueChange = { morningIntention = it },
                placeholder = { Text("What positive metric or target are you aiming for today?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .testTag("morning_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Mood and Energy Parameters
            Text("PM: Performance Analysis", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Today's mood (1–5)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { num ->
                        val isSelected = moodRating == num
                        val bgColor = if (isSelected) Color(0xFFF57C00) else MaterialTheme.colorScheme.surface
                        val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable { moodRating = num },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$num", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = txtColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Command satisfied energy (1–5)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { num ->
                        val isSelected = energyLevel == num
                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable { energyLevel = num },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$num", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = txtColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Gratitude Logs
            Text("PM: Gratitude Ledger (Three things you are thankful for)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorCompleted)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = eveningGrateful1,
                onValueChange = { eveningGrateful1 = it },
                label = { Text("1. Thankful for...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grateful1_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = eveningGrateful2,
                onValueChange = { eveningGrateful2 = it },
                label = { Text("2. Thankful for...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grateful2_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = eveningGrateful3,
                onValueChange = { eveningGrateful3 = it },
                label = { Text("3. Thankful for...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grateful3_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // General Evening Reflections
            Text("General Evening Reflections", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Write down anything noteworthy that transpired today.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("review_notes_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            if (statusText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(statusText, color = ColorCompleted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Close Focus Day Log",
                onClick = {
                    val combinedGrateful = listOf(eveningGrateful1, eveningGrateful2, eveningGrateful3)
                        .filter { it.isNotEmpty() }
                        .joinToString(" • ")

                    // Call the distinct morning/evening planners in sequential order
                    viewModel.saveMorningPlan(
                        intention = morningIntention.trim().ifEmpty { "Plan active day" },
                        energyLevel = energyLevel,
                        priorities = morningIntention.trim()
                    )
                    
                    viewModel.saveEveningReview(
                        whatWentWell = notes.trim().ifEmpty { "Productive sunset schedule check completed" },
                        improvement = "Refined system priorities",
                        gratitude = combinedGrateful.ifEmpty { "Grateful for mindful progress blocks today." },
                        tomorrowFocus = "Execute agenda items",
                        mood = moodRating
                    )

                    statusText = "Completed day registry synchronized!"
                    morningIntention = ""
                    eveningGrateful1 = ""
                    eveningGrateful2 = ""
                    eveningGrateful3 = ""
                    notes = ""
                }
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun DailyReviewHistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val reviewLogs by viewModel.allDailyReviews.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Focus Journal Archives",
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
            if (reviewLogs.isEmpty()) {
                EmptyState(
                    title = "Archive is Empty",
                    subtitle = "Complete daily AM-PM reviews to build a beautiful local gratitude backlog.",
                    icon = Icons.Default.Inbox
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(reviewLogs) { log ->
                        val dateLabel = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(Date(log.createdAt))
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(dateLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFE0F2FE))
                                                .padding(vertical = 2.dp, horizontal = 6.dp)
                                        ) {
                                            Text("Mood: ${log.mood}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFF1F5F9))
                                                .padding(vertical = 2.dp, horizontal = 6.dp)
                                        ) {
                                            Text("Energy: ${log.energyLevel}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (log.intention.isNotEmpty()) {
                                    Text("AM Intention:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(log.intention, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (log.gratitude.isNotEmpty()) {
                                    Text("PM Gratitude Ledger:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorCompleted)
                                    Text(log.gratitude, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (log.whatWentWell.isNotEmpty()) {
                                    Text("Evening Journal:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    Text(log.whatWentWell, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
