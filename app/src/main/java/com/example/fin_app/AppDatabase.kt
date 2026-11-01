package com.example.fin_app

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, GoalEntity::class], // <-- Добавили GoalEntity
    version = 4, // <-- ВАЖНО: версия должна остаться 1, но если приложение вылетит, поменяй на 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun goalDao(): GoalDao // <-- Добавили GoalDao
}