package com.example.trackermoney.data

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.flow.first

private val Context.userDataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserPreferencesRepository(
    private val context: Context
) {
    private val emailKey = stringPreferencesKey("user_email")
    private val saltKey = stringPreferencesKey("password_salt")
    private val passwordHashKey = stringPreferencesKey("password_hash")

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
