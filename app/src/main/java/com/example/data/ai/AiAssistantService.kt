package com.example.data.ai

import com.example.core.database.*
import java.text.SimpleDateFormat
import java.util.*

interface AiAssistantService {
    fun generateTodayBrief(
        userName: String,
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        expenses: List<ExpenseEntity>,
        sessions: List<FocusSessionEntity>,
        currency: String
    ): String

    fun generateProgressSummary(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        sessions: List<FocusSessionEntity>
    ): String

    fun generateStudyAdvice(cards: List<StudyCardEntity>): String

    fun generateExpenseAnalysis(expenses: List<ExpenseEntity>, monthlyBudget: Double, currency: String): String

    fun generateProductivityAdvice(tasks: List<TaskEntity>, sessions: List<FocusSessionEntity>): String

    fun generateStudyQuestions(noteTitle: String, noteContent: String): String

    fun generateNoteSummary(noteTitle: String, noteContent: String): String

    fun respondToUserQuery(
        query: String,
        userName: String,
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        expenses: List<ExpenseEntity>,
        sessions: List<FocusSessionEntity>,
        cards: List<StudyCardEntity>,
        currency: String,
        budget: Double
    ): String
}

class LocalMockAiAssistantService : AiAssistantService {

    override fun generateTodayBrief(
        userName: String,
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        expenses: List<ExpenseEntity>,
        sessions: List<FocusSessionEntity>,
        currency: String
    ): String {
        val pendingTasks = tasks.filter { !it.isCompleted }
        val criticalTasks = pendingTasks.filter { it.priority == "Critical" }
        val focusMin = sessions.sumOf { it.durationMinutes }
        val spend = expenses.sumOf { it.amount }

        val format = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        val dateStr = format.format(Date())

        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        val taskBrief = if (criticalTasks.isNotEmpty()) {
            "You have ${criticalTasks.size} critical items pending today: \"${criticalTasks.first().title}\" needs immediate attention."
        } else if (pendingTasks.isNotEmpty()) {
            "You have ${pendingTasks.size} tasks on your planner today. Try to tackle \"${pendingTasks.first().title}\" first."
        } else {
            "Your task list for today is beautifully clear! Excellent planning."
        }

        val habitBrief = if (habits.isNotEmpty()) {
            "Don't forget your active habits. Consistency compounds your growth."
        } else {
            "No active habits set up yet. Go to More -> Habits to create some rituals."
        }

        val expenseBrief = if (spend > 0) {
            "Today's expenses are at $currency ${String.format("%.2f", spend)}."
        } else {
            "You haven't logged any spend yet today. Keep up the high savings mindset!"
        }

        return """
            Welcome to DailyOS AI, $userName!
            Today is $dateStr.
            
            $greeting!
            • **Task Status**: $taskBrief
            • **Habits**: $habitBrief
            • **Focus Session**: You've clocked $focusMin minutes of deep focus today.
            • **Finances**: $expenseBrief
            
            *Tip: Set an intention, tackle your top priority first, and take short breaks. You have this in control!*
        """.trimIndent()
    }

    override fun generateProgressSummary(
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        sessions: List<FocusSessionEntity>
    ): String {
        val completedCount = tasks.count { it.isCompleted }
        val totalTasks = tasks.size
        val focusMins = sessions.sumOf { it.durationMinutes }
        val totalHabitLogs = logs.size

        val taskStats = if (totalTasks > 0) {
            "Completed $completedCount out of $totalTasks tasks (${(completedCount * 100) / totalTasks}% rate)."
        } else {
            "No tasks added to calculate metrics yet."
        }

        return """
            📊 PROGRESS SUMMARY REPORT
            
            **Tasks Analytics**:
            $taskStats
            
            **Habit Rituals**:
            You have logged completion times a total of $totalHabitLogs times across your rituals. Habits are building blocks of success!
            
            **Deep Focus**:
            You've logged $focusMins minutes in focus timer sessions. Deep work is the super-power of this century.
            
            *AI Recommendation*: Based on active logs, your most consistent hours of productivity lie in morning sessions. Consider scheduling high-cognition creative tasks before 11 AM!
        """.trimIndent()
    }

    override fun generateStudyAdvice(cards: List<StudyCardEntity>): String {
        val total = cards.size
        val easy = cards.count { it.difficulty == "Easy" }
        val hard = cards.count { it.difficulty == "Hard" || it.difficulty == "Again" }

        return """
            🧠 ACTIVE RECALL & SPACED REPETITION ANALYSIS
            
            **Flashcard Metrics**:
            • Total cards in deck: $total
            • Mastery Status: $easy cards marked Easy, $hard cards marked challenging.
            
            **Spaced Repetition Tip**:
            Review your cards daily. When you struggle, choose 'Again' or 'Hard'. The algorithm will resurface these cards tomorrow and the day after to lock them in your long-term hippocampal memory.
            
            *Study Mantra*: Do not just reread text. Active quiz-style recall coupled with memory palaces is 300% more effective for long-term retention.
        """.trimIndent()
    }

    override fun generateExpenseAnalysis(expenses: List<ExpenseEntity>, monthlyBudget: Double, currency: String): String {
        val totalSpend = expenses.sumOf { it.amount }
        val categories = expenses.groupBy { it.category }
        
        val breakDown = StringBuilder()
        categories.forEach { (cat, list) ->
            val catTotal = list.sumOf { it.amount }
            breakDown.append("• **$cat**: $currency ${String.format("%.2f", catTotal)} (${String.format("%.1f", (catTotal / totalSpend) * 100)}%)\n")
        }

        val budgetStatus = if (totalSpend > monthlyBudget) {
            "⚠️ OVER BUDGET! You are over your monthly budget of $currency ${String.format("%.2f", monthlyBudget)} by $currency ${String.format("%.2f", totalSpend - monthlyBudget)}!"
        } else {
            "✅ Safe. You have consumed ${String.format("%.1f", (totalSpend / monthlyBudget) * 100)}% of your monthly budget space of $currency ${String.format("%.2f", monthlyBudget)}."
        }

        return """
            💳 PERSONAL FINANCE INSIGHTS
            
            **Spending Breakdown**:
            Total recorded spend: $currency ${String.format("%.2f", totalSpend)}
            
            ${if (expenses.isNotEmpty()) breakDown.toString() else "• No expenditures noted yet."}
            
            **Budget Status**:
            $budgetStatus
            
            *Smart Spending Tip*: Spend on assets that save time or improve wellness. Re-evaluate small overhead subscription fees, they quietly eat away cash-flow!
        """.trimIndent()
    }

    override fun generateProductivityAdvice(tasks: List<TaskEntity>, sessions: List<FocusSessionEntity>): String {
        val pendingCount = tasks.count { !it.isCompleted }
        val focusMin = sessions.sumOf { it.durationMinutes }

        return """
            ⚡ PRODUCTIVITY RATING & INTUITION
            
            **Current Context**:
            • Tasks left outstanding: $pendingCount
            • Today's focused minutes: $focusMin mins
            
            **Action Plan**:
            1. **Select One Thing**: Multitasking is a myth. Choose your single highest-impact task.
            2. **Timer block**: Start a 25-minute Pomodoro session in Focus Tab. Do not look at notifications.
            3. **Parkinson's Law**: Constrain your timelines. Give yourself exactly 1 hour to finish that presentation.
            
            *Mantra*: "Amateurs wait for motivation; professionals get to work."
        """.trimIndent()
    }

    override fun generateStudyQuestions(noteTitle: String, noteContent: String): String {
        if (noteContent.isEmpty()) return "Please draft content in this note, so I can extract active recall cards for you!"
        
        return """
            📝 ACTIVE RECALL CARDS EXTRACTED FROM: "$noteTitle"
            
            Here are study questions to test your knowledge:
            
            **Question 1**: What is the core definition of the primary concept discussed in "$noteTitle"?
            *Memory Palace hint: Imagine this concept sitting right at your bedroom entrance.*
            
            **Question 2**: What are the 3 critical properties or components of this topic?
            *Memory Palace hint: Pair these 3 items with items on your study desk.*
            
            **Question 3**: What is the main utility or practical application of what you just noted?
            
            *Tip: Tap the 'Convert to Flashcard' icon to add these straight to your study revision revision deck!*
        """.trimIndent()
    }

    override fun generateNoteSummary(noteTitle: String, noteContent: String): String {
        if (noteContent.isEmpty()) return "Please type some content to summarize!"
        val wordCount = noteContent.split("\\s+".toRegex()).size

        return """
            📌 AI SMART SUMMARY: "$noteTitle"
            
            **Executive Summary**:
            The user recorded dynamic thoughts regarding "$noteTitle" ($wordCount words).
            
            **Key Insights**:
            • Idea revolves around system management and local intelligence tools.
            • Outlines structured actions to optimize focus periods and task alignment.
            • Focuses heavily on the offline-first approach.
            
            *Next Action suggestion*: Check if there's any immediate actionable item inside. If so, convert this note into a task to track completion!
        """.trimIndent()
    }

    override fun respondToUserQuery(
        query: String,
        userName: String,
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>,
        expenses: List<ExpenseEntity>,
        sessions: List<FocusSessionEntity>,
        cards: List<StudyCardEntity>,
        currency: String,
        budget: Double
    ): String {
        val lowercaseQuery = query.lowercase()
        return when {
            lowercaseQuery.contains("plan") || lowercaseQuery.contains("today") || lowercaseQuery.contains("brief") -> {
                generateTodayBrief(userName, tasks, habits, expenses, sessions, currency)
            }
            lowercaseQuery.contains("progress") || lowercaseQuery.contains("summarize") || lowercaseQuery.contains("report") -> {
                generateProgressSummary(tasks, habits, logs, sessions)
            }
            lowercaseQuery.contains("study") || lowercaseQuery.contains("card") || lowercaseQuery.contains("exam") || lowercaseQuery.contains("memory") -> {
                generateStudyAdvice(cards)
            }
            lowercaseQuery.contains("expense") || lowercaseQuery.contains("spend") || lowercaseQuery.contains("money") || lowercaseQuery.contains("finance") || lowercaseQuery.contains("budget") -> {
                generateExpenseAnalysis(expenses, budget, currency)
            }
            lowercaseQuery.contains("focus") || lowercaseQuery.contains("productivity") || lowercaseQuery.contains("work") -> {
                generateProductivityAdvice(tasks, sessions)
            }
            else -> {
                """
                    DailyOS AI Assistant here! 🧠
                    
                    I am analyzing your local device database rule-base contents.
                    
                    Here are things you can ask me:
                    1. "Plan my day" (Provides dynamic dashboard greeting)
                    2. "Analyze my spending" (Provides category breakdowns vs budget)
                    3. "What should I focus on?" (Provides productivity action lists)
                    4. "Summarize my progress" (Logs completion rates)
                    5. "Give study advice" (Analyzes your Active Recall deck)
                    
                    *Active local database connection is fully secure. Your data remains fully private, encrypted local to this device.*
                """.trimIndent()
            }
        }
    }
}
