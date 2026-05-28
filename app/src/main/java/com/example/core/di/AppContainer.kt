package com.example.core.di

import android.content.Context
import androidx.room.Room
import com.example.core.database.DailyOsDatabase
import com.example.core.datastore.PreferencesManager
import com.example.data.ai.AiAssistantService
import com.example.data.ai.LocalMockAiAssistantService
import com.example.data.repository.*

import com.example.core.notifications.NotificationHelper

interface AppContainer {
    val database: DailyOsDatabase
    val preferencesManager: PreferencesManager
    val taskRepository: TaskRepository
    val habitRepository: HabitRepository
    val noteRepository: NoteRepository
    val studyCardRepository: StudyCardRepository
    val expenseRepository: ExpenseRepository
    val focusRepository: FocusRepository
    val reviewRepository: ReviewRepository
    val aiMessageRepository: AiMessageRepository
    val aiAssistantService: AiAssistantService
    val notificationHelper: NotificationHelper
}

class AppContainerImpl(private val context: Context) : AppContainer {

    override val database: DailyOsDatabase by lazy {
        Room.databaseBuilder(
            context,
            DailyOsDatabase::class.java,
            "daily_os_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    override val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    override val habitRepository: HabitRepository by lazy {
        HabitRepository(database.habitDao())
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    override val studyCardRepository: StudyCardRepository by lazy {
        StudyCardRepository(database.studyCardDao())
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao())
    }

    override val focusRepository: FocusRepository by lazy {
        FocusRepository(database.focusSessionDao())
    }

    override val reviewRepository: ReviewRepository by lazy {
        ReviewRepository(database.dailyReviewDao())
    }

    override val aiMessageRepository: AiMessageRepository by lazy {
        AiMessageRepository(database.aiMessageDao())
    }

    override val aiAssistantService: AiAssistantService by lazy {
        LocalMockAiAssistantService()
    }

    override val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context)
    }
}
