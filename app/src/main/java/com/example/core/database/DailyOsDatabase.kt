package com.example.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        StudyCardEntity::class,
        ExpenseEntity::class,
        FocusSessionEntity::class,
        DailyReviewEntity::class,
        AiMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DailyOsDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun noteDao(): NoteDao
    abstract fun studyCardDao(): StudyCardDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailyReviewDao(): DailyReviewDao
    abstract fun aiMessageDao(): AiMessageDao
}
