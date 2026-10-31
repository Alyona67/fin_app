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

// ПАЛИТРА
val LavenderBg = Color(0xFFF4F0FA)
val GradientPurpleStart = Color(0xFF7B3AF1)
val GradientPurpleEnd = Color(0xFF996FF3)
val AccentPurple = Color(0xFF6B46C1)
val TextDark = Color(0xFF2D3748)
val TextGray = Color(0xFF718096)
val CardWhite = Color(0xFFFFFFFF)

// Цвета для темной темы
val DarkBg = Color(0xFF121212)
val DarkCard = Color(0xFF1E1E24)

@Composable
fun DashboardScreen(
    viewModel: MainViewModel = viewModel(),
    onAddIncomeClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val balance by viewModel.totalBalance.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val spentMap = remember(transactions, categories) {
        categories.associate { cat ->
            cat.id to transactions.filter { it.categoryId == cat.id && it.type == TransactionType.EXPENSE }.sumOf { it.amount }
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
                        .background(Brush.linearGradient(colors = listOf(GradientPurpleStart, GradientPurpleEnd)))
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(categories) { category ->
                    val spent = transactions
                        .filter { it.categoryId == category.id && it.type == TransactionType.EXPENSE }
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

        FloatingActionButton(
            onClick = onAddCategoryClick,
            containerColor = Color(0xFF6843A9),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Добавить категорию", tint = Color.White)
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
            onUpdateLimit = { newLimit ->
                viewModel.updateCategoryLimit(selectedCategory!!.id, newLimit)
            }
        )
    }
}

@Composable
fun ProfileScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToGoals: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) DarkBg else LavenderBg)
            .padding(24.dp)
    ) {
        Text(
            text = "Настройки",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else TextDark
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDarkTheme) DarkCard else CardWhite)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Темная тема",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else TextDark
            )
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { onThemeChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GradientPurpleStart
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(if (isDarkTheme) DarkCard else CardWhite)
                .clickable { onNavigateToGoals() }
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Мои цели",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else TextDark
            )
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = GradientPurpleStart
            )
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean = false
) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    // Состояние для транзакции, которую хотим удалить
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

    val currentBg = if (isDarkTheme) DarkBg else LavenderBg
    val currentTextColor = if (isDarkTheme) Color.White else TextDark
    val currentCardColor = if (isDarkTheme) DarkCard else CardWhite

    val sortedTransactions = transactions.sortedByDescending { it.date }

    Box(modifier = Modifier.fillMaxSize().background(currentBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text("История", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = currentTextColor)
            Spacer(modifier = Modifier.height(16.dp))

            if (sortedTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("У вас пока нет операций", color = TextGray, fontSize = 16.sp)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(sortedTransactions) { transaction ->
                        val category = categories.find { it.id == transaction.categoryId }
                        val title = if (transaction.type == TransactionType.INCOME) "Пополнение" else (category?.name ?: "Без категории")
                        val icon = if (transaction.type == TransactionType.INCOME) Icons.Filled.Add else (category?.icon ?: Icons.Filled.Star)
                        val color = if (transaction.type == TransactionType.INCOME) Color(0xFF38A169) else (category?.color ?: TextGray)
                        val amountText = if (transaction.type == TransactionType.INCOME) "+${String.format("%.2f", transaction.amount)} Br" else "-${String.format("%.2f", transaction.amount)} Br"

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
                                        // Вставляем функцию форматирования даты (если ты её добавила)
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
                                        color = if (transaction.type == TransactionType.INCOME) Color(0xFF38A169) else currentTextColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Вместо прямого удаления, мы теперь открываем диалог
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

    // ОКНО ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Удалить операцию?") },
            text = {
                Text("Вы уверены, что хотите удалить эту операцию? Сумма вернется на баланс.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { viewModel.deleteTransaction(it) }
                        transactionToDelete = null // Закрываем окно
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
    onUpdateLimit: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingLimit by remember { mutableStateOf(false) }
    var limitText by remember { mutableStateOf(category.limit.toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = CardWhite) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

                if (!hasExpenses) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.clip(CircleShape).background(Color.Red.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Удалить", tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditingLimit) {
                OutlinedTextField(
                    value = limitText, onValueChange = { limitText = it },
                    label = { Text("Новый лимит (Br)") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = category.color, unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = TextDark, unfocusedTextColor = TextDark,
                        focusedLabelColor = category.color, unfocusedLabelColor = TextGray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { isEditingLimit = false },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Отмена", color = TextGray) }
                    Button(
                        onClick = {
                            val newLimit = limitText.toDoubleOrNull()
                            if (newLimit != null && newLimit > 0) { onUpdateLimit(newLimit); onDismiss() }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = category.color)
                    ) { Text("Сохранить", fontWeight = FontWeight.Bold) }
                }
            } else {
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it },
                    label = { Text("Сумма расхода (Br)") },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = category.color, unfocusedBorderColor = Color.LightGray,
                        focusedTextColor = TextDark, unfocusedTextColor = TextDark,
                        focusedLabelColor = category.color, unfocusedLabelColor = TextGray
                    )
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

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { isEditingLimit = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) { Text("Изменить лимит", color = category.color, fontWeight = FontWeight.Medium) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

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