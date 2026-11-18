package com.example.trackermoney.viewmodel

import android.app.Application
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackermoney.data.SupabaseRepository
import com.example.trackermoney.data.UserPreferencesRepository
import com.example.trackermoney.domain.AnalyticsPeriod
import com.example.trackermoney.domain.ExpenseChartData
import com.example.trackermoney.domain.ExpenseStatistics
import com.example.trackermoney.domain.HomeExpenseSummary
import com.example.trackermoney.model.Expense
import com.example.trackermoney.R
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackerUiState(
    val currentScreen: String = "splash",
    val expenses: List<Expense> = emptyList(),
    val sortedExpenses: List<Expense> = emptyList(),
    val homeSummary: HomeExpenseSummary = HomeExpenseSummary(),
    val analyticsPeriod: AnalyticsPeriod = AnalyticsPeriod.WEEK,
    val analyticsData: ExpenseChartData = ExpenseChartData(),
    val monthlyBudget: Double = 0.0,
    val isDarkTheme: Boolean = false,
    val expenseBeingEdited: Expense? = null,
    val isLoading: Boolean = true,
    val loadError: String? = null
)

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesRepository = UserPreferencesRepository(application)
    private val cloudRepository = SupabaseRepository()

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState = _uiState.asStateFlow()

    private val messageChannel = Channel<String>(Channel.BUFFERED)
    val messages = messageChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            val isDarkTheme = preferencesRepository.loadDarkTheme()
            _uiState.update { it.copy(isDarkTheme = isDarkTheme) }

            if (cloudRepository.hasActiveSession()) {
                try {
                    val expenses = loadExpensesWithLegacyMigration()
                    val monthlyBudget = loadMonthlyBudgetOrDefault()
                    _uiState.update {
                        it.withExpenses(expenses).copy(
                            currentScreen = "home",
                            isLoading = false,
                            monthlyBudget = monthlyBudget
                        )
                    }
                } catch (error: Exception) {
                    _uiState.update {
                        it.copy(
                            currentScreen = "load_error",
                            isLoading = false,
                            loadError = expenseLoadError(error)
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(currentScreen = "login", isLoading = false) }
            }
        }

        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val nextDayStart = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis
                delay((nextDayStart - now).coerceAtLeast(1L))
                _uiState.update { it.refreshExpenseSummaries() }
            }
        }
    }

    fun navigateTo(screen: String) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            setLoading(true)
            try {
                cloudRepository.authenticate(email, password)
            } catch (error: Exception) {
                messageChannel.trySend(authErrorMessage(error))
                setLoading(false)
                return@launch
            }

            try {
                val expenses = loadExpensesWithLegacyMigration()
                val monthlyBudget = loadMonthlyBudgetOrDefault()
                messageChannel.trySend(localizedString(R.string.auth_success))
                _uiState.update {
                    it.copy(
                        currentScreen = "home",
                        isLoading = false,
                        loadError = null,
                        monthlyBudget = monthlyBudget
                    ).withExpenses(expenses)
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        currentScreen = "load_error",
                        isLoading = false,
                        loadError = expenseLoadError(error)
                    )
                }
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            setLoading(true)
            try {
                cloudRepository.register(email, password)
                messageChannel.trySend(localizedString(R.string.register_success))
                _uiState.update { it.copy(currentScreen = "login", isLoading = false) }
            } catch (error: Exception) {
                messageChannel.trySend(authErrorMessage(error, isRegistration = true))
                setLoading(false)
            }
        }
    }

    fun saveExpense(amount: Double, category: String, note: String, selectedDateMillis: Long) {
        viewModelScope.launch {
            if (!amount.isFinite() || amount <= 0.0) {
                messageChannel.trySend(localizedString(R.string.expense_amount_positive))
                return@launch
            }
            if (category.isBlank()) {
                messageChannel.trySend(localizedString(R.string.expense_category_required))
                return@launch
            }

            setLoading(true)
            val currentTime = Calendar.getInstance()
            val date = Calendar.getInstance().apply {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, currentTime.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, currentTime.get(Calendar.MINUTE))
                set(Calendar.SECOND, currentTime.get(Calendar.SECOND))
                set(Calendar.MILLISECOND, currentTime.get(Calendar.MILLISECOND))
            }.timeInMillis
            val current = _uiState.value
            val edited = current.expenseBeingEdited
            val expense = edited?.copy(amount = amount, category = category, note = note, date = date)
                ?: Expense(amount = amount, category = category, note = note, date = date)

            try {
                if (edited == null) cloudRepository.addExpense(expense)
                else cloudRepository.updateExpense(expense)
            } catch (error: Exception) {
                messageChannel.trySend(expenseSaveError(error))
                setLoading(false)
                return@launch
            }

            val localExpenses = if (edited == null) {
                _uiState.value.expenses + expense
            } else {
                _uiState.value.expenses.map { if (it.id == expense.id) expense else it }
            }
            _uiState.update {
                it.withExpenses(localExpenses).copy(
                    expenseBeingEdited = null,
                    currentScreen = "home",
                    isLoading = false
                )
            }

            try {
                val updatedExpenses = cloudRepository.loadExpenses()
                _uiState.update { it.withExpenses(updatedExpenses) }
            } catch (error: Exception) {
                messageChannel.trySend(
                    localizedString(
                        R.string.expense_saved_refresh_failed,
                        expenseSaveError(error)
                    )
                )
            }
        }
    }

    fun editExpense(expense: Expense) {
        _uiState.update { it.copy(expenseBeingEdited = expense, currentScreen = "add") }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                cloudRepository.deleteExpense(expense)
                _uiState.update { state ->
                    state.withExpenses(state.expenses.filterNot { it.id == expense.id })
                }
            } catch (_: Exception) {
                messageChannel.trySend(localizedString(R.string.expense_delete_failed))
            }
        }
    }

    fun selectAnalyticsPeriod(period: AnalyticsPeriod) {
        _uiState.update { state -> state.copy(analyticsPeriod = period).refreshAnalytics() }
    }

    fun retryLoadingExpenses() {
        viewModelScope.launch {
            setLoading(true)
            try {
                val expenses = loadExpensesWithLegacyMigration()
                val monthlyBudget = loadMonthlyBudgetOrDefault()
                _uiState.update {
                    it.copy(
                        currentScreen = "home",
                        isLoading = false,
                        loadError = null,
                        monthlyBudget = monthlyBudget
                    ).withExpenses(expenses)
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        currentScreen = "load_error",
                        isLoading = false,
                        loadError = expenseLoadError(error)
                    )
                }
            }
        }
    }

    fun changeTheme(isDarkTheme: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDarkTheme) }
        viewModelScope.launch { preferencesRepository.saveDarkTheme(isDarkTheme) }
    }

    fun saveMonthlyBudget(amount: Double) {
        if (!amount.isFinite() || amount < 0.0) {
            messageChannel.trySend(localizedString(R.string.budget_invalid))
            return
        }

        viewModelScope.launch {
            try {
                cloudRepository.saveMonthlyBudget(amount)
                _uiState.update { it.copy(monthlyBudget = amount) }
                messageChannel.trySend(localizedString(R.string.budget_saved))
            } catch (error: Exception) {
                messageChannel.trySend(
                    localizedString(R.string.budget_save_failed)
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                cloudRepository.signOut()
                _uiState.update {
                    it.copy(currentScreen = "login", expenseBeingEdited = null)
                        .withExpenses(emptyList())
                }
            } catch (_: Exception) {
                messageChannel.trySend(localizedString(R.string.logout_failed))
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            setLoading(true)
            try {
                cloudRepository.deleteAllExpenses()
                preferencesRepository.clearProgress()
                _uiState.update {
                    it.copy(
                        expenseBeingEdited = null,
                        isLoading = false
                    ).withExpenses(emptyList())
                }
                messageChannel.trySend(localizedString(R.string.reset_success))
            } catch (_: Exception) {
                messageChannel.trySend(localizedString(R.string.reset_failed))
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun refreshLocalizedData() {
        _uiState.update { it.refreshAnalytics() }
    }

    private fun currentAppLocale(): Locale =
        AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()

    private fun localizedString(@StringRes resourceId: Int, vararg arguments: Any): String {
        val application = getApplication<Application>()
        val locale = AppCompatDelegate.getApplicationLocales().get(0)
            ?: return application.getString(resourceId, *arguments)
        val configuration = Configuration(application.resources.configuration).apply {
            setLocale(locale)
        }
        return application.createConfigurationContext(configuration)
            .getString(resourceId, *arguments)
    }

    private suspend fun loadMonthlyBudgetOrDefault(): Double = try {
        cloudRepository.loadMonthlyBudget()
    } catch (_: Exception) {
        messageChannel.trySend(
            localizedString(R.string.budget_load_failed)
        )
        0.0
    }

    private fun TrackerUiState.withExpenses(
        newExpenses: List<Expense>,
        now: Long = System.currentTimeMillis()
    ): TrackerUiState = copy(
        expenses = newExpenses,
        sortedExpenses = newExpenses.sortedWith(
            compareByDescending<Expense> { it.date }.thenByDescending { it.id }
        ),
        homeSummary = ExpenseStatistics.homeSummary(newExpenses, now),
        analyticsData = ExpenseStatistics.chart(
            newExpenses,
            analyticsPeriod,
            now,
            currentAppLocale()
        )
    )

    private fun TrackerUiState.refreshExpenseSummaries(
        now: Long = System.currentTimeMillis()
    ): TrackerUiState = withExpenses(expenses, now)

    private fun TrackerUiState.refreshAnalytics(
        now: Long = System.currentTimeMillis()
    ): TrackerUiState = copy(
        analyticsData = ExpenseStatistics.chart(
            expenses,
            analyticsPeriod,
            now,
            currentAppLocale()
        )
    )

    private fun authErrorMessage(error: Exception, isRegistration: Boolean = false): String {
        val details = errorDetails(error)
        val normalized = details.lowercase()

        return when {
            normalized.contains("email not confirmed") || normalized.contains("email_not_confirmed") ->
                localizedString(R.string.auth_email_unconfirmed)
            normalized.contains("invalid login credentials") ->
                localizedString(R.string.auth_invalid_credentials)
            normalized.contains("already registered") || normalized.contains("user already exists") ->
                localizedString(R.string.auth_already_registered)
            isRegistration -> localizedString(R.string.auth_register_failed, details)
            else -> localizedString(R.string.auth_login_failed, details)
        }
    }

    private fun errorDetails(error: Exception): String =
        error.message
            ?.lineSequence()
            ?.firstOrNull()
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.take(160)
            ?: localizedString(
                R.string.error_reason_unknown,
                error::class.simpleName ?: localizedString(R.string.error_fallback)
            )

    private fun expenseLoadError(error: Exception): String {
        val details = errorDetails(error)
        val normalized = details.lowercase()

        val guidance = when {
            normalized.contains("schema cache") || normalized.contains("does not exist") ||
                normalized.contains("not found") ->
                localizedString(R.string.expense_load_schema_help)
            normalized.contains("permission") || normalized.contains("row-level security") ||
                normalized.contains("401") || normalized.contains("403") ->
                localizedString(R.string.expense_load_permission_help)
            normalized.contains("timeout") || normalized.contains("connect") ||
                normalized.contains("host") || normalized.contains("network") ->
                localizedString(R.string.expense_load_network_help)
            else -> ""
        }

        return listOf(guidance, localizedString(R.string.technical_reason, details))
            .filter(String::isNotBlank)
            .joinToString("\n\n")
    }

    private fun expenseSaveError(error: Exception): String {
        val details = errorDetails(error)
        val normalized = details.lowercase()

        val cause = when {
            normalized.contains("unknownhost") || normalized.contains("unable to resolve host") ||
                normalized.contains("failed to connect") || normalized.contains("timeout") ||
                normalized.contains("network is unreachable") ->
                localizedString(R.string.expense_save_network_help)
            normalized.contains("permission denied") || normalized.contains("row-level security") ||
                normalized.contains("42501") || normalized.contains("401") || normalized.contains("403") ->
                localizedString(R.string.expense_save_permission_help)
            normalized.contains("schema cache") || normalized.contains("column") ||
                normalized.contains("does not exist") || normalized.contains("not-null") ->
                localizedString(R.string.expense_save_schema_help)
            else -> ""
        }

        return listOf(cause, localizedString(R.string.technical_reason, details))
            .filter(String::isNotBlank)
            .joinToString("\n\n")
    }

    private suspend fun loadExpensesWithLegacyMigration(): List<Expense> {
        var expenses = cloudRepository.loadExpenses()

        if (!preferencesRepository.hasMigratedExpensesToCloud()) {
            val existingIds = expenses.mapTo(mutableSetOf()) { it.id }
            preferencesRepository.loadExpenses().forEach { legacyExpense ->
                val cloudId = UUID.nameUUIDFromBytes(
                    "legacy:${legacyExpense.id}:${legacyExpense.date}".toByteArray()
                ).toString()
                if (cloudId !in existingIds) {
                    cloudRepository.addExpense(legacyExpense.copy(id = cloudId))
                }
            }
            preferencesRepository.markExpensesMigratedToCloud()
            expenses = cloudRepository.loadExpenses()
        }

        return expenses
    }
}
