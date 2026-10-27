package com.example.trackermoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.model.Expense
import com.example.trackermoney.ui.AddExpenseScreen
import com.example.trackermoney.ui.HomeScreen
import com.example.trackermoney.ui.HistoryScreen
import com.example.trackermoney.ui.LoginScreen
import com.example.trackermoney.ui.RegisterScreen
import com.example.trackermoney.ui.SettingsScreen
import com.example.trackermoney.ui.SplashScreen

private val Accent = Color(0xFF6275ED)
private val Muted = Color(0xFF737887)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentScreen by remember {
                mutableStateOf("splash")
            }

            var expenses by remember {
                mutableStateOf(emptyList<Expense>())
            }

            var isDarkTheme by remember {
                mutableStateOf(false)
            }

            MaterialTheme(
                colorScheme = if (isDarkTheme) {
                    darkColorScheme()
                } else {
                    lightColorScheme()
                }
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (
                            currentScreen == "home" ||
                            currentScreen == "analytics" ||
                            currentScreen == "history" ||
                            currentScreen == "profile"
                        ) {
                            BottomNavigation(
                                currentScreen = currentScreen,
                                onNavigate = { currentScreen = it },
                                onAdd = { currentScreen = "add" }
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (currentScreen) {

                            "splash" -> SplashScreen(
                                onFinished = {
                                    currentScreen = "login"
                                }
                            )

                            "login" -> LoginScreen(
                                onLoginSuccess = {
                                    currentScreen = "home"
                                },
                                onNavigateToRegister = {
                                    currentScreen = "register"
                                }
                            )

                            "register" -> RegisterScreen(
                                onRegistered = {
                                    currentScreen = "login"
                                },
                                onNavigateToLogin = {
                                    currentScreen = "login"
                                }
                            )

                            "home" -> HomeScreen(
                                expenses = expenses,
                                onAddExpense = {
                                    currentScreen = "add"
                                }
                            )

                            "add" -> AddExpenseScreen(
                                onBack = {
                                    currentScreen = "home"
                                },
                                onSave = {
                                        amount,
                                        category,
                                        title,
                                        dateMillis ->
                                    expenses = expenses + Expense(
                                        amount = amount,
                                        category = category,
                                        note = title,
                                        date = dateMillis
                                    )
                                    currentScreen = "home"
                                }
                            )

                            "history" -> HistoryScreen(
                                expenses = expenses
                            )

                            "profile" -> SettingsScreen(
                                isDarkTheme = isDarkTheme,
                                onThemeChange = {
                                    isDarkTheme = it
                                },
                                onLogout = {
                                    expenses = emptyList()
                                    currentScreen = "login"
                                }
                            )

                            else -> PlaceholderScreen(
                                title = "Аналитика"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavigation(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .navigationBarsPadding()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavigationItem(
                icon = Icons.Outlined.Home,
                label = "Главная",
                selected = currentScreen == "home",
                onClick = { onNavigate("home") }
            )

            NavigationItem(
                icon = Icons.Outlined.PieChart,
                label = "Аналитика",
                selected = currentScreen == "analytics",
                onClick = { onNavigate("analytics") }
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(50.dp)
                    .background(Accent, CircleShape)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Добавить расход",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            NavigationItem(
                icon = Icons.Outlined.History,
                label = "История",
                selected = currentScreen == "history",
                onClick = { onNavigate("history") }
            )

            NavigationItem(
                icon = Icons.Outlined.Person,
                label = "Настройки",
                selected = currentScreen == "profile",
                onClick = { onNavigate("profile") }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) {
        Accent
    } else {
        Muted
    }

    Column(
        modifier = Modifier
            .width(68.dp)
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(26.dp)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            }
        )
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Этот раздел добавим позже",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}