package com.example.presentation.study

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.StudyCardEntity
import com.example.presentation.MainViewModel
import com.example.presentation.components.*
import com.example.ui.theme.ColorCompleted

@Composable
fun StudyScreen(
    viewModel: MainViewModel,
    onNavigateToAddCard: () -> Unit,
    onNavigateToReviewCard: () -> Unit
) {
    val allCards by viewModel.allStudyCards.collectAsState()
    val dueCards by viewModel.dueStudyCards.collectAsState()

    // Dynamic stats compilation
    val completedCount = allCards.count { it.difficulty == "Easy" || it.difficulty == "Good" }
    val totalCount = allCards.size
    val retentionEstimate = if (totalCount > 0) (completedCount * 100) / totalCount else 0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_card_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Card")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AppTopBar(
                title = "Active Recall Spaced Revision",
                navigationIcon = {
                    IconButton(onClick = { viewModel.triggerTodayBriefUpdate() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh brief")
                    }
                }
            )

            // Flashcards progress grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Revision Queue",
                    value = "${dueCards.size} Due Today",
                    icon = Icons.Default.Psychology,
                    accentColor = Color(0xFFF97316)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Estimated Retention",
                    value = "$retentionEstimate%",
                    icon = Icons.Default.TrendingUp,
                    accentColor = ColorCompleted
                )
            }

            // Central launch deck button CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (dueCards.isNotEmpty()) {
                            onNavigateToReviewCard()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("start_review_btn"),
                    enabled = dueCards.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF97316), contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (dueCards.isNotEmpty()) "Recall ${dueCards.size} Due Items Now" else "Deck fully memorized!",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            SectionHeader(title = "Your Spaced Memory Deck (${allCards.size})")

            if (allCards.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "Deck is Empty",
                        subtitle = "Add active recall cards using the '+' button to leverage the spacing effect.",
                        icon = Icons.Default.Inbox
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(allCards) { card ->
                        var showConfirmDelete by remember { mutableStateOf(false) }

                        if (showConfirmDelete) {
                            ConfirmDeleteDialog(
                                title = "Confirm Card Deletion",
                                text = "Do you want to delete this study card permanently?",
                                onConfirm = { viewModel.deleteStudyCard(card) },
                                onDismiss = { showConfirmDelete = false }
                            )
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = card.front,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Subject: ${card.subject} • Difficulty: ${card.difficulty}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }

                                IconButton(onClick = { showConfirmDelete = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete card", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditStudyCardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var memoryPalace by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            AppTopBar(
                title = "New Recall Card",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = front,
                onValueChange = { front = it },
                label = { Text("Question / Prompt *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_front_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = back,
                onValueChange = { back = it },
                label = { Text("Answer Explanation *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("card_back_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject (e.g. chemistry, logic) *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_subject_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = memoryPalace,
                onValueChange = { memoryPalace = it },
                label = { Text("Memory Palace / Visual Anchor Cue") },
                placeholder = { Text("e.g. Imagine this term glowing red on your kitchen shelf.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .testTag("card_palace_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Secure Deck Item",
                onClick = {
                    if (front.trim().isEmpty() || back.trim().isEmpty() || subject.trim().isEmpty()) {
                        errorText = "Question, Explanation, and Subject are required fields."
                    } else {
                        viewModel.addStudyCard(
                            front = front.trim(),
                            back = back.trim(),
                            subject = subject.trim(),
                            memoryPalace = memoryPalace.trim()
                        )
                        onBack()
                    }
                }
            )
        }
    }
}

@Composable
fun ReviewStudyCardScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val dueCards by viewModel.dueStudyCards.collectAsState()
    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    if (dueCards.isEmpty() || currentIndex >= dueCards.size) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(64.dp), tint = ColorCompleted)
                Spacer(modifier = Modifier.height(20.dp))
                Text("All cards reviewed!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Your memory retention for today is maximally locked in. Good job!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(30.dp))
                Box(modifier = Modifier.width(180.dp)) {
                    PrimaryButton(text = "Go Back", onClick = onBack)
                }
            }
        }
        return
    }

    val card = dueCards[currentIndex]

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Revision: ${currentIndex + 1} / ${dueCards.size}",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Exit review mode")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subject: ${card.subject}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontWeight = FontWeight.Black
            )

            // Dynamic card flipped view
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp)
                    .clickable { isRevealed = !isRevealed },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isRevealed) {
                        // FRONT QUESTION
                        Text(
                            text = card.front,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                        ) {
                            Text("Reveal Answer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        if (card.memoryPalace.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                text = "💡 Memory Cue: \"${card.memoryPalace}\"",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // BACK ANSWER
                        Text(
                            text = "ANSWER SUMMARY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = card.back,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Tap to view front question again",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Rating review action deck (Spacing algorithm feedback buttons)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Did your active recall succeed?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val spacingButtons = listOf(
                        "Again" to Color(0xFFEF4444), // Crimson
                        "Hard" to Color(0xFFF97316),  // Orange
                        "Good" to Color(0xFF22C55E),  // Green
                        "Easy" to Color(0xFF3B82F6)   // Blue
                    )

                    spacingButtons.forEach { (label, color) ->
                        Button(
                            onClick = {
                                viewModel.reviewStudyCard(card.id, label)
                                isRevealed = false
                                currentIndex++
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.15f), contentColor = color),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("score_$label")
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
