package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // Low, Medium, High, Critical
    val category: String, // Personal, Study, Work, Health, Finance, Spiritual, Other
    val dueDate: Long,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val frequency: String, // Daily, Weekly
    val targetCount: Int = 1,
    val colorHex: Int = -0xc4a382, // Default modern color
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateString: String, // YYYY-MM-DD
    val count: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val tags: String, // Comma separated tags
    val isPinned: Boolean = false,
    val category: String, // Study, Idea, Meeting, Personal, Research, Spiritual, Finance
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_cards")
data class StudyCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val front: String,
    val back: String,
    val subject: String,
    val difficulty: String = "Good", // Again, Hard, Good, Easy
    val nextReviewDate: Long = System.currentTimeMillis(),
    val intervalDays: Int = 1,
    val memoryPalace: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String, // Food, Transport, Education, Health, Bills, Shopping, Family, Donation, Other
    val note: String,
    val date: Long = System.currentTimeMillis(),
    val currency: String = "NPR" // NPR, USD, INR, EUR
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_reviews")
data class DailyReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // YYYY-MM-DD
    val morningPlanSaved: Boolean = false,
    val eveningReviewSaved: Boolean = false,
    val intention: String = "",
    val energyLevel: Int = 3, // 1 to 5
    val prioritiesJson: String = "", // Comma-separated or JSON list
    val whatWentWell: String = "",
    val improvement: String = "",
    val gratitude: String = "",
    val tomorrowFocus: String = "",
    val mood: Int = 3, // 1 to 5
    val aiReflection: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val role: String, // user, assistant
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
