package com.example.fin_app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas

// ПАЛИТРА
val LavenderBg = Color(0xFFF4F0FA)
val GradientPurpleStart = Color(0xFF7B3AF1)
val GradientPurpleEnd = Color(0xFF9979E5)
val AccentPurple = Color(0xFF6B46C1)
val TextDark = Color(0xFF2D3748)
val TextGray = Color(0xFF718096)
val CardWhite = Color(0xFFFFFFFF)

// Цвета для темной темы
val DarkBg = Color(0xFF121212)
val DarkCard = Color(0xFF1E1E24)
val DarkCardBorder = Color(0xFF2A2A35) // Для обводки карточек
val DarkGradientStart = Color(0xA12B1556) // Приглушенный фиолетовый
val DarkGradientEnd = Color(0xA4381852)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = viewModel(),
    onAddIncomeClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val balance by viewModel.totalBalance.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currency by viewModel.currency.collectAsState()

    val spentMap = remember(transactions, categories) {
        categories.associate { cat ->
            cat.id to transactions.filter { it.categoryId == cat.id && it.type == "EXPENSE" }.sumOf { it.amount }
        }
    }

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    Box(modifier = Modifier.fillMaxSize().background(currentBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            item {
                // КАРТОЧКА БАЛАНСА
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp), ambientColor = GradientPurpleStart.copy(alpha = 0.3f), spotColor = Color.Black)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                colors = if (isDarkTheme) listOf(DarkGradientStart, DarkGradientEnd)
                                else listOf(GradientPurpleStart, GradientPurpleEnd)
                            )
                        )
                        .padding(horizontal = 24.dp, vertical = 23.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Мой баланс", color = Color.White.copy(alpha = 0.8f), fontSize = 17.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { onAddIncomeClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Пополнить", tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${String.format("%.2f", balance).replace('.', ',')} Br",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Категории",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = currentTextColor,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            items(categories) { category ->
                val spent = spentMap[category.id] ?: 0.0
                val progress = if (category.limit > 0) (spent / category.limit).toFloat() else 0f
                val isOverLimit = progress > 1f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp), ambientColor = Color.LightGray.copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(24.dp))
                        .background(currentCardColor)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(category.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(category.icon, contentDescription = null, tint = category.color)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = currentTextColor)
                                Text(
                                    text = "${String.format("%.0f", spent)} / ${category.limit} Br",
                                    color = if (isOverLimit) Color(0xFFE53E3E) else TextGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (isOverLimit) Color(0xFFE53E3E) else category.color,
                                trackColor = if (isDarkTheme) Color.DarkGray else LavenderBg,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetsScreen(
    viewModel: MainViewModel = viewModel(),
    onAddCategoryClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    Box(modifier = Modifier.fillMaxSize().background(currentBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Управление", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "У вас пока нет категорий. Добавьте первую!",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(categories) { category ->
                        val spent = transactions
                            .filter { it.categoryId == category.id && it.type == "EXPENSE" }
                            .sumOf { it.amount }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color.LightGray.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(currentCardColor)
                                .clickable { selectedCategory = category }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(category.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(category.name, fontWeight = FontWeight.Bold, color = currentTextColor)
                                        Text("Лимит: ${category.limit} Br", fontSize = 12.sp, color = TextGray)
                                    }
                                }
                                Text(
                                    text = "${String.format("%.0f", spent)} Br",
                                    fontWeight = FontWeight.Bold,
                                    color = category.color
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddCategoryClick,
            containerColor = Color(0xFF6843A9),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить категорию", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }

    if (selectedCategory != null) {
        CategoryActionSheet(
            category = selectedCategory!!,
            hasExpenses = transactions.any { it.categoryId == selectedCategory!!.id },
            onDismiss = { selectedCategory = null },
            onAddExpense = { amount ->
                viewModel.addExpense(amount, selectedCategory!!.id)
                selectedCategory = null
            },
            onDelete = {
                viewModel.deleteCategory(selectedCategory!!.id)
                selectedCategory = null
            },
            onUpdateCategory = { name, limit, icon, color -> // <-- ИЗМЕНЕНО
                viewModel.updateCategory(selectedCategory!!.id, name, limit, icon, color)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    var showBalanceDialog by remember { mutableStateOf(false) }
    var newBalanceText by remember { mutableStateOf("") }
    val currency by viewModel.currency.collectAsState()
    var showComingSoonDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    val tips = listOf(
        "Записывай траты сразу после покупки. Так ты не забудешь, куда ушли деньги.",
        "Правило 50/30/20: 50% на necessities, 30% на желания, 20% на сбережения.",
        "Перед покупкой дорогой вещи подожди 24 часа. Часто желание пропадает.",
        "Анализируй свои подписки. Возможно, ты платишь за то, чем не пользуешься.",
        "Ставь финансовые цели. Когда есть цель, копить гораздо интереснее.",
        "Не сравнивай свои траты с чужими. У каждого свой бюджет и свои потребности.",
        "Откладывай 10% от любого дохода сразу, как только его получил."
    )
    // Получаем номер дня в году (от 1 до 365)
    val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    // Выбираем совет, индекс которого зависит от дня
    val dailyTip = tips[dayOfYear % tips.size]

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(currentBg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Профиль",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = currentTextColor
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Переключатель темы
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(currentCardColor)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Темная тема", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = currentTextColor)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeChange(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = GradientPurpleStart)
            )
        }



        Spacer(modifier = Modifier.height(26.dp))
        Text("Совет дня", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
        Spacer(modifier = Modifier.height(12.dp))

        // Карточка с советом
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDarkTheme) DarkCard else Color(0xFFFFF3E0))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💡", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = dailyTip,
                fontSize = 14.sp,
                color = if (isDarkTheme) Color.White else TextDark,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(26.dp))
        Text("Инструменты", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
        Spacer(modifier = Modifier.height(12.dp))

        // 3. Мои цели
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(currentCardColor)
                .clickable { onNavigateToGoals() }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Мои цели", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = currentTextColor)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Аналитика (заглушка)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(currentCardColor)
                .clickable { onNavigateToAnalytics() }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Аналитика и графики", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = currentTextColor)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(currentCardColor)
                .clickable { showComingSoonDialog = true } // <-- ОТКРЫВАЕМ ОКНО
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Изменить общий баланс", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = currentTextColor)
            Icon(Icons.Filled.Edit, contentDescription = null, tint = GradientPurpleStart)
        }

        Spacer(modifier = Modifier.height(28.dp))


        // 5. Сброс данных
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Red.copy(alpha = 0.1f))
                .clickable { showResetDialog = true }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Сбросить все данные", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Red)
            Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red)
        }
    }

    // Диалог подтверждения сброса
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Вы уверены?") },
            text = { Text("Все ваши транзакции будут безвозвратно удалены. Категории останутся на месте.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllData()
                    showResetDialog = false
                }) {
                    Text("Сбросить", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = CardWhite
        )
    }

    if (showBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            title = { Text("Новый общий баланс") },
            text = {
                OutlinedTextField(
                    value = newBalanceText,
                    onValueChange = { newBalanceText = it },
                    label = { Text("Сумма") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)

                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = newBalanceText.toDoubleOrNull()
                    if (amount != null) {
                        viewModel.setTotalBalance(amount)
                    }
                    showBalanceDialog = false
                }) {
                    Text("Сохранить", color = GradientPurpleStart, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBalanceDialog = false }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = CardWhite
        )
    }

    // Окно "Скоро здесь можно будет менять баланс"
    if (showComingSoonDialog) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = false },
            title = { Text("В разработке") },
            text = { Text("Скоро здесь можно будет менять общий баланс. А пока используйте кнопку «Пополнить» на главном экране или добавьте трату.") },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = false }) {
                    Text("Понятно", color = GradientPurpleStart, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardWhite
        )
    }
}

@Composable
fun HistoryScreen(
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean = false
) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    // Фильтруем и сортируем (с поддержкой поиска по целям)
    val filteredTransactions = transactions
        .filter { transaction ->
            if (searchQuery.isBlank()) return@filter true

            val searchText = when {
                transaction.goalName != null -> transaction.goalName
                transaction.type == "INCOME" -> "Пополнение"
                else -> categories.find { it.id == transaction.categoryId }?.name ?: ""
            }

            searchText.contains(searchQuery, ignoreCase = true)
        }
        .sortedByDescending { it.date }

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    Box(modifier = Modifier.fillMaxSize().background(currentBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("История", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск по категориям и целям...", color = TextGray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск", tint = TextGray) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GradientPurpleStart,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = currentCardColor,
                    unfocusedContainerColor = currentCardColor,
                    focusedTextColor = currentTextColor,
                    unfocusedTextColor = currentTextColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isBlank()) "У вас пока нет операций" else "Ничего не найдено",
                        color = TextGray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredTransactions) { transaction ->
                        val category = categories.find { it.id == transaction.categoryId }
                        val title = when {
                            transaction.goalName != null -> transaction.goalName
                            transaction.type == "INCOME" -> "Пополнение"
                            else -> category?.name ?: "Без категории"
                        }
                        val icon = if (transaction.type == "INCOME") Icons.Filled.Add else (category?.icon ?: Icons.Filled.Star)
                        val color = if (transaction.type == "INCOME") Color(0xFF38A169) else (category?.color ?: TextGray)
                        val amountText = if (transaction.type == "INCOME") "+${String.format("%.2f", transaction.amount)} Br" else "-${String.format("%.2f", transaction.amount)} Br"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = Color.LightGray.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(currentCardColor)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = color)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(title, fontWeight = FontWeight.Bold, color = currentTextColor)
                                        Text(
                                            text = formatTransactionDate(transaction.date),
                                            fontSize = 12.sp,
                                            color = TextGray
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = amountText,
                                        fontWeight = FontWeight.Bold,
                                        color = if (transaction.type == "INCOME") Color(0xFF38A169) else currentTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = { transactionToDelete = transaction },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Удалить",
                                            tint = Color.Red.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Окно подтверждения удаления
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Удалить операцию?") },
            text = { Text("Вы уверены, что хотите удалить эту операцию? Сумма вернется на баланс.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null
                    }
                ) {
                    Text("Удалить", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = CardWhite,
            titleContentColor = TextDark,
            textContentColor = TextGray
        )
    }
}

// ОКНА
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (Double, Int) -> Unit,
    categories: List<Category>
) {
    var amountText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: 1) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Новый расход", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = amountText, onValueChange = { amountText = it },
                label = { Text("Сумма (Br)") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple, unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = TextDark, unfocusedTextColor = TextDark,
                    focusedLabelColor = AccentPurple, unfocusedLabelColor = TextGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Выбери категорию", fontSize = 16.sp, color = TextDark, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(categories) { category ->
                    val isSelected = category.id == selectedCategoryId
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategoryId = category.id }
                            .padding(8.dp)
                            .background(if (isSelected) category.color.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Icon(category.icon, contentDescription = null, tint = category.color)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(category.name, fontSize = 12.sp, color = if (isSelected) category.color else TextGray, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount != null && amount > 0) onSave(amount, selectedCategoryId)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Сохранить", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeSheet(onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amountText by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Пополнить баланс", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = amountText, onValueChange = { amountText = it },
                label = { Text("Сумма дохода (Br)") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple, unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = TextDark, unfocusedTextColor = TextDark,
                    focusedLabelColor = AccentPurple, unfocusedLabelColor = TextGray
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { amountText.toDoubleOrNull()?.let { if (it > 0) onSave(it) }; onDismiss() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Пополнить", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(onDismiss: () -> Unit, onSave: (String, Double, ImageVector, Color) -> Unit) {
    var nameText by remember { mutableStateOf("") }
    var limitText by remember { mutableStateOf("") }

    val availableIcons = listOf(
        Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Place, Icons.Filled.Star,
        Icons.Filled.ShoppingCart, Icons.Filled.DirectionsCar, Icons.Filled.Flight, Icons.Filled.Pets
    )

    val availableColors = listOf(
        Color(0xFFE53E3E), Color(0xFFDD6B20), Color(0xFFD69E2E), Color(0xFF38A169),
        Color(0xFF3182CE), Color(0xFF805AD5), Color(0xFFD53F8C), Color(0xFF319795),
        Color(0xFF0B2F75), Color(0xFF750DBE)
    )

    var selectedIcon by remember { mutableStateOf(availableIcons.first()) }
    var selectedColor by remember { mutableStateOf(availableColors.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Новая категория", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nameText, onValueChange = { nameText = it },
                label = { Text("Название") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple, unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = TextDark, unfocusedTextColor = TextDark,
                    focusedLabelColor = AccentPurple, unfocusedLabelColor = TextGray
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = limitText, onValueChange = { limitText = it },
                label = { Text("Бюджет на месяц (Br)") },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPurple, unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = TextDark, unfocusedTextColor = TextDark,
                    focusedLabelColor = AccentPurple, unfocusedLabelColor = TextGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Выбери цвет", fontSize = 16.sp, color = TextDark, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(availableColors) { color ->
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(if (isSelected) 3.dp else 0.dp, if (isSelected) TextDark else Color.Transparent, CircleShape)
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Выбери иконку", fontSize = 16.sp, color = TextDark, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                items(availableIcons) { icon ->
                    val isSelected = icon == selectedIcon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) selectedColor.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
                            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) selectedColor else Color.Transparent, CircleShape)
                            .clickable { selectedIcon = icon },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = if (isSelected) selectedColor else TextGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (nameText.isNotBlank() && limit != null && limit > 0) {
                        onSave(nameText, limit, selectedIcon, selectedColor)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("Создать", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryActionSheet(
    category: Category,
    hasExpenses: Boolean,
    onDismiss: () -> Unit,
    onAddExpense: (Double) -> Unit,
    onDelete: () -> Unit,
    onUpdateCategory: (String, Double, ImageVector, Color) -> Unit // <-- НОВЫЙ КОЛЛБЭК
) {
    var amountText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) } // <-- Режим редактирования

    // Поля для редактирования
    var editName by remember { mutableStateOf(category.name) }
    var editLimit by remember { mutableStateOf(category.limit.toString()) }
    var editIcon by remember { mutableStateOf(category.icon) }
    var editColor by remember { mutableStateOf(category.color) }

    val availableIcons = listOf(
        Icons.Filled.Home, Icons.Filled.Favorite, Icons.Filled.Place, Icons.Filled.Star,
        Icons.Filled.ShoppingCart, Icons.Filled.DirectionsCar, Icons.Filled.Flight, Icons.Filled.Pets
    )
    val availableColors = listOf(
        Color(0xFFE53E3E), Color(0xFFDD6B20), Color(0xFFD69E2E), Color(0xFF38A169),
        Color(0xFF3182CE), Color(0xFF805AD5), Color(0xFFD53F8C), Color(0xFF319795),
        Color(0xFF0B2F75), Color(0xFF750DBE)
    )

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ЗАГОЛОВОК
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(category.icon, contentDescription = null, tint = category.color) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(category.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Текущий лимит: ${category.limit} Br", fontSize = 14.sp, color = TextGray)
                    }
                }

                // Кнопка удаления (только если нет трат и не в режиме редактирования)
                if (!hasExpenses && !isEditing) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.clip(CircleShape).background(Color.Red.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить", tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- РЕЖИМ РЕДАКТИРОВАНИЯ ---
            if (isEditing) {
                OutlinedTextField(
                    value = editName, onValueChange = { editName = it },
                    label = { Text("Название") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editLimit, onValueChange = { editLimit = it },
                    label = { Text("Лимит (Br)") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Цвет", fontSize = 14.sp, color = TextGray, modifier = Modifier.align(Alignment.Start))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(availableColors) { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(if (color == editColor) 3.dp else 0.dp, TextDark, CircleShape)
                                .clickable { editColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Иконка", fontSize = 14.sp, color = TextGray, modifier = Modifier.align(Alignment.Start))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    items(availableIcons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (icon == editIcon) editColor.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.2f))
                                .border(if (icon == editIcon) 2.dp else 0.dp, if (icon == editIcon) editColor else Color.Transparent, CircleShape)
                                .clickable { editIcon = icon },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = if (icon == editIcon) editColor else TextGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Отмена", color = TextGray) }
                    Button(
                        onClick = {
                            val limit = editLimit.toDoubleOrNull()
                            if (editName.isNotBlank() && limit != null && limit > 0) {
                                onUpdateCategory(editName, limit, editIcon, editColor)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = editColor)
                    ) { Text("Сохранить", fontWeight = FontWeight.Bold) }
                }

            } else {
                // --- ОБЫЧНЫЙ РЕЖИМ (Добавление траты) ---
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it },
                    label = { Text("Сумма расхода (Br)") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull()
                        if (amount != null && amount > 0) onAddExpense(amount)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = category.color)
                ) { Text("Добавить расход", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                Spacer(modifier = Modifier.height(8.dp))
                // Кнопка перехода в режим редактирования
                // Кнопка перехода в режим редактирования
                TextButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Редактировать категорию", color = category.color, fontWeight = FontWeight.Medium) }
            }
        }
    }

    // Диалог удаления
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить категорию?") },
            text = { Text("Вы уверены, что хотите удалить \"${category.name}\"?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Удалить", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = TextGray) }
            },
            containerColor = CardWhite, titleContentColor = TextDark, textContentColor = TextGray
        )
    }
}

fun formatTransactionDate(timestamp: Long): String {
    val now = Calendar.getInstance()
    val transactionTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = sdfTime.format(Date(timestamp))

    val isToday = now.get(Calendar.YEAR) == transactionTime.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == transactionTime.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == transactionTime.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == transactionTime.get(Calendar.DAY_OF_YEAR)

    return when {
        isToday -> "Сегодня, $timeString"
        isYesterday -> "Вчера, $timeString"
        else -> {
            val sdfDate = SimpleDateFormat("d MMMM", Locale("ru")) // Например "12 мая"
            "${sdfDate.format(Date(timestamp))}, $timeString"
        }
    }
}

@Composable
fun GoalsScreen(
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean = false
) {
    val goals by viewModel.goals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddMoneyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedGoalForMoney by remember { mutableStateOf<GoalEntity?>(null) }
    var selectedGoalForDelete by remember { mutableStateOf<GoalEntity?>(null) }
    var completedGoalName by remember { mutableStateOf<String?>(null) }

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    Box(modifier = Modifier.fillMaxSize().background(currentBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("Мои цели", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
            Spacer(modifier = Modifier.height(16.dp))

            if (goals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("У вас пока нет целей. Добавьте первую!", color = TextGray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
                    items(goals) { goal ->
                        val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
                        val isCompleted = progress >= 1f

                        LaunchedEffect(isCompleted, goal.isCongratulated) {
                            if (isCompleted && !goal.isCongratulated) {
                                completedGoalName = goal.name
                                viewModel.markGoalAsCongratulated(goal)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = Color.LightGray.copy(alpha = 0.3f))
                                .clip(RoundedCornerShape(24.dp))
                                .background(currentCardColor)
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = currentTextColor)
                                        Text(
                                            "${String.format("%.0f", goal.currentAmount)} / ${goal.targetAmount} Br",
                                            fontSize = 14.sp,
                                            color = if (isCompleted) Color(0xFF38A169) else TextGray
                                        )
                                    }
                                    IconButton(onClick = { selectedGoalForDelete = goal; showDeleteDialog = true }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Удалить", tint = Color.Red.copy(alpha = 0.5f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    progress = { progress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                    color = if (isCompleted) Color(0xFF38A169) else GradientPurpleStart,
                                    trackColor = if (isDarkTheme) Color.DarkGray else LavenderBg,
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                if (!isCompleted) {
                                    Button(
                                        onClick = {
                                            selectedGoalForMoney = goal
                                            showAddMoneyDialog = true
                                        },
                                        modifier = Modifier.fillMaxWidth().height(40.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = GradientPurpleStart.copy(alpha = 0.1f), contentColor = GradientPurpleStart)
                                    ) {
                                        Text("Отложить деньги", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text("🎉 Цель достигнута!", color = Color(0xFF38A169), fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddGoalDialog = true },
            containerColor = Color(0xFF6843A9),
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(64.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить цель", tint = Color.White,  modifier = Modifier.size(28.dp))
        }
    }

    // ДИАЛОГ СОЗДАНИЯ ЦЕЛИ
    if (showAddGoalDialog) {
        var goalName by remember { mutableStateOf("") }
        var goalTarget by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Новая цель") },
            text = {
                Column {
                    OutlinedTextField(
                        value = goalName, onValueChange = { goalName = it },
                        label = { Text("Название (например, Отпуск)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = goalTarget, onValueChange = { goalTarget = it },
                        label = { Text("Сумма цели (Br)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = goalTarget.toDoubleOrNull()
                    if (goalName.isNotBlank() && target != null && target > 0) {
                        viewModel.addGoal(goalName, target, "🎯")
                        showAddGoalDialog = false
                    }
                }) {
                    Text("Создать", color = GradientPurpleStart, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = CardWhite
        )
    }

    // ДИАЛОГ ОТКЛАДЫВАНИЯ ДЕНЕГ
    if (showAddMoneyDialog && selectedGoalForMoney != null) {
        var moneyText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMoneyDialog = false },
            title = { Text("Отложить на \"${selectedGoalForMoney!!.name}\"") },
            text = {
                OutlinedTextField(
                    value = moneyText, onValueChange = { moneyText = it },
                    label = { Text("Сумма (Br)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = moneyText.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addMoneyToGoal(selectedGoalForMoney!!, amount)
                        showAddMoneyDialog = false
                    }
                }) {
                    Text("Отложить", color = GradientPurpleStart, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMoneyDialog = false }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = CardWhite
        )
    }

    // ДИАЛОГ УДАЛЕНИЯ ЦЕЛИ
    if (showDeleteDialog && selectedGoalForDelete != null) {
        val isGoalCompleted = selectedGoalForDelete!!.currentAmount >= selectedGoalForDelete!!.targetAmount
        val message = if (isGoalCompleted) {
            "Вы уверены, что хотите удалить достигнутую цель \"${selectedGoalForDelete!!.name}\"? Деньги на баланс не вернутся, так как цель уже выполнена."
        } else {
            "Вы уверены, что хотите удалить цель \"${selectedGoalForDelete!!.name}\"? Накопленные деньги вернутся на баланс."
        }

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить цель?") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(selectedGoalForDelete!!)
                    showDeleteDialog = false
                }) {
                    Text("Удалить", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", color = TextGray)
                }
            },
            containerColor = CardWhite
        )
    }

    // ОКНО ПОЗДРАВЛЕНИЯ
    if (completedGoalName != null) {
        AlertDialog(
            onDismissRequest = { completedGoalName = null },
            title = {
                Text(
                    text = "🎉 Поздравляем!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38A169),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Ты достигла цели \"$completedGoalName\"! Гордимся твоей дисциплиной и упорством. Продолжай в том же духе!",
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { completedGoalName = null },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38A169))
                ) {
                    Text("Ура!", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            containerColor = CardWhite
        )
    }
}


@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean = false
) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Считаем траты по категориям и целям
    val expensesByCategory = transactions
        .filter { it.type == "EXPENSE" }
        .groupBy {
            if (it.categoryId == -3) -3 // Цели
            else it.categoryId
        }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    val totalExpense = expensesByCategory.values.sum()

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    Box(modifier = Modifier.fillMaxSize().background(currentBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("Аналитика", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
            Spacer(modifier = Modifier.height(24.dp))

            if (totalExpense == 0.0) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Пока нет расходов для анализа", color = TextGray, fontSize = 16.sp)
                }
            } else {
                // Круговая диаграмма
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        var startAngle = -90f

                        // Настройки для текста внутри круга
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 12.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }

                        expensesByCategory.forEach { (categoryId, amount) ->
                            val category = categories.find { it.id == categoryId }
                            val isGoal = categoryId == -3
                            val sweepAngle = (amount / totalExpense * 360f).toFloat()

                            // Рисуем сектор
                            drawArc(
                                color = if (isGoal) GradientPurpleStart else (category?.color ?: Color.Gray),
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )

                            // Рисуем процент внутри сектора
                            if (sweepAngle > 20f) { // Показываем, если сектор достаточно большой
                                val midAngle = startAngle + sweepAngle / 2
                                val radius = size.minDimension / 2.5f
                                val x = center.x + radius * kotlin.math.cos(Math.toRadians(midAngle.toDouble())).toFloat()
                                val y = center.y + radius * kotlin.math.sin(Math.toRadians(midAngle.toDouble())).toFloat()

                                // Считаем процент с 1 знаком после запятой и меняем точку на запятую
                                val percent = String.format("%.1f", (amount / totalExpense * 100)).replace('.', ',')

                                drawContext.canvas.nativeCanvas.drawText(
                                    "$percent%",
                                    x, y,
                                    paint
                                )
                            }
                            startAngle += sweepAngle
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Легенда с суммами
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(expensesByCategory.toList().sortedByDescending { it.second }) { (categoryId, amount) ->
                        val category = categories.find { it.id == categoryId }
                        val isGoal = categoryId == -3

                        // Процент с 1 знаком после запятой
                        val percentStr = String.format("%.1f", (amount / totalExpense * 100)).replace('.', ',')

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(currentCardColor)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isGoal) GradientPurpleStart else (category?.color ?: Color.Gray))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isGoal) "Цели" else (category?.name ?: "Без категории"), color = currentTextColor)
                                    Text(
                                        text = "${String.format("%.2f", amount).replace('.', ',')} Br",
                                        fontSize = 12.sp,
                                        color = TextGray
                                    )
                                }
                            }
                            Text(
                                "$percentStr%",
                                color = if (isGoal) GradientPurpleStart else (category?.color ?: Color.Gray),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}