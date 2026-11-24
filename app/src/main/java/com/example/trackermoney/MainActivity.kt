package com.example.trackermoney

import android.os.Bundle
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
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
import androidx.lifecycle.ViewModelProvider
import com.example.trackermoney.viewmodel.TrackerViewModel
import androidx.core.os.LocaleListCompat
import com.example.trackermoney.R

class MainActivity : AppCompatActivity() {
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onStart() {
        super.onStart()
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                ViewModelProvider(this@MainActivity)[TrackerViewModel::class.java].syncPendingExpenses()
            }
        }
        networkCallback = callback
        runCatching { connectivityManager.registerDefaultNetworkCallback(callback) }
    }

    override fun onStop() {
        networkCallback?.let { callback ->
            runCatching {
                getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(callback)
            }
        }
        networkCallback = null
        super.onStop()
    }

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

                            "history" -> FragmentDestination("history")

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
                                onResetProgress = trackerViewModel::resetProgress,
                                onOpenAbout = { startActivity(Intent(this@MainActivity, AboutActivity::class.java)) },
                                onShareApp = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, getString(R.string.about_share_text))
                                    }
                                    startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_share)))
                                }
                            )

                            "analytics" -> FragmentDestination("analytics")
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
private fun FragmentDestination(destination: String) {
    val activity = LocalContext.current as FragmentActivity
    val fragmentManager = activity.supportFragmentManager
    val containerId = remember(destination) { View.generateViewId() }
    val fragment = remember(destination) {
        when (destination) {
            "history" -> com.example.trackermoney.ui.HistoryFragment()
            else -> com.example.trackermoney.ui.AnalyticsFragment()
        }
    }

    AndroidView(
        factory = { context -> FragmentContainerView(context).apply { id = containerId } },
        update = { container ->
            container.post {
                if (container.isAttachedToWindow && fragmentManager.findFragmentById(container.id) == null) {
                    fragmentManager.beginTransaction()
                        .replace(container.id, fragment, destination)
                        .commitNowAllowingStateLoss()
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(containerId) {
        onDispose {
            fragmentManager.findFragmentById(containerId)?.let { current ->
                fragmentManager.beginTransaction()
                    .remove(current)
                    .commitNowAllowingStateLoss()
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
