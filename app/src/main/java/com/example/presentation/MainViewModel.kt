package com.example.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.*
import com.example.core.datastore.PreferencesManager
import com.example.core.notifications.NotificationHelper
import com.example.data.ai.AiAssistantService
import com.example.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val studyCardRepository: StudyCardRepository,
    private val expenseRepository: ExpenseRepository,
    private val focusRepository: FocusRepository,
    private val reviewRepository: ReviewRepository,
    private val aiMessageRepository: AiMessageRepository,
    private val aiAssistantService: AiAssistantService,
    private val preferencesManager: PreferencesManager,
    private val notificationHelper: NotificationHelper
) : ViewModel() {

    // Onboarding & Preferences
    val onboardingCompleted = preferencesManager.onboardingCompleted.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val userName = preferencesManager.userName.stateIn(viewModelScope, SharingStarted.Eagerly, "Innovator")
    val userTheme = preferencesManager.theme.stateIn(viewModelScope, SharingStarted.Eagerly, "System")
    val userCurrency = preferencesManager.currency.stateIn(viewModelScope, SharingStarted.Eagerly, "NPR")
    val reminderTime = preferencesManager.reminderTime.stateIn(viewModelScope, SharingStarted.Eagerly, "21:00")
    val defaultFocusDuration = preferencesManager.focusDuration.stateIn(viewModelScope, SharingStarted.Eagerly, 25)
    val monthlyBudget = preferencesManager.monthlyBudget.stateIn(viewModelScope, SharingStarted.Eagerly, 5000.0)
    val notificationsEnabled = preferencesManager.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // Reactive Data Flows
    val allTasks = taskRepository.allTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayPendingTasks = taskRepository.getPendingTasksForToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allHabits = habitRepository.allHabits.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allHabitLogs = habitRepository.allLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allNotes = noteRepository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allStudyCards = studyCardRepository.allCards.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dueStudyCards = studyCardRepository.getDueCards().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val allExpenses = expenseRepository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val todayExpenses = expenseRepository.getExpensesForToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val focusSessionHistory = focusRepository.allSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val focusMinutesToday = focusRepository.getFocusMinutesToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    val allDailyReviews = reviewRepository.allReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiMessages = aiMessageRepository.allMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _isAILoading = MutableStateFlow(false)
    val isAILoading = _isAILoading.asStateFlow()

    private val _todayBrief = MutableStateFlow("Pre-populating your life board...")
    val todayBrief = _todayBrief.asStateFlow()

    // Focus Timer Engine
    private val _focusTimeRemaining = MutableStateFlow(1500L) // 25 min default
    val focusTimeRemaining = _focusTimeRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _timerSessionTitle = MutableStateFlow("Deep Work Session")
    val timerSessionTitle = _timerSessionTitle.asStateFlow()

    private var timerJob: Job? = null
    private var originalDurationMinutes: Int = 25

    init {
        // Run Pre-population check
        checkAndPrepopulateSampleData()
        triggerTodayBriefUpdate()
    }

    private fun checkAndPrepopulateSampleData() {
        viewModelScope.launch(Dispatchers.IO) {
            // First observe if tasks are empty from repository directly (e.g. suspending check)
            allTasks.first { true } // Wait for flow collection once
            
            // Check if db pre-population is needed
            val needsPop = taskRepository.allTasks.first().isEmpty() && habitRepository.allHabits.first().isEmpty()
            
            if (needsPop) {
                // Prepopulate 5 Tasks
                val today = System.currentTimeMillis()
                val tomorrow = today + 24 * 60 * 60 * 1000L
                val threeDays = today + 3 * 24 * 60 * 60 * 1000L
                val fiveDays = today + 5 * 24 * 60 * 60 * 1000L

                val sampleTasks = listOf(
                    TaskEntity(title = "Review Organic Chem Nomenclature 🔥", description = "Test key compounds under functional groups list. Complete workbook 2 questions.", priority = "Critical", category = "Study", dueDate = today),
                    TaskEntity(title = "Write Weekly Board Update", description = "Draft core project metrics, update layout design specs list and check dev-server credentials.", priority = "High", category = "Work", dueDate = tomorrow),
                    TaskEntity(title = "Book Medical Health Checkup", description = "Regular dentist review and blood profile. Call city hospital clinic desk.", priority = "Medium", category = "Health", dueDate = threeDays),
                    TaskEntity(title = "Review Monthly Finances & Budget", description = "Cross-check food and high-interest entertainment expenses this quarter.", priority = "Low", category = "Finance", dueDate = fiveDays),
                    TaskEntity(title = "Daily Evening Mindful Check-in", description = "Clock 15-minutes focused deep breathing, review intentions and write simple gratitude notes.", priority = "Medium", category = "Spiritual", dueDate = today)
                )
                for (task in sampleTasks) taskRepository.insertTask(task)

                // Prepopulate 5 Habits
                val sampleHabits = listOf(
                    HabitEntity(name = "Study Deep Work", description = "Complete at least 1 focus block of undivided attention.", frequency = "Daily", targetCount = 2, colorHex = -0xd89006),
                    HabitEntity(name = "Exercise Regularly", description = "Quick functional run, pull-ups, or standard yoga sessions.", frequency = "Daily", targetCount = 1, colorHex = -0xc13d3d),
                    HabitEntity(name = "Meditation / Prayer", description = "Sitting quiet focus. Build clarity and breathing awareness.", frequency = "Daily", targetCount = 1, colorHex = -0x8e24aa),
                    HabitEntity(name = "Reading Deeply", description = "10 pages of high-cognition literature or non-fiction textbook.", frequency = "Daily", targetCount = 1, colorHex = -0x00897b),
                    HabitEntity(name = "Log All Expenses", description = "Save spendings directly to DailyOS within 10 minutes of buy.", frequency = "Daily", targetCount = 1, colorHex = -0xd32f2f)
                )
                for (habit in sampleHabits) {
                    val id = habitRepository.insertHabit(habit).toInt()
                    // Prepopulate past habit log
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    habitRepository.completeHabitForToday(id, sdf.format(Date(today - 24 * 60 * 60 * 1000L)))
                    habitRepository.completeHabitForToday(id, sdf.format(Date(today)))
                }

                // Prepopulate 5 Notes
                val sampleNotes = listOf(
                    NoteEntity(title = "Startup Strategy & Core Mission", content = "Focus on modern offline-first intelligence tools. Leverage direct SQLite processing to achieve instant premium sub-10ms response latency on mobile grids.", tags = "business,strategy", isPinned = true, category = "Idea"),
                    NoteEntity(title = "Active Recall Study Guide Notes", content = "1. Test yourself *before* reading the answers to stimulate memory neural pathways.\n2. Leverage visual cues and Memory Palaces to build rich emotional memories around abstract facts.", tags = "revision,study", isPinned = true, category = "Study"),
                    NoteEntity(title = "Team Sprint Standup Alignment", content = "Reviewing next release scope. Need to optimize local DB caching blocks. Check if backup system respects edge constraints perfectly on low-spec units.", tags = "work,sync", isPinned = false, category = "Meeting"),
                    NoteEntity(title = "Quiet Gratitude Ledger", content = "Unbelievably grateful for clean drinking water, access to high-speed internet, family health, and quiet study spaces. Keep the baseline high.", tags = "journal", isPinned = false, category = "Personal"),
                    NoteEntity(title = "Sermon Study Reflection Daily", content = "Focusing on patience, community alignment, and simple serving structures: 'The best way to locate your identity is to dissolve it in service of peers.'", tags = "spiritual,reflection", isPinned = false, category = "Spiritual")
                )
                for (note in sampleNotes) noteRepository.insertNote(note)

                // Prepopulate 5 Study Cards (Active Recall)
                val sampleCards = listOf(
                    StudyCardEntity(front = "What is Spaced Repetition?", back = "An evidence-based learning technique where information review is scheduled over double-spaced intervals (e.g., 1, 2, 5, 10 days) to match mental decay rates.", subject = "Cognition Science", memoryPalace = "Imagine a grand spiral staircase where step gaps double with height."),
                    StudyCardEntity(front = "Explain 'Active Recall' method.", back = "Forcing the brain to retrieve facts rather than passively re-reading text. Retrieving facts physically reconstructs neurons.", subject = "Learning Theory", memoryPalace = "A theater spotlight piercing dark shadows to find a wooden trunk."),
                    StudyCardEntity(front = "What does ACID stand for in Database systems?", back = "Atomicity, Consistency, Isolation, Durability. Standard for bulletproof storage.", subject = "Computer Science", memoryPalace = "A metallic science lab cabinet containing 4 glowing flasks of compound fluid."),
                    StudyCardEntity(front = "What is Keynesian Economics?", back = "Theory advocating active state spending stimulus during demand recessions to kickstart circulation.", subject = "Macro Economics", memoryPalace = "John Maynard Keynes handing paper wind-mills to crowds in rainy weather."),
                    StudyCardEntity(front = "Who formulated the General Relativity?", back = "Albert Einstein in 1915.", subject = "Relativistic Physics", memoryPalace = "A wall grandfather-clock slowly melting under gravity down a curved grid.")
                )
                for (card in sampleCards) studyCardRepository.insertCard(card)

                // Prepopulate 5 Expenses
                val sampleExpenses = listOf(
                    ExpenseEntity(amount = 120.0, category = "Food", note = "Special Matcha Latte & double-shot Espresso on main block", date = today),
                    ExpenseEntity(amount = 450.0, category = "Transport", note = "Weekly subway commute card reload", date = today),
                    ExpenseEntity(amount = 2400.0, category = "Education", note = "Advanced Memory Techniques textbook bundle", date = today),
                    ExpenseEntity(amount = 650.0, category = "Health", note = "Organic Vitamin D supplement bottle", date = today - 36 * 60 * 60 * 1000L),
                    ExpenseEntity(amount = 1800.0, category = "Bills", note = "High-speed broadband network subscription renewal", date = today - 48 * 60 * 60 * 1000L)
                )
                for (exp in sampleExpenses) expenseRepository.insertExpense(exp)

                // Prepopulate 2 Focus Sessions
                focusRepository.saveFocusSession("Organic Chemistry Revision", 45)
                focusRepository.saveFocusSession("Sprint Product Refinement", 25)

                // Prepopulate 1 Daily Review
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                reviewRepository.saveReview(
                    DailyReviewEntity(
                        dateString = sdf.format(Date()),
                        morningPlanSaved = true,
                        eveningReviewSaved = true,
                        intention = "Work deep blocks and track financial statements",
                        energyLevel = 4,
                        prioritiesJson = "Finish nomenclature, Read, Limit grocery spends",
                        whatWentWell = "Clocked over 70 minutes deep focus, avoided fast food spending completely",
                        improvement = "Sleep hygiene: limited blue light screen before bed",
                        gratitude = "Uncluttered study table and warm evening tea",
                        tomorrowFocus = "Tackle writing update draft of chemistry reports",
                        mood = 5,
                        aiReflection = "Excellent progress! You checked critical study tasks early first. Continue locking down focus sessions to protect creative flows. Keep budget buffers active.",
                        createdAt = today
                    )
                )

                // Default Intro message in AI chat
                aiMessageRepository.insertMessage(
                    AiMessageEntity(
                        role = "assistant",
                        content = "Welcome, $userName! DailyOS local rule-base engine activated.\n\nI can compose daily briefs, evaluate expense reports, summarize smart notes, or build custom quiz cards.\n\nClick any suggest prompt bubble below to try!"
                    )
                )

                triggerTodayBriefUpdate()
            }
        }
    }

    fun generateNoteSummary(title: String, content: String): String {
        return aiAssistantService.generateNoteSummary(title, content)
    }

    fun generateStudyQuestions(title: String, content: String): String {
        return aiAssistantService.generateStudyQuestions(title, content)
    }

    fun forcePrepopulateMockSampleDataNow() {
        viewModelScope.launch(Dispatchers.IO) {
            // Prepopulate 5 Tasks
            val today = System.currentTimeMillis()
            val tomorrow = today + 24 * 60 * 60 * 1000L
            val threeDays = today + 3 * 24 * 60 * 60 * 1000L
            val fiveDays = today + 5 * 24 * 60 * 60 * 1000L

            val sampleTasks = listOf(
                TaskEntity(title = "Review Organic Chem Nomenclature 🔥", description = "Test key compounds under functional groups list. Complete workbook 2 questions.", priority = "Critical", category = "Study", dueDate = today),
                TaskEntity(title = "Write Weekly Board Update", description = "Draft core project metrics, update layout design specs list and check dev-server credentials.", priority = "High", category = "Work", dueDate = tomorrow),
                TaskEntity(title = "Book Medical Health Checkup", description = "Regular dentist review and blood profile. Call city hospital clinic desk.", priority = "Medium", category = "Health", dueDate = threeDays),
                TaskEntity(title = "Review Monthly Finances & Budget", description = "Cross-check food and high-interest entertainment expenses this quarter.", priority = "Low", category = "Finance", dueDate = fiveDays),
                TaskEntity(title = "Daily Evening Mindful Check-in", description = "Clock 15-minutes focused deep breathing, review intentions and write simple gratitude notes.", priority = "Medium", category = "Spiritual", dueDate = today)
            )
            for (task in sampleTasks) taskRepository.insertTask(task)

            // Prepopulate Habits
            val sampleHabits = listOf(
                HabitEntity(name = "Study Deep Work", description = "Complete at least 1 focus block of undivided attention.", frequency = "Daily", targetCount = 2, colorHex = -0xd89006),
                HabitEntity(name = "Exercise Regularly", description = "Quick functional run, pull-ups, or standard yoga sessions.", frequency = "Daily", targetCount = 1, colorHex = -0xc13d3d)
            )
            for (habit in sampleHabits) {
                val id = habitRepository.insertHabit(habit).toInt()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                habitRepository.completeHabitForToday(id, sdf.format(Date(today - 24 * 60 * 60 * 1000L)))
                habitRepository.completeHabitForToday(id, sdf.format(Date(today)))
            }
            triggerTodayBriefUpdate()
        }
    }

    fun triggerTodayBriefUpdate() {
        viewModelScope.launch {
            // Give slight buffer to load DB
            delay(800)
            combine(
                allTasks,
                allHabits,
                todayExpenses,
                focusSessionHistory
            ) { tasks, habits, expenses, sessions ->
                aiAssistantService.generateTodayBrief(
                    userName = userName.value,
                    tasks = tasks,
                    habits = habits,
                    expenses = expenses,
                    sessions = sessions,
                    currency = userCurrency.value
                )
            }.collectLatest { brief ->
                _todayBrief.value = brief
            }
        }
    }

    // Settings actions
    fun updateUserName(name: String) = viewModelScope.launch { preferencesManager.setUserName(name) }
    fun updateUserTheme(theme: String) = viewModelScope.launch { preferencesManager.setTheme(theme) }
    fun updateUserCurrency(curr: String) = viewModelScope.launch { preferencesManager.setCurrency(curr) }
    fun updateReminderTime(time: String) = viewModelScope.launch { preferencesManager.setReminderTime(time) }
    fun updateDefaultFocusDuration(mins: Int) {
        viewModelScope.launch {
            preferencesManager.setFocusDuration(mins)
            if (!_isTimerRunning.value) {
                _focusTimeRemaining.value = mins * 60 * 1000L
            }
        }
    }
    fun updateMonthlyBudget(budget: Double) = viewModelScope.launch { preferencesManager.setMonthlyBudget(budget) }
    fun updateNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { preferencesManager.setNotificationsEnabled(enabled) }
    
    fun resetAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.resetAll()
            aiMessageRepository.clearChat()
            taskRepository.allTasks.first().forEach { taskRepository.deleteTask(it) }
            noteRepository.allNotes.first().forEach { noteRepository.deleteNote(it) }
            habitRepository.allHabits.first().forEach { habitRepository.deleteHabit(it) }
            studyCardRepository.allCards.first().forEach { studyCardRepository.deleteCard(it) }
            expenseRepository.allExpenses.first().forEach { expenseRepository.deleteExpense(it) }
            // Re-check populates
            checkAndPrepopulateSampleData()
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted(true)
        }
    }

    // Task Interactions
    fun addTask(title: String, description: String, priority: String, category: String, dueDate: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                title = title,
                description = description,
                priority = priority,
                category = category,
                dueDate = dueDate
            )
            taskRepository.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTask(task)
        }
    }

    fun toggleTaskCompleted(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.toggleTaskCompleted(id)
        }
    }

    // Habit Interactions
    fun addHabit(name: String, description: String, frequency: String, targetCount: Int, colorHex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val habit = HabitEntity(
                name = name,
                description = description,
                frequency = frequency,
                targetCount = targetCount,
                colorHex = colorHex
            )
            habitRepository.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            habitRepository.deleteHabit(habit)
        }
    }

    fun toggleHabitCompletedToday(habitId: Int, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            if (isCompleted) {
                habitRepository.completeHabitForToday(habitId, dateStr)
                if (notificationsEnabled.value) {
                    val habit = habitRepository.getHabitById(habitId)
                    if (habit != null) {
                        notificationHelper.showHabitReminder(habit.name)
                    }
                }
            } else {
                habitRepository.uncompleteHabitForToday(habitId, dateStr)
            }
        }
    }

    // Note Interactions
    fun addNote(title: String, content: String, tags: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                title = title,
                content = content,
                tags = tags,
                category = category
            )
            noteRepository.insertNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.deleteNote(note)
        }
    }

    fun togglePinNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.togglePinNote(id)
        }
    }

    fun convertNoteToTask(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                title = "Todo: " + note.title,
                description = note.content,
                priority = "Medium",
                category = "Personal",
                dueDate = System.currentTimeMillis() + 24 * 60 * 60 * 1000L // Due tomorrow
            )
            taskRepository.insertTask(task)
            noteRepository.insertNote(note.copy(tags = if (note.tags.isEmpty()) "todo" else note.tags + ",todo"))
        }
    }

    // Study Interactions
    fun addStudyCard(front: String, back: String, subject: String, memoryPalace: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val card = StudyCardEntity(
                front = front,
                back = back,
                subject = subject,
                memoryPalace = memoryPalace
            )
            studyCardRepository.insertCard(card)
        }
    }

    fun deleteStudyCard(card: StudyCardEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            studyCardRepository.deleteCard(card)
        }
    }

    fun reviewStudyCard(id: Int, rating: String) {
        viewModelScope.launch(Dispatchers.IO) {
            studyCardRepository.reviewCard(id, rating)
        }
    }

    // Expense Interactions
    fun addExpense(amount: Double, category: String, note: String, date: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val expense = ExpenseEntity(
                amount = amount,
                category = category,
                note = note,
                date = date,
                currency = userCurrency.value
            )
            expenseRepository.insertExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.deleteExpense(expense)
        }
    }

    // Focus Timer Operations
    fun setFocusSessionParams(title: String, durationMinutes: Int) {
        _timerSessionTitle.value = title
        originalDurationMinutes = durationMinutes
        _focusTimeRemaining.value = durationMinutes * 60 * 1000L
    }

    fun startFocusTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            while (_focusTimeRemaining.value > 0L && _isTimerRunning.value) {
                delay(1000)
                _focusTimeRemaining.value -= 1000L
            }
            if (_focusTimeRemaining.value <= 0L) {
                completeFocusSession()
            }
        }
    }

    fun pauseFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun stopFocusTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _focusTimeRemaining.value = originalDurationMinutes * 60 * 1000L
    }

    private fun completeFocusSession() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            focusRepository.saveFocusSession(_timerSessionTitle.value, originalDurationMinutes)
            if (notificationsEnabled.value) {
                notificationHelper.showFocusCompletedNotification(_timerSessionTitle.value)
            }
            // Reset to defaults
            _focusTimeRemaining.value = originalDurationMinutes * 60 * 1000L
        }
    }

    // Daily Review Entries
    fun saveMorningPlan(intention: String, energyLevel: Int, priorities: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val existing = reviewRepository.getReviewByDate(dateStr)

            val updated = existing?.copy(
                morningPlanSaved = true,
                intention = intention,
                energyLevel = energyLevel,
                prioritiesJson = priorities
            ) ?: DailyReviewEntity(
                dateString = dateStr,
                morningPlanSaved = true,
                intention = intention,
                energyLevel = energyLevel,
                prioritiesJson = priorities
            )
            reviewRepository.saveReview(updated)
            triggerTodayBriefUpdate()
        }
    }

    fun saveEveningReview(whatWentWell: String, improvement: String, gratitude: String, tomorrowFocus: String, mood: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val existing = reviewRepository.getReviewByDate(dateStr)

            // Compile stats for Local AI Reflection
            val spendDaily = expenseRepository.getExpensesForToday().first().sumOf { it.amount }
            val focusMints = focusRepository.getFocusMinutesToday().first() ?: 0
            val pendingT = taskRepository.getPendingTasksForToday().first().size

            val reflection = "Daily logs complete! You spent $spendDaily in the $dateStr tracker index. " +
                    "You locked in $focusMints deep work minutes, leaving you with $pendingT tasks outstanding. " +
                    "Focus tomorrow on: \"$tomorrowFocus\". Keep up the reflection model."

            val updated = existing?.copy(
                eveningReviewSaved = true,
                whatWentWell = whatWentWell,
                improvement = improvement,
                gratitude = gratitude,
                tomorrowFocus = tomorrowFocus,
                mood = mood,
                aiReflection = reflection
            ) ?: DailyReviewEntity(
                dateString = dateStr,
                eveningReviewSaved = true,
                whatWentWell = whatWentWell,
                improvement = improvement,
                gratitude = gratitude,
                tomorrowFocus = tomorrowFocus,
                mood = mood,
                aiReflection = reflection
            )
            reviewRepository.saveReview(updated)
            triggerTodayBriefUpdate()
        }
    }

    // AI Chat Assistant
    fun sendUserMessage(query: String) {
        if (query.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            // Save User message
            val userMsg = AiMessageEntity(role = "user", content = query.trim())
            aiMessageRepository.insertMessage(userMsg)

            // Trigger AI Typing Loader
            _isAILoading.value = true

            // Gather context
            val tasks = allTasks.value
            val habits = allHabits.value
            val logs = allHabitLogs.value
            val expenses = allExpenses.value
            val sessions = focusSessionHistory.value
            val cards = allStudyCards.value
            val curr = userCurrency.value
            val budgetVal = monthlyBudget.value
            val nameVal = userName.value

            delay(1200) // Realistic typing feel

            val replyText = aiAssistantService.respondToUserQuery(
                query = query,
                userName = nameVal,
                tasks = tasks,
                habits = habits,
                logs = logs,
                expenses = expenses,
                sessions = sessions,
                cards = cards,
                currency = curr,
                budget = budgetVal
            )

            val assistantMsg = AiMessageEntity(role = "assistant", content = replyText)
            aiMessageRepository.insertMessage(assistantMsg)
            _isAILoading.value = false
        }
    }

    fun submitPresetQuery(query: String) {
        sendUserMessage(query)
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            aiMessageRepository.clearChat()
            // Insert initial welcome
            aiMessageRepository.insertMessage(
                AiMessageEntity(
                    role = "assistant",
                    content = "Local rule-base AI cleaned. How can I help you plan your tasks, budgets, or study blocks today?"
                )
            )
        }
    }
}

class MainViewModelFactory(
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val noteRepository: NoteRepository,
    private val studyCardRepository: StudyCardRepository,
    private val expenseRepository: ExpenseRepository,
    private val focusRepository: FocusRepository,
    private val reviewRepository: ReviewRepository,
    private val aiMessageRepository: AiMessageRepository,
    private val aiAssistantService: AiAssistantService,
    private val preferencesManager: PreferencesManager,
    private val notificationHelper: NotificationHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                taskRepository, habitRepository, noteRepository, studyCardRepository,
                expenseRepository, focusRepository, reviewRepository, aiMessageRepository,
                aiAssistantService, preferencesManager, notificationHelper
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class modelClass")
    }
}
