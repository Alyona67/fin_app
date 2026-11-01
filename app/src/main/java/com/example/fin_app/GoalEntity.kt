package com.example.fin_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double, // Сколько нужно накопить
    val currentAmount: Double = 0.0, // Сколько уже накоплено
    val emoji: String, // Иконка цели (например, "✈️")
    val isCongratulated: Boolean = false
)