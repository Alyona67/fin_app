package com.example.fin_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fin_app.ui.theme.Fin_appTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Fin_appTheme {
                val viewModel: MainViewModel = viewModel()
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    var showExpenseSheet by remember { mutableStateOf(false) }
    var showIncomeSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }

    var isDarkTheme by remember { mutableStateOf(false) }

    // 4 вкладки внизу: Главная, Категории, История, Профиль
    val items = listOf(
        BottomNavItem("dashboard", "Главная", Icons.Filled.Home),
        BottomNavItem("budgets", "Категории", Icons.Filled.List),
        BottomNavItem("history", "История", Icons.Filled.DateRange),
        BottomNavItem("profile", "Профиль", Icons.Filled.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkTheme) Color(0xFF1E1E24) else Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color(0xFF5B21B6) else Color(0xFF9CA3AF)
                            )
                        },
                        label = {
                            Text(
                                item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF5B21B6) else Color(0xFF9CA3AF)
                            )
                        },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF5B21B6).copy(alpha = 0.1f)
                        ),
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }

                            // Если нажали на Профиль, принудительно закрываем Цели (если они открыты)
                            if (item.route == "profile") {
                                navController.popBackStack("profile", inclusive = false)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddIncomeClick = { showIncomeSheet = true },
                    isDarkTheme = isDarkTheme
                )
            }
            composable("budgets") {
                BudgetsScreen(
                    viewModel = viewModel,
                    onAddCategoryClick = { showCategorySheet = true },
                    isDarkTheme = isDarkTheme
                )
            }
            composable("history") {
                HistoryScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
            }
            composable("goals") {
                GoalsScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
            }
            composable("analytics") {
                AnalyticsScreen(viewModel = viewModel, isDarkTheme = isDarkTheme)
            }
            composable("profile") {
                ProfileScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = { isDarkTheme = it },
                    onNavigateToGoals = {
                        // Переход на скрытый экран Целей
                        navController.navigate("goals")
                    },
                    onNavigateToAnalytics = { navController.navigate("analytics") }
                )
            }
        }
    }

    // Всплывающие окна (Bottom Sheets)
    if (showExpenseSheet) {
        AddTransactionSheet(
            onDismiss = { showExpenseSheet = false },
            onSave = { amount, categoryId -> viewModel.addExpense(amount, categoryId) },
            categories = viewModel.categories.collectAsState().value
        )
    }
    if (showIncomeSheet) {
        AddIncomeSheet(
            onDismiss = { showIncomeSheet = false },
            onSave = { amount -> viewModel.addIncome(amount) }
        )
    }
    if (showCategorySheet) {
        AddCategorySheet(
            onDismiss = { showCategorySheet = false },
            onSave = { name, limit, icon, color -> viewModel.addCategory(name, limit, icon, color) }
        )
    }
}