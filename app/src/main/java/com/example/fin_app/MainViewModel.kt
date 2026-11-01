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
    private val goalDao = DatabaseProvider.getDatabase(application).goalDao()

    // 1. Транзакции
    val transactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Категории
    val categoriesDb: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoriesDb.map { dbList ->
        dbList.map { dbCat ->
            val icon = getIconByName(dbCat.iconName)
            val color = try { Color(android.graphics.Color.parseColor(dbCat.colorHex)) } catch (e: Exception) { Color.Gray }
            Category(dbCat.id, dbCat.name, icon, color, dbCat.limitAmount)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Баланс (как было)
    val totalBalance: StateFlow<Double> = transactions.map { list ->
        val income = list.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        1500.0 + income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1500.0)

    // 4. Валюта
    private val _currency = MutableStateFlow("Br")
    val currency: StateFlow<String> = _currency.asStateFlow()

    // 5. Добавление расхода
    fun addExpense(amount: Double, categoryId: Int) {
        viewModelScope.launch {
            dao.insertTransaction(TransactionEntity(amount = amount, categoryId = categoryId, type = "EXPENSE", date = System.currentTimeMillis()))
        }
    }

    // 6. Пополнение баланса
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

    // 8. Удаление категории
    fun deleteCategory(categoryId: Int) {
        viewModelScope.launch {
            val current = categoriesDb.value.find { it.id == categoryId }
            if (current != null) {
                dao.deleteCategory(current)
            }
        }
    }

    // 9. Удаление транзакции
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    // 10. Сброс данных
    fun clearAllData() {
        viewModelScope.launch {
            dao.clearAllTransactions()
        }
    }

    // 11. Изменение баланса
    fun setTotalBalance(newBalance: Double) {
        viewModelScope.launch {
            val currentBalance = totalBalance.value
            val difference = newBalance - currentBalance

            if (difference != 0.0) {
                dao.insertTransaction(
                    TransactionEntity(
                        amount = kotlin.math.abs(difference),
                        categoryId = -1,
                        type = if (difference > 0) "INCOME" else "EXPENSE",
                        date = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // 12. Обновление категории
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

    // --- ЦЕЛИ ---
    val goals: StateFlow<List<GoalEntity>> = goalDao.getAllGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addGoal(name: String, targetAmount: Double, emoji: String) {
        viewModelScope.launch {
            goalDao.insertGoal(GoalEntity(name = name, targetAmount = targetAmount, emoji = emoji))
        }
    }

    fun addMoneyToGoal(goal: GoalEntity, amount: Double) {
        viewModelScope.launch {
            val newCurrentAmount = goal.currentAmount + amount
            goalDao.updateGoal(goal.copy(currentAmount = newCurrentAmount))
            dao.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    categoryId = -3,
                    type = "EXPENSE",
                    date = System.currentTimeMillis(),
                    goalName = goal.name
                )
            )
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            val isCompleted = goal.currentAmount >= goal.targetAmount
            if (!isCompleted && goal.currentAmount > 0) {
                dao.insertTransaction(TransactionEntity(amount = goal.currentAmount, categoryId = -2, type = "INCOME", date = System.currentTimeMillis()))
            }
            goalDao.deleteGoal(goal)
        }
    }

    fun markGoalAsCongratulated(goal: GoalEntity) {
        viewModelScope.launch {
            goalDao.updateGoal(goal.copy(isCongratulated = true))
        }
    }

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

    // Экспорт в CSV
    // Экспорт в CSV (принимает списки из UI)
    fun exportToCsv(
        transactions: List<TransactionEntity>,
        categories: List<Category>
    ): java.io.File? {
        if (transactions.isEmpty()) return null

        val csvHeader = "Дата;Категория;Сумма (Br);Тип\n"
        val csvBody = transactions.joinToString("\n") { transaction ->
            val date = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(transaction.date))
            val categoryName = when {
                transaction.goalName != null -> transaction.goalName
                transaction.type == "INCOME" -> "Пополнение"
                else -> categories.find { it.id == transaction.categoryId }?.name ?: "Без категории"
            }
            val type = if (transaction.type == "INCOME") "Доход" else "Расход"
            "$date;$categoryName;${transaction.amount};$type"
        }

        // Добавляем BOM (Byte Order Mark) для корректного отображения кириллицы в Excel
        val bom = "\uFEFF"
        val csvContent = bom + csvHeader + csvBody

        val fileName = "fin_app_export_${System.currentTimeMillis()}.csv"
        val file = java.io.File(getApplication<Application>().cacheDir, fileName)

        return try {
            file.writeText(csvContent)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}