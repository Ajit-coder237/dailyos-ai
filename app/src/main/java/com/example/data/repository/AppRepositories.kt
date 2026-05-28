package com.example.data.repository

import com.example.core.database.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getPendingTasksForToday(): Flow<List<TaskEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return taskDao.getPendingTasksForToday(calendar.timeInMillis)
    }

    suspend fun insertTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun toggleTaskCompleted(id: Int) {
        val task = taskDao.getTaskById(id)
        if (task != null) {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted, updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun getTaskById(id: Int): TaskEntity? = taskDao.getTaskById(id)
}

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllHabitLogs()

    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>> = habitDao.getLogsForDate(dateString)

    suspend fun getHabitById(id: Int): HabitEntity? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }

    suspend fun completeHabitForToday(habitId: Int, dateString: String) {
        val log = HabitLogEntity(habitId = habitId, dateString = dateString, count = 1)
        habitDao.insertHabitLog(log)
    }

    suspend fun uncompleteHabitForToday(habitId: Int, dateString: String) {
        habitDao.deleteHabitLogForDate(habitId, dateString)
    }
}

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun getNoteById(id: Int): NoteEntity? = noteDao.getNoteById(id)

    suspend fun togglePinNote(id: Int) {
        val note = noteDao.getNoteById(id)
        if (note != null) {
            noteDao.insertNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }
}

class StudyCardRepository(private val studyCardDao: StudyCardDao) {
    val allCards: Flow<List<StudyCardEntity>> = studyCardDao.getAllCards()

    fun getDueCards(): Flow<List<StudyCardEntity>> {
        return studyCardDao.getDueCards(System.currentTimeMillis())
    }

    suspend fun insertCard(card: StudyCardEntity) {
        studyCardDao.insertCard(card)
    }

    suspend fun deleteCard(card: StudyCardEntity) {
        studyCardDao.deleteCard(card)
    }

    suspend fun getCardById(id: Int): StudyCardEntity? = studyCardDao.getCardById(id)

    suspend fun reviewCard(id: Int, difficulty: String) {
        val card = studyCardDao.getCardById(id)
        if (card != null) {
            val days = when (difficulty) {
                "Again" -> 1
                "Hard" -> 2
                "Good" -> 5
                "Easy" -> 10
                else -> 5
            }
            val nextReview = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
            studyCardDao.insertCard(
                card.copy(
                    difficulty = difficulty,
                    intervalDays = days,
                    nextReviewDate = nextReview
                )
            )
        }
    }
}

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getExpensesForToday(): Flow<List<ExpenseEntity>> {
        val calendar = Calendar.getInstance()
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return expenseDao.getExpensesForDay(startOfDay, endOfDay)
    }

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}

class FocusRepository(private val focusSessionDao: FocusSessionDao) {
    val allSessions: Flow<List<FocusSessionEntity>> = focusSessionDao.getAllSessions()

    fun getFocusMinutesToday(): Flow<Int?> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return focusSessionDao.getFocusMinutesToday(calendar.timeInMillis)
    }

    suspend fun saveFocusSession(title: String, durationMinutes: Int) {
        val session = FocusSessionEntity(title = title, durationMinutes = durationMinutes)
        focusSessionDao.insertSession(session)
    }
}

class ReviewRepository(private val dailyReviewDao: DailyReviewDao) {
    val allReviews: Flow<List<DailyReviewEntity>> = dailyReviewDao.getAllReviews()

    suspend fun getReviewByDate(dateString: String): DailyReviewEntity? {
        return dailyReviewDao.getReviewByDate(dateString)
    }

    suspend fun saveReview(review: DailyReviewEntity) {
        dailyReviewDao.insertReview(review)
    }
}

class AiMessageRepository(private val aiMessageDao: AiMessageDao) {
    val allMessages: Flow<List<AiMessageEntity>> = aiMessageDao.getAllMessages()

    suspend fun insertMessage(message: AiMessageEntity) {
        aiMessageDao.insertMessage(message)
    }

    suspend fun clearChat() {
        aiMessageDao.clearChat()
    }
}
