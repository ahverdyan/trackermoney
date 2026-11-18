package com.example.trackermoney

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
import com.example.trackermoney.ui.TrackerMoneyTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trackermoney.viewmodel.TrackerViewModel
import androidx.core.os.LocaleListCompat
import com.example.trackermoney.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val trackerViewModel: TrackerViewModel = viewModel()
            val uiState by trackerViewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val currentLanguage = AppCompatDelegate.getApplicationLocales()
                .get(0)?.language ?: resources.configuration.locales[0].language

            LaunchedEffect(currentLanguage) {
                trackerViewModel.refreshLocalizedData()
            }

            LaunchedEffect(trackerViewModel) {
                trackerViewModel.messages.collect { message ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        withDismissAction = false,
                        duration = SnackbarDuration.Short
                    )
                }
            }

            TrackerMoneyTheme(isDarkTheme = uiState.isDarkTheme) {
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
                                monthlyBudget = uiState.monthlyBudget,
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
                                currentLanguage = currentLanguage,
                                onLanguageChange = { languageTag ->
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.forLanguageTags(languageTag)
                                    )
                                },
                                monthlyBudget = uiState.monthlyBudget,
                                onSaveMonthlyBudget = trackerViewModel::saveMonthlyBudget,
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
                                    text = stringResource(R.string.load_error_title),
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
                                    Text(
                                        stringResource(
                                            if (uiState.isLoading) R.string.loading else R.string.retry
                                        )
                                    )
                                }
                                TextButton(onClick = trackerViewModel::logout) {
                                    Text(stringResource(R.string.settings_logout))
                                }
                            }
                            else -> PlaceholderScreen(title = stringResource(R.string.nav_analytics))
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
                label = stringResource(R.string.nav_home),
                selected = currentScreen == "home",
                onClick = { onNavigate("home") }
            )

            NavigationItem(
                icon = Icons.Outlined.PieChart,
                label = stringResource(R.string.nav_analytics),
                selected = currentScreen == "analytics",
                onClick = { onNavigate("analytics") }
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(50.dp)
                    .background(colorResource(R.color.brand_accent), CircleShape)
                    .clickable { onAdd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.nav_add_expense),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            NavigationItem(
                icon = Icons.Outlined.History,
                label = stringResource(R.string.nav_history),
                selected = currentScreen == "history",
                onClick = { onNavigate("history") }
            )

            NavigationItem(
                icon = Icons.Outlined.Person,
                label = stringResource(R.string.nav_settings),
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
    val accent = colorResource(R.color.brand_accent)
    val muted = colorResource(R.color.brand_muted)
    val color = if (selected) {
        accent
    } else {
        muted
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
                text = stringResource(R.string.placeholder_later),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
