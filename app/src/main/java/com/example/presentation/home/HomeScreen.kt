package com.example.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.TaskEntity
import com.example.presentation.MainViewModel
import com.example.presentation.components.QuickActionCard
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.StatCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigate: (String) -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val userCurrency by viewModel.userCurrency.collectAsState()
    val todayPendingTasks by viewModel.todayPendingTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allHabits by viewModel.allHabits.collectAsState()
    val allHabitLogs by viewModel.allHabitLogs.collectAsState()
    val dueCards by viewModel.dueStudyCards.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    val focusMinutes by viewModel.focusMinutesToday.collectAsState()
    val dailyBriefText by viewModel.todayBrief.collectAsState()

    val scrollState = rememberScrollState()

    // Dynamically calculate simple Daily Progress Score
    val totalTasksToday = allTasks.count()
    val completedTasksToday = allTasks.count { it.isCompleted }
    val taskProductivityFactor = if (totalTasksToday > 0) (completedTasksToday * 40) / totalTasksToday else 25
    val focusFactor = minOf(40, (focusMinutes ?: 0) / 2) // Max 40 points
    val habitFactor = minOf(20, allHabitLogs.filter { 
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        it.dateString == sdf.format(Date())
    }.size * 5) // Max 20 points
    val totalDailyProgressScore = minOf(100, taskProductivityFactor + focusFactor + habitFactor)

    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val currentDateStr = dateFormat.format(Date())

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val timeOfDayGreeting = when (currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp) // Avoid overlap with bottom nav bar
    ) {
        // Top Header Row (No ugly colored background box, 100% sleek aesthetic)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$timeOfDayGreeting, $userName",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = currentDateStr,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            // User Avatar Circle representing Alex or AA
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color(0xFFD1E4FF))
                    .clickable { onNavigate("settings") }
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(2).uppercase(),
                    color = Color(0xFF004A77),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Daily Score & AI Brief (Gradient background layout)
        Card(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("ai_brief_card"),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF005FB0), Color(0xFF00315C))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left area: AI Brief info
                    Column(
                        modifier = Modifier.weight(1f).padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Text(
                                text = "AI BRIEF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (dailyBriefText.isNotBlank()) "\"$dailyBriefText\"" else "\"No pending briefs. You are good to plan your deep work metrics.\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 19.sp,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }

                    // Right area: Circular meter representation of Daily Score
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = totalDailyProgressScore / 100f,
                                modifier = Modifier.fillMaxSize(),
                                color = Color.White,
                                strokeWidth = 4.dp,
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                            Text(
                                text = "$totalDailyProgressScore",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "DAILY SCORE",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Quick Actions Grid (Horizontal Scrollable)
        SectionHeader(title = "Quick Command Actions")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { QuickActionCard("Add Task", Icons.Default.AddTask, MaterialTheme.colorScheme.primary) { onNavigate("add_edit_task") } }
            item { QuickActionCard("Add Note", Icons.Default.NoteAdd, MaterialTheme.colorScheme.secondary) { onNavigate("add_edit_note") } }
            item { QuickActionCard("Spend", Icons.Default.AccountBalanceWallet, MaterialTheme.colorScheme.tertiary) { onNavigate("add_expense") } }
            item { QuickActionCard("Focus Timer", Icons.Default.HourglassEmpty, Color(0xFF8E24AA)) { onNavigate("focus") } }
            item { QuickActionCard("Review Day", Icons.Default.MenuBook, Color(0xFFF57C00)) { onNavigate("daily_review") } }
            item { QuickActionCard("Add Habit", Icons.Default.Event, Color(0xFF10B981)) { onNavigate("add_edit_habit") } }
        }

        // Statistics Matrix Group (Grid-like)
        SectionHeader(title = "Health & Performance Ledger")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Agenda Tasks",
                value = "${todayPendingTasks.size} Left",
                icon = Icons.Default.CheckCircle,
                accentColor = Color(0xFF3B82F6)
            )
            val completedLogCount = allHabitLogs.filter {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                it.dateString == sdf.format(Date())
            }.size
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Active Habits",
                value = "$completedLogCount Completed",
                icon = Icons.Default.LocalFireDepartment,
                accentColor = Color(0xFF10B981)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            val totalExpenseToday = todayExpenses.sumOf { it.amount }
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Daily Expense",
                value = "$userCurrency ${String.format("%.1f", totalExpenseToday)}",
                icon = Icons.Default.Receipt,
                accentColor = Color(0xFF0D9488)
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Deep Focus Blocks",
                value = "${focusMinutes ?: 0} Min Today",
                icon = Icons.Default.Timer,
                accentColor = Color(0xFF8E24AA)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            StatCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                title = "Spaced Repetitive Memory Deck",
                value = "${dueCards.size} Cards Due To Review",
                icon = Icons.Default.Psychology,
                accentColor = Color(0xFFF97316)
            )
        }

        // Top Priorities Today section
        SectionHeader(title = "Agenda Critical Priorities Today")
        val criticalTasks = todayPendingTasks.filter { it.priority == "Critical" }
        val finalDisplayTasks = criticalTasks.ifEmpty { todayPendingTasks.take(2) }

        if (finalDisplayTasks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Nice job! You have cleared all important tasks for today.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                for (task in finalDisplayTasks) {
                    HomeTaskItem(task = task) {
                        viewModel.toggleTaskCompleted(task.id)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun HomeTaskItem(
    task: TaskEntity,
    onCheckedChange: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onCheckedChange() }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Category: ${task.category}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (task.priority == "Critical") ColorCritical.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Text(
                    text = task.priority,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.priority == "Critical") ColorCritical else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
