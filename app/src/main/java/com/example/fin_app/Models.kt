package com.example.fin_app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType {
    EXPENSE, INCOME // Расход или Доход
}

data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val amount: Double,
    val categoryId: Int,
    val type: TransactionType = TransactionType.EXPENSE, // По умолчанию - расход
    val date: Long = System.currentTimeMillis()
)

data class Category(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val limit: Double
)

// Базовые категории
val defaultCategories = listOf(
    Category(1, "Продукты", Icons.Filled.Home, Color(0xFF4CAF50), 150.0),
    Category(2, "Здоровье", Icons.Filled.Favorite, Color(0xFFFF9800), 50.0),
    Category(3, "Транспорт", Icons.Filled.Place, Color(0xFF2196F3), 30.0),
    Category(4, "Развлечения", Icons.Filled.Star, Color(0xFF9C27B0), 70.0)
)