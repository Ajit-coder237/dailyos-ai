package com.example.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.TaskEntity
import com.example.presentation.MainViewModel
import com.example.presentation.components.*
import com.example.ui.theme.ColorCompleted
import com.example.ui.theme.ColorCritical
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskListScreen(
    viewModel: MainViewModel,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (Int) -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedTabFilter by remember { mutableStateOf("Pending") } // Pending, Upcoming, Completed

    val filteredTasks = allTasks.filter { task ->
        val matchesSearch = task.title.contains(searchQuery, ignoreCase = true) ||
                task.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == "All" || task.category == selectedCategoryFilter
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val matchesTab = when (selectedTabFilter) {
            "Pending" -> !task.isCompleted && task.dueDate <= todayEnd
            "Upcoming" -> !task.isCompleted && task.dueDate > todayEnd
            "Completed" -> task.isCompleted
            else -> true
        }

        matchesSearch && matchesCategory && matchesTab
    }

    val categories = listOf("All", "Personal", "Study", "Work", "Health", "Finance", "Spiritual", "Other")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp) // Offset above bottom navigation bar
                    .testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
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
                title = "Daily Task Plan",
                actions = {
                    IconButton(onClick = onNavigateToAddTask) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Quick add")
                    }
                }
            )

            // Search Bar
            Box(modifier = Modifier.padding(16.dp)) {
                CustomSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search tasks..."
                )
            }

            // Categories horizontal scroller
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        isSelected = selectedCategoryFilter == category,
                        onClick = { selectedCategoryFilter = category }
                    )
                }
            }

            // Tabs Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("Pending", "Upcoming", "Completed").forEach { tab ->
                    val isSelected = selectedTabFilter == tab
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    val txtColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { selectedTabFilter = tab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = txtColor
                        )
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    EmptyState(
                        title = "No $selectedTabFilter Tasks",
                        subtitle = "Select another tab filter or tap the '+' icon to log a new command item.",
                        icon = Icons.Default.TaskAlt
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
                    items(filteredTasks, key = { it.id }) { task ->
                        var showConfirmDelete by remember { mutableStateOf(false) }

                        if (showConfirmDelete) {
                            ConfirmDeleteDialog(
                                title = "Confirm Task Removal",
                                text = "Are you sure you want to delete task \"${task.title}\"?",
                                onConfirm = { viewModel.deleteTask(task) },
                                onDismiss = { showConfirmDelete = false }
                            )
                        }

                        TaskRow(
                            task = task,
                            onCompleteToggle = { viewModel.toggleTaskCompleted(task.id) },
                            onDeleteClick = { showConfirmDelete = true },
                            onEditClick = { onNavigateToEditTask(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditTaskScreen(
    viewModel: MainViewModel,
    taskId: Int? = null,
    onBack: () -> Unit
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val existingTask = remember(taskId, allTasks) {
        allTasks.find { it.id == taskId }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var category by remember { mutableStateOf("Personal") }
    var dueDateOffsetDays by remember { mutableStateOf(0) } // 0 = Today, 1 = Tomorrow, 3 = In 3 Days, 7 = In 1 Week
    var errorText by remember { mutableStateOf("") }

    LaunchedEffect(existingTask) {
        existingTask?.let {
            title = it.title
            description = it.description
            priority = it.priority
            category = it.category
            // Approximate date offset
            val diffMs = it.dueDate - System.currentTimeMillis()
            val diffDays = (diffMs / (24 * 60 * 60 * 1000L)).toInt()
            dueDateOffsetDays = when {
                diffDays <= 0 -> 0
                diffDays <= 1 -> 1
                diffDays <= 3 -> 3
                else -> 7
            }
        }
    }

    val priorities = listOf("Low", "Medium", "High", "Critical")
    val categories = listOf("Personal", "Study", "Work", "Health", "Finance", "Spiritual", "Other")

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            AppTopBar(
                title = if (taskId == null) "New Daily Task" else "Update Task",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Task Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("task_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Selector Grid
            Text("Set Priority Level", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                priorities.forEach { prio ->
                    val isSelected = priority == prio
                    val (bgColor, txtColor) = if (isSelected) {
                        when (prio) {
                            "Critical" -> ColorCritical to Color.White
                            "High" -> ColorCritical.copy(alpha = 0.8f) to Color.White
                            else -> MaterialTheme.colorScheme.primary to Color.White
                        }
                    } else {
                        MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { priority = prio }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(prio, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category tag list
            Text("Choose Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    CategoryChip(
                        category = cat,
                        isSelected = isSelected,
                        onClick = { category = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Due Date Offset Selector Row
            Text("Select Due Date", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                val dates = listOf("Today" to 0, "Tomorrow" to 1, "In 3 Days" to 3, "In 1 Week" to 7)
                dates.forEach { (label, offset) ->
                    val isSelected = dueDateOffsetDays == offset
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val txtColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgColor)
                            .clickable { dueDateOffsetDays = offset }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = txtColor)
                    }
                }
            }

            if (errorText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = if (taskId == null) "Submit New Task" else "Save Changes",
                onClick = {
                    if (title.trim().isEmpty()) {
                        errorText = "Please enter task title."
                    } else {
                        val calcDueDate = System.currentTimeMillis() + (dueDateOffsetDays * 24 * 60 * 60 * 1000L)
                        if (existingTask != null) {
                            viewModel.updateTask(
                                existingTask.copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = priority,
                                    category = category,
                                    dueDate = calcDueDate
                                )
                            )
                        } else {
                            viewModel.addTask(
                                title = title.trim(),
                                description = description.trim(),
                                priority = priority,
                                category = category,
                                dueDate = calcDueDate
                            )
                        }
                        onBack()
                    }
                }
            )
        }
    }
}

@Composable
fun TaskRow(
    task: TaskEntity,
    onCompleteToggle: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dueDateStr = sdf.format(Date(task.dueDate))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onCompleteToggle() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ColorCritical, modifier = Modifier.size(18.dp))
                }
            }

            if (task.description.isNotEmpty()) {
                Text(
                    text = task.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(start = 40.dp, top = 2.dp, bottom = 4.dp),
                    lineHeight = 16.sp,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PriorityChip(task.priority)
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 2.dp, horizontal = 6.dp)
                ) {
                    Text(task.category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(11.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Text(
                    text = dueDateStr,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
