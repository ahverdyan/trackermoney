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
import com.example.trackermoney.ui.AddExpenseScreen
import com.example.trackermoney.ui.HomeScreen
import com.example.trackermoney.ui.HistoryScreen
import com.example.trackermoney.ui.LoginScreen
import com.example.trackermoney.ui.RegisterScreen
import com.example.trackermoney.ui.SettingsScreen
import com.example.trackermoney.ui.SplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackermoney.viewmodel.TrackerViewModel

private val Accent = Color(0xFF6275ED)
private val Muted = Color(0xFF737887)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val trackerViewModel: TrackerViewModel = viewModel()
            val uiState by trackerViewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(trackerViewModel) {
                trackerViewModel.messages.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        withDismissAction = false,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            MaterialTheme(
                colorScheme = if (uiState.isDarkTheme) {
                    darkColorScheme()
                } else {
                    lightColorScheme()
                }
            ) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (
                            uiState.currentScreen == "home" ||
                            uiState.currentScreen == "analytics" ||
                            uiState.currentScreen == "history" ||
                            uiState.currentScreen == "profile"
                        ) {
                            BottomNavigation(
                                currentScreen = uiState.currentScreen,
                                onNavigate = trackerViewModel::navigateTo,
                                onAdd = { trackerViewModel.navigateTo("add") }
                            )
                        }
                    }
                ) { padding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        when (uiState.currentScreen) {

                            "splash" -> SplashScreen(
                                onFinished = {
                                    trackerViewModel.navigateTo("login")
                                }
                            )

                            "login" -> LoginScreen(
                                isLoading = uiState.isLoading,
                                onLogin = trackerViewModel::login,
                                onNavigateToRegister = {
                                    trackerViewModel.navigateTo("register")
                                }
                            )

                            "register" -> RegisterScreen(
                                isLoading = uiState.isLoading,
                                onRegister = trackerViewModel::register,
                                onNavigateToLogin = {
                                    trackerViewModel.navigateTo("login")
                                }
                            )

                            "home" -> HomeScreen(
                                summary = uiState.homeSummary,
                                onAddExpense = {
                                    trackerViewModel.navigateTo("add")
                                }
                            )

                            "add" -> AddExpenseScreen(
                                onBack = {
                                    trackerViewModel.navigateTo("home")
                                },
                                onSave = trackerViewModel::saveExpense,
                                expenseToEdit = uiState.expenseBeingEdited
                            )

                            "history" -> HistoryScreen(
                                sortedExpenses = uiState.sortedExpenses,
                                onEdit = trackerViewModel::editExpense,
                                onDelete = trackerViewModel::deleteExpense
                            )

                            "profile" -> SettingsScreen(
                                isDarkTheme = uiState.isDarkTheme,
                                onThemeChange = trackerViewModel::changeTheme,
                                onLogout = trackerViewModel::logout,
                                onResetProgress = trackerViewModel::resetProgress
                            )

                            "analytics" -> com.example.trackermoney.ui.AnalyticsScreen(
                                data = uiState.analyticsData,
                                selectedPeriod = uiState.analyticsPeriod,
                                onPeriodChange = trackerViewModel::selectAnalyticsPeriod
                            )
                            "load_error" -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Не удалось загрузить расходы",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = uiState.loadError.orEmpty(),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(20.dp))
                                Button(
                                    onClick = trackerViewModel::retryLoadingExpenses,
                                    enabled = !uiState.isLoading
                                ) {
                                    Text(if (uiState.isLoading) "Загрузка..." else "Попробовать снова")
                                }
                                TextButton(onClick = trackerViewModel::logout) {
                                    Text("Выйти из аккаунта")
                                }
                            }
                            else -> PlaceholderScreen(title = "Аналитика")
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
