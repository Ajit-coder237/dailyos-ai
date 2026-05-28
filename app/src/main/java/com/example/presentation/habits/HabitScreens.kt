package com.example.presentation.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.HabitEntity
import com.example.core.database.HabitLogEntity
import com.example.presentation.MainViewModel
import com.example.presentation.components.*
import com.example.ui.theme.ColorCompleted
import com.example.ui.theme.ColorCritical
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitListScreen(
    viewModel: MainViewModel,
    onNavigateToAddHabit: () -> Unit,
    onNavigateToHabitDetail: (Int) -> Unit
) {
    val habits by viewModel.allHabits.collectAsState()
    val logs by viewModel.allHabitLogs.collectAsState()

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("add_habit_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
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
                title = "Routine Habit Logs",
                navigationIcon = {
                    IconButton(onClick = { viewModel.triggerTodayBriefUpdate() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )

            // Weekly Tracker header view (Interactive calendar bar)
            WeeklyHabitCalendarHeader()

            if (habits.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "No Rituals Active",
                        subtitle = "Build discipline! Click the '+' button to log a daily habit like Meditation or Reading.",
                        icon = Icons.Default.Cached
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
                    items(habits, key = { it.id }) { habit ->
                        val habitLogsForHabit = logs.filter { it.habitId == habit.id }
                        val isDoneToday = logs.any { it.habitId == habit.id && it.dateString == todayStr }
                        val streak = calculateHabitStreak(logs, habit.id)

                        HabitRowItem(
                            habit = habit,
                            isDone = isDoneToday,
                            streakDays = streak,
                            onToggleComplete = { viewModel.toggleHabitCompletedToday(habit.id, !isDoneToday) },
                            onCardClick = { onNavigateToHabitDetail(habit.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitDetailScreen(
    viewModel: MainViewModel,
    habitId: Int,
    onBack: () -> Unit
) {
    val habits by viewModel.allHabits.collectAsState()
    val logs by viewModel.allHabitLogs.collectAsState()

    val habit = remember(habitId, habits) { habits.find { it.id == habitId } }
    val matchingLogs = remember(habitId, logs) { logs.filter { it.habitId == habitId }.sortedByDescending { it.dateString } }

    var showConfirmDelete by remember { mutableStateOf(false) }

    if (habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Habit not found")
        }
        return
    }

    if (showConfirmDelete) {
        ConfirmDeleteDialog(
            title = "Delete Routine",
            text = "Are you sure you want to permanently delete \"${habit.name}\"?",
            onConfirm = {
                viewModel.deleteHabit(habit)
                onBack()
            },
            onDismiss = { showConfirmDelete = false }
        )
    }

    val streak = calculateHabitStreak(logs, habitId)

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            AppTopBar(
                title = habit.name,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showConfirmDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete ritual", tint = ColorCritical)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stat Board Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Streak 🔥", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text("$streak Days", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(habit.colorHex).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Cached, contentDescription = null, tint = Color(habit.colorHex))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Description:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(habit.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 20.sp)
                }
            }

            SectionHeader(title = "Historical Completion Log Calendar")

            if (matchingLogs.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "No Completions Yet",
                        subtitle = "Toggle this habit completed on the listing tab to build your streak registry.",
                        icon = Icons.Default.EventAvailable
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(matchingLogs) { log ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(log.dateString, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Text("Completed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditHabitScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var targetCount by remember { mutableStateOf(1) }
    var selectedColorIndex by remember { mutableStateOf(0) }
    var errorText by remember { mutableStateOf("") }

    val colors = listOf(
        -0xd89006, // High Amber
        -0xc13d3d, // High Crimson
        -0x8e24aa, // Rich Purple
        -0x00897b, // Emerald Teal
        -0x1e88e5  // Classic Blue
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            AppTopBar(
                title = "New Ritual",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("habit_name_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Short Purpose / Motto *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .testTag("habit_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Frequency", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Daily", "Weekly").forEach { freq ->
                    val isSelected = frequency == freq
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { frequency = freq }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(freq, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Selection Palette
            Text("Assign Visual Theme Space", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colors.forEachIndexed { index, colorVal ->
                    val isSelected = selectedColorIndex == index
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .clickable { selectedColorIndex = index }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Launch Ritual",
                onClick = {
                    if (name.trim().isEmpty() || description.trim().isEmpty()) {
                        errorText = "Name and Motto are required fields."
                    } else {
                        viewModel.addHabit(
                            name = name.trim(),
                            description = description.trim(),
                            frequency = frequency,
                            targetCount = targetCount,
                            colorHex = colors[selectedColorIndex]
                        )
                        onBack()
                    }
                }
            )
        }
    }
}

@Composable
fun HabitRowItem(
    habit: HabitEntity,
    isDone: Boolean,
    streakDays: Int,
    onToggleComplete: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Done toggle button checkbox with custom ripple feedback
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) ColorCompleted.copy(alpha = 0.15f)
                            else Color(habit.colorHex).copy(alpha = 0.12f)
                        )
                        .clickable(onClick = onToggleComplete),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Complete checkin",
                        tint = if (isDone) ColorCompleted else Color(habit.colorHex),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = habit.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = habit.description,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Streak Fire Display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0F2FE)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "streak", tint = Color(0xFFF97316), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$streakDays d", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0369A1))
                }
            }
        }
    }
}

@Composable
fun WeeklyHabitCalendarHeader() {
    val sdf = SimpleDateFormat("dd", Locale.getDefault())
    val sdfDay = SimpleDateFormat("E", Locale.getDefault())
    
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek) // Start of week

    val weekDates = remember {
        (0..6).map {
            val dateVal = cal.time
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dateVal
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Weekly Ritual Progress Board", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                weekDates.forEach { date ->
                    val isToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date) == SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = sdfDay.format(date).take(1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = sdf.format(date),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

fun calculateHabitStreak(logs: List<HabitLogEntity>, habitId: Int): Int {
    val dates = logs.filter { it.habitId == habitId }.map { it.dateString }.toSet()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var streak = 0
    val cal = Calendar.getInstance()

    val todayStr = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStr = sdf.format(cal.time)

    if (todayStr !in dates && yesterdayStr !in dates) {
        return 0
    }

    cal.time = Date() // Reset
    while (true) {
        val checkStr = sdf.format(cal.time)
        if (checkStr in dates) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            if (checkStr == todayStr) {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                continue
            }
            break
        }
    }
    return streak
}
