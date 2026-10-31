package com.example.fin_app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

class MainViewModel : ViewModel() {

    // Начальный баланс (1500 Br)
    private var _totalBalance = MutableStateFlow(1500.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    // Список всех транзакций
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Список категорий (изначально берем базовые)
    private val _categories = MutableStateFlow(defaultCategories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // 1. Добавление расхода
    fun addExpense(amount: Double, categoryId: Int) {
        val newTransaction = Transaction(amount = amount, categoryId = categoryId, type = TransactionType.EXPENSE)
        _transactions.update { it + newTransaction }
        _totalBalance.update { it - amount }
    }

    // 2. Пополнение баланса (Сбережения/Доход)
    fun addIncome(amount: Double) {
        val newTransaction = Transaction(amount = amount, categoryId = -1, type = TransactionType.INCOME)
        _transactions.update { it + newTransaction }
        _totalBalance.update { it + amount }
    }

    // 3. Добавление новой категории (ТЕПЕРЬ ПРИНИМАЕТ ИКОНКУ)
    // 3. Добавление новой категории (принимает цвет и иконку)
    fun addCategory(name: String, limit: Double, icon: ImageVector, color: Color) {
        val newId = (_categories.value.maxOfOrNull { it.id } ?: 0) + 1

        val newCategory = Category(
            id = newId,
            name = name,
            icon = icon,
            color = color, // Используем выбранный цвет
            limit = limit
        )
        _categories.update { it + newCategory }
    }

    // 4. Подсчет потраченного по конкретной категории
    fun getSpentForCategory(categoryId: Int): Double {
        return _transactions.value
            .filter { it.categoryId == categoryId && it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    // 5. Удаление категории
    fun deleteCategory(categoryId: Int) {
        // Удаляем категорию из списка
        _categories.update { currentList ->
            currentList.filter { it.id != categoryId }
        }
        // Заодно удаляем все транзакции, которые были привязаны к этой категории
        _transactions.update { currentList ->
            currentList.filter { it.categoryId != categoryId }
        }
    }

    // 6. Обновление лимита категории
    fun updateCategoryLimit(categoryId: Int, newLimit: Double) {
        _categories.update { currentList ->
            currentList.map { category ->
                if (category.id == categoryId) {
                    category.copy(limit = newLimit) // Меняем только лимит
                } else {
                    category
                }
            }
        }
    }

    // Удаление транзакции (с возвратом денег на баланс)
    fun deleteTransaction(transaction: Transaction) {
        // Удаляем транзакцию из списка
        _transactions.update { currentList ->
            currentList.filter { it.id != transaction.id }
        }

        // Возвращаем деньги на баланс в зависимости от типа операции
        if (transaction.type == TransactionType.EXPENSE) {
            _totalBalance.update { it + transaction.amount } // Возвращаем потраченное
        } else if (transaction.type == TransactionType.INCOME) {
            _totalBalance.update { it - transaction.amount } // Забираем добавленное
        }
    }
}