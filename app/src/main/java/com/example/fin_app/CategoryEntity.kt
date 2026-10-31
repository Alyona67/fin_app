package com.example.fin_app

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String, // Название иконки
    val colorHex: String, // Цвет в HEX
    val limitAmount: Double
)