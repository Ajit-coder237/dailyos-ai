package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.presentation.MainViewModel
import com.example.presentation.MainViewModelFactory
import com.example.presentation.assistant.AssistantScreen
import com.example.presentation.expenses.AddExpenseScreen
import com.example.presentation.expenses.ExpenseScreen
import com.example.presentation.focus.FocusHistoryScreen
import com.example.presentation.focus.FocusScreen
import com.example.presentation.habits.AddEditHabitScreen
import com.example.presentation.habits.HabitDetailScreen
import com.example.presentation.habits.HabitListScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.more.MoreScreen
import com.example.presentation.notes.AddEditNoteScreen
import com.example.presentation.notes.NoteDetailScreen
import com.example.presentation.notes.NotesScreen
import com.example.presentation.onboarding.OnboardingScreen
import com.example.presentation.review.DailyReviewHistoryScreen
import com.example.presentation.review.DailyReviewScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.study.AddEditStudyCardScreen
import com.example.presentation.study.ReviewStudyCardScreen
import com.example.presentation.study.StudyScreen
import com.example.presentation.tasks.AddEditTaskScreen
import com.example.presentation.tasks.TaskListScreen
import com.example.ui.theme.DailyOsTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Retrieve DI manual container components
            val appContainer = (application as DailyOsApplication).container
            
            val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = MainViewModelFactory(
                    appContainer.taskRepository,
                    appContainer.habitRepository,
                    appContainer.noteRepository,
                    appContainer.studyCardRepository,
                    appContainer.expenseRepository,
                    appContainer.focusRepository,
                    appContainer.reviewRepository,
                    appContainer.aiMessageRepository,
                    appContainer.aiAssistantService,
                    appContainer.preferencesManager,
                    appContainer.notificationHelper
                )
            )

            val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
            val themeMode by viewModel.userTheme.collectAsState()

            DailyOsTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Show dynamic lower bar only on main level destinations of the application scope
                val mainDestinations = listOf("home", "tasks", "notes", "focus", "more")
                val showBottomBar = onboardingCompleted && currentRoute in mainDestinations

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                val tabs = listOf(
                                    NavigationTab("home", "Home", Icons.Default.Home),
                                    NavigationTab("tasks", "Tasks", Icons.Default.TaskAlt),
                                    NavigationTab("notes", "Vault", Icons.Default.Description),
                                    NavigationTab("focus", "Deep Focus", Icons.Default.HourglassEmpty),
                                    NavigationTab("more", "Hub", Icons.Default.GridView)
                                )

                                tabs.forEach { tab ->
                                    val isSelected = currentRoute == tab.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        label = { Text(tab.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        ),
                                        onClick = {
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (onboardingCompleted) "home" else "onboarding",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Onboarding
                        composable("onboarding") {
                            OnboardingScreen(onFinish = {
                                viewModel.completeOnboarding()
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            })
                        }

                        // Main level tabs
                        composable("home") {
                            HomeScreen(viewModel = viewModel, onNavigate = { route -> navController.navigate(route) })
                        }
                        composable("tasks") {
                            TaskListScreen(
                                viewModel = viewModel,
                                onNavigateToAddTask = { navController.navigate("add_edit_task") },
                                onNavigateToEditTask = { taskId -> navController.navigate("add_edit_task?taskId=$taskId") }
                            )
                        }
                        composable("notes") {
                            NotesScreen(
                                viewModel = viewModel,
                                onNavigateToAddNote = { navController.navigate("add_edit_note") },
                                onNavigateToNoteDetail = { noteId -> navController.navigate("note_detail/$noteId") },
                                onNavigateToEditNote = { noteId -> navController.navigate("add_edit_note?noteId=$noteId") }
                            )
                        }
                        composable("focus") {
                            FocusScreen(
                                viewModel = viewModel,
                                onNavigateToHistory = { navController.navigate("focus_history") }
                            )
                        }
                        composable("more") {
                            MoreScreen(onNavigate = { route -> navController.navigate(route) })
                        }

                        // Tasks subroutes
                        composable("add_edit_task?taskId={taskId}") { backStackEntry ->
                            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
                            AddEditTaskScreen(viewModel = viewModel, taskId = taskId, onBack = { navController.popBackStack() })
                        }
                        composable("add_edit_task") {
                            AddEditTaskScreen(viewModel = viewModel, taskId = null, onBack = { navController.popBackStack() })
                        }

                        // Notes subroutes
                        composable("note_detail/{noteId}") { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
                            NoteDetailScreen(
                                viewModel = viewModel,
                                noteId = noteId,
                                onEditClick = { navController.navigate("add_edit_note?noteId=$noteId") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_edit_note?noteId={noteId}") { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
                            AddEditNoteScreen(viewModel = viewModel, noteId = noteId, onBack = { navController.popBackStack() })
                        }
                        composable("add_edit_note") {
                            AddEditNoteScreen(viewModel = viewModel, noteId = null, onBack = { navController.popBackStack() })
                        }

                        // Focus subroutes
                        composable("focus_history") {
                            FocusHistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // Habits subroutes
                        composable("habits") {
                            HabitListScreen(
                                viewModel = viewModel,
                                onNavigateToAddHabit = { navController.navigate("add_edit_habit") },
                                onNavigateToHabitDetail = { habitId -> navController.navigate("habit_detail/$habitId") }
                            )
                        }
                        composable("habit_detail/{habitId}") { backStackEntry ->
                            val habitId = backStackEntry.arguments?.getString("habitId")?.toIntOrNull() ?: 0
                            HabitDetailScreen(viewModel = viewModel, habitId = habitId, onBack = { navController.popBackStack() })
                        }
                        composable("add_edit_habit") {
                            AddEditHabitScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // Study subroutes
                        composable("study") {
                            StudyScreen(
                                viewModel = viewModel,
                                onNavigateToAddCard = { navController.navigate("add_edit_card") },
                                onNavigateToReviewCard = { navController.navigate("review_cards") }
                            )
                        }
                        composable("add_edit_card") {
                            AddEditStudyCardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable("review_cards") {
                            ReviewStudyCardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // Expenses subroutes
                        composable("expenses") {
                            ExpenseScreen(
                                viewModel = viewModel,
                                onNavigateToAddExpense = { navController.navigate("add_expense") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_expense") {
                            AddExpenseScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // Daily review subroutes
                        composable("daily_review") {
                            DailyReviewScreen(
                                viewModel = viewModel,
                                onNavigateToHistory = { navController.navigate("daily_review_history") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("daily_review_history") {
                            DailyReviewHistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // AI assistant subroutes
                        composable("ai_assistant") {
                            AssistantScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }

                        // Settings subroutes
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

data class NavigationTab(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
