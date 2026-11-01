package com.example.fin_app

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.getDatabase(application).appDao()

    // 1. Данные из базы
    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesDb: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Преобразуем CategoryEntity в наш обычный класс Category для UI
    val categories: StateFlow<List<Category>> = categoriesDb.map { dbList ->
        dbList.map { dbCat ->
            val icon = getIconByName(dbCat.iconName)
            val color = try { Color(android.graphics.Color.parseColor(dbCat.colorHex)) } catch (e: Exception) { Color.Gray }
            Category(dbCat.id, dbCat.name, icon, color, dbCat.limitAmount)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Переменная стартового баланса (должна быть объявлена ДО totalBalance)
    private val _initialBalance = MutableStateFlow(1500.0)
    val initialBalance: StateFlow<Double> = _initialBalance.asStateFlow()

    // 3. Расчет баланса
    val totalBalance: StateFlow<Double> = transactions.map { list ->
        val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        1500.0 + income - expense // 1500 - наш стартовый баланс
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1500.0)

    // 4. Валюта
    private val _currency = MutableStateFlow("Br")
    val currency: StateFlow<String> = _currency.asStateFlow()

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
    }

    // 5. Добавление расхода
    fun addExpense(amount: Double, categoryId: Int) {
        viewModelScope.launch {
            dao.insertTransaction(TransactionEntity(amount = amount, categoryId = categoryId, type = "EXPENSE", date = System.currentTimeMillis()))
        }
    }

    // 6. Пополнение баланса (Доход)
    fun addIncome(amount: Double) {
        viewModelScope.launch {
            dao.insertTransaction(TransactionEntity(amount = amount, categoryId = -1, type = "INCOME", date = System.currentTimeMillis()))
        }
    }

    // 7. Добавление категории
    fun addCategory(name: String, limit: Double, icon: ImageVector, color: Color) {
        viewModelScope.launch {
            val iconName = getIconName(icon)
            val colorHex = String.format("#%06X", (0xFFFFFF and color.toArgb()))
            dao.insertCategory(CategoryEntity(name = name, limitAmount = limit, iconName = iconName, colorHex = colorHex))
        }
    }

    // 8. Обновление лимита категории
    fun updateCategoryLimit(categoryId: Int, newLimit: Double) {
        viewModelScope.launch {
            val current = categoriesDb.value.find { it.id == categoryId }
            if (current != null) {
                dao.insertCategory(current.copy(limitAmount = newLimit))
            }
        }
    }

    // 9. Удаление категории
    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            val current = categoriesDb.value.find { it.id == categoryId }
            if (current != null) {
                dao.deleteCategory(current)
            }
        }
    }

    // 10. Удаление транзакции
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    // 11. Сброс всех данных
    fun clearAllData() {
        viewModelScope.launch {
            dao.clearAllTransactions()
        }
    }

    // 12. Изменение общего баланса
    // 12. Изменение общего баланса (через добавление корректирующей транзакции)
    fun setTotalBalance(newBalance: Double) {
        viewModelScope.launch {
            val currentBalance = totalBalance.value // Текущий баланс
            val difference = newBalance - currentBalance // Разница

            if (difference > 0) {
                // Если баланс увеличился, добавляем "виртуальный доход"
                dao.insertTransaction(
                    TransactionEntity(amount = difference, categoryId = -1, type = "INCOME", date = System.currentTimeMillis())
                )
            } else if (difference < 0) {
                // Если баланс уменьшился, добавляем "виртуальный расход"
                dao.insertTransaction(
                    TransactionEntity(amount = -difference, categoryId = -1, type = "EXPENSE", date = System.currentTimeMillis())
                )
            }
        }
    }

    // Вспомогательные функции для иконок
    private fun getIconByName(name: String): ImageVector {
        return when (name) {
            "Home" -> Icons.Filled.Home
            "Favorite" -> Icons.Filled.Favorite
            "Place" -> Icons.Filled.Place
            "Star" -> Icons.Filled.Star
            "ShoppingCart" -> Icons.Filled.ShoppingCart
            "DirectionsCar" -> Icons.Filled.DirectionsCar
            "Flight" -> Icons.Filled.Flight
            "Pets" -> Icons.Filled.Pets
            else -> Icons.Filled.Star
        }
    }

    private fun getIconName(icon: ImageVector): String {
        return when (icon) {
            Icons.Filled.Home -> "Home"
            Icons.Filled.Favorite -> "Favorite"
            Icons.Filled.Place -> "Place"
            Icons.Filled.Star -> "Star"
            Icons.Filled.ShoppingCart -> "ShoppingCart"
            Icons.Filled.DirectionsCar -> "DirectionsCar"
            Icons.Filled.Flight -> "Flight"
            Icons.Filled.Pets -> "Pets"
            else -> "Star"
        }
    }

    // 13. Полное обновление категории
    fun updateCategory(categoryId: Int, newName: String, newLimit: Double, newIcon: ImageVector, newColor: Color) {
        viewModelScope.launch {
            val current = categoriesDb.value.find { it.id == categoryId }
            if (current != null) {
                val iconName = getIconName(newIcon)
                val colorHex = String.format("#%06X", (0xFFFFFF and newColor.toArgb()))
                dao.insertCategory(
                    current.copy(
                        name = newName,
                        limitAmount = newLimit,
                        iconName = iconName,
                        colorHex = colorHex
                    )
                )
            }
        }
    }
}