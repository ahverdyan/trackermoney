package com.example.trackermoney.data

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import com.example.trackermoney.model.Expense

private val Context.userDataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesRepository(
    private val context: Context
) {
    private val emailKey = stringPreferencesKey("user_email")
    private val saltKey = stringPreferencesKey("password_salt")
    private val passwordHashKey = stringPreferencesKey("password_hash")
    private val loggedInKey = booleanPreferencesKey("logged_in")
    private val expensesKey = stringPreferencesKey("expenses_json")
    private val darkThemeKey = booleanPreferencesKey("dark_theme")

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
                id = item.optLong("id"),
                amount = item.optDouble("amount"),
                category = item.optString("category"),
                note = item.optString("note"),
                date = item.optLong("date")
            )
        }
    }

    suspend fun saveExpenses(expenses: List<Expense>) {
        val array = JSONArray()
        expenses.forEach { expense ->
            array.put(JSONObject().apply {
                put("id", expense.id)
                put("amount", expense.amount)
                put("category", expense.category)
                put("note", expense.note)
                put("date", expense.date)
            })
        }
        context.userDataStore.edit { it[expensesKey] = array.toString() }
    }

    suspend fun clearProgress() {
        context.userDataStore.edit {
            it.remove(expensesKey)
            it.remove(loggedInKey)
        }
    }

    suspend fun hasActiveSession(): Boolean =
        context.userDataStore.data.first()[loggedInKey] ?: false

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        context.userDataStore.edit { it[loggedInKey] = isLoggedIn }
    }

    suspend fun register(email: String, password: String): Boolean {
        val preferences = context.userDataStore.data.first()

        // Если пользователь уже зарегистрирован, повторно не регистрируем.
        if (preferences[emailKey] != null) {
            return false
        }

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val passwordHash = hashPassword(password, salt)

        context.userDataStore.edit { data ->
            data[emailKey] = email.trim().lowercase()
            data[saltKey] = Base64.encodeToString(salt, Base64.NO_WRAP)
            data[passwordHashKey] = passwordHash
        }

        return true
    }

    suspend fun authenticate(
        email: String,
        password: String
    ): Boolean {
        val preferences = context.userDataStore.data.first()

        val savedEmail = preferences[emailKey] ?: return false
        val savedSalt = preferences[saltKey] ?: return false
        val savedHash = preferences[passwordHashKey] ?: return false

        if (savedEmail != email.trim().lowercase()) {
            return false
        }

        val salt = Base64.decode(savedSalt, Base64.NO_WRAP)
        val enteredHash = hashPassword(password, salt)

        return enteredHash == savedHash
    }

    private fun hashPassword(
        password: String,
        salt: ByteArray
    ): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            120_000,
            256
        )

        return try {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }
}
