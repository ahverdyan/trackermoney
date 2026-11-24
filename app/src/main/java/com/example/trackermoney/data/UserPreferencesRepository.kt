package com.example.trackermoney.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import com.example.trackermoney.model.Expense

private val Context.userDataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesRepository(
    private val context: Context
) {
    private val expensesKey = stringPreferencesKey("expenses_json")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")
    private val cloudMigrationDoneKey = booleanPreferencesKey("cloud_expenses_migration_done")

    suspend fun loadDarkTheme(): Boolean =
        context.userDataStore.data.first()[darkThemeKey] ?: false

    suspend fun saveDarkTheme(isDark: Boolean) {
        context.userDataStore.edit { it[darkThemeKey] = isDark }
    }

    suspend fun loadExpenses(): List<Expense> {
        val saved = context.userDataStore.data.first()[expensesKey] ?: return emptyList()
        val array = JSONArray(saved)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            Expense(
                id = item.optString("id"),
                amount = item.optDouble("amount"),
                category = item.optString("category"),
                note = item.optString("note"),
                date = item.optLong("date")
            )
        }
    }

    suspend fun clearProgress() {
        context.userDataStore.edit {
            it.remove(expensesKey)
            // После сброса не нужно повторно мигрировать старые локальные расходы.
            it[cloudMigrationDoneKey] = true
        }
    }

    suspend fun hasMigratedExpensesToCloud(): Boolean =
        context.userDataStore.data.first()[cloudMigrationDoneKey] ?: false

    suspend fun markExpensesMigratedToCloud() {
        context.userDataStore.edit { it[cloudMigrationDoneKey] = true }
    }

}
