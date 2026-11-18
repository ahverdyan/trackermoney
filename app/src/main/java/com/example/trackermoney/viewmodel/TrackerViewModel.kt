package com.example.trackermoney.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trackermoney.data.SupabaseRepository
import com.example.trackermoney.data.UserPreferencesRepository
import com.example.trackermoney.domain.AnalyticsPeriod
import com.example.trackermoney.domain.ExpenseChartData
import com.example.trackermoney.domain.ExpenseStatistics
import com.example.trackermoney.domain.HomeExpenseSummary
import com.example.trackermoney.model.Expense
import java.util.Calendar
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
                    _uiState.update {
                        it.withExpenses(expenses).copy(currentScreen = "home", isLoading = false)
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
                messageChannel.trySend("Вход выполнен успешно!")
                _uiState.update {
                    it.copy(
                        currentScreen = "home",
                        isLoading = false,
                        loadError = null
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
                messageChannel.trySend("Регистрация прошла успешно! Проверьте почту, если придёт письмо подтверждения.")
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
                messageChannel.trySend("Сумма расхода должна быть больше нуля.")
                return@launch
            }
            if (category.isBlank()) {
                messageChannel.trySend("Выберите категорию расхода.")
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
                    "Расход сохранён, но список не обновился. ${expenseSaveError(error)}"
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
                messageChannel.trySend("Не удалось удалить расход")
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
                _uiState.update {
                    it.copy(
                        currentScreen = "home",
                        isLoading = false,
                        loadError = null
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

    fun logout() {
        viewModelScope.launch {
            try {
                cloudRepository.signOut()
                _uiState.update {
                    it.copy(currentScreen = "login", expenseBeingEdited = null)
                        .withExpenses(emptyList())
                }
            } catch (_: Exception) {
                messageChannel.trySend("Не удалось выйти из аккаунта")
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
                messageChannel.trySend("Расходы удалены. Вы остались в аккаунте.")
            } catch (_: Exception) {
                messageChannel.trySend("Не удалось сбросить данные")
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
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
        analyticsData = ExpenseStatistics.chart(newExpenses, analyticsPeriod, now)
    )

    private fun TrackerUiState.refreshExpenseSummaries(
        now: Long = System.currentTimeMillis()
    ): TrackerUiState = withExpenses(expenses, now)

    private fun TrackerUiState.refreshAnalytics(
        now: Long = System.currentTimeMillis()
    ): TrackerUiState = copy(
        analyticsData = ExpenseStatistics.chart(expenses, analyticsPeriod, now)
    )

    private fun authErrorMessage(error: Exception, isRegistration: Boolean = false): String {
        val details = errorDetails(error)
        val normalized = details.lowercase()

        return when {
            normalized.contains("email not confirmed") || normalized.contains("email_not_confirmed") ->
                "Подтвердите email по ссылке из письма, затем попробуйте войти."
            normalized.contains("invalid login credentials") ->
                "Неверный email или пароль. Если аккаунт новый, сначала подтвердите email в письме."
            normalized.contains("already registered") || normalized.contains("user already exists") ->
                "Этот email уже зарегистрирован. Попробуйте войти."
            isRegistration -> "Не удалось зарегистрироваться. Подробности: $details"
            else -> "Не удалось выполнить вход. Подробности: $details"
        }
    }

    private fun errorDetails(error: Exception): String =
        error.message
            ?.lineSequence()
            ?.firstOrNull()
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.take(160)
            ?: "Причина не указана (${error::class.simpleName ?: "ошибка"})."

    private fun expenseLoadError(error: Exception): String {
        val details = errorDetails(error)
        val normalized = details.lowercase()

        val guidance = when {
            normalized.contains("schema cache") || normalized.contains("does not exist") ||
                normalized.contains("not found") ->
                "Supabase не находит таблицу public.expenses. Проверьте, что SQL-миграция расходов выполнена в SQL Editor."
            normalized.contains("permission") || normalized.contains("row-level security") ||
                normalized.contains("401") || normalized.contains("403") ->
                "Supabase отказал в доступе. Проверьте RLS-политики таблицы expenses и активную сессию пользователя."
            normalized.contains("timeout") || normalized.contains("connect") ||
                normalized.contains("host") || normalized.contains("network") ->
                "Нет соединения с Supabase. Проверьте интернет и URL проекта."
            else -> ""
        }

        return listOf(guidance, "Техническая причина: $details")
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
                "Телефон не смог подключиться к Supabase. Проверьте сеть; если API недоступен из вашей сети, попробуйте VPN или другой интернет."
            normalized.contains("permission denied") || normalized.contains("row-level security") ||
                normalized.contains("42501") || normalized.contains("401") || normalized.contains("403") ->
                "Supabase отклонил запись. Проверьте, что пользователь вошёл в аккаунт и INSERT-политика RLS разрешает добавление расходов."
            normalized.contains("schema cache") || normalized.contains("column") ||
                normalized.contains("does not exist") || normalized.contains("not-null") ->
                "Схема таблицы expenses не совпадает с приложением. Проверьте SQL-миграцию и имена колонок."
            else -> ""
        }

        return listOf(cause, "Техническая причина: $details")
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
