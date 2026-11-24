package com.example.trackermoney.data

import com.example.trackermoney.model.Expense
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

class SupabaseRepository(
    private val client: io.github.jan.supabase.SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun register(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun authenticate(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    fun hasActiveSession(): Boolean = client.auth.currentSessionOrNull() != null

    fun authenticatedUserId(): String = client.auth.currentUserOrNull()?.id
        ?: error("An authenticated user is required to access expenses")

    suspend fun signOut() {
        client.auth.signOut()
    }

    suspend fun loadExpenses(): List<Expense> = client
        .from("expenses")
        .select()
        .decodeList<CloudExpense>()
        .map(CloudExpense::toExpense)
        .sortedByDescending(Expense::date)

    suspend fun addExpense(expense: Expense) {
        client.from("expenses").upsert(expense.toCloudExpense())
    }

    suspend fun updateExpense(expense: Expense) {
        client.from("expenses").update(expense.toCloudExpense()) {
            filter { eq("id", expense.id) }
        }
    }

    suspend fun deleteExpense(expense: Expense) {
        client.from("expenses").delete {
            filter { eq("id", expense.id) }
        }
    }

    suspend fun deleteAllExpenses() {
        loadExpenses().forEach { expense -> deleteExpense(expense) }
    }

    suspend fun loadMonthlyBudget(): Double {
        val userId = currentUserId()
        return client.from("user_monthly_budgets").select {
            filter { eq("user_id", userId) }
        }.decodeList<CloudUserSettings>().firstOrNull()?.monthlyBudget ?: 0.0
    }

    suspend fun saveMonthlyBudget(amount: Double) {
        require(amount.isFinite() && amount >= 0.0) {
            "Monthly budget must be a non-negative number"
        }
        client.from("user_monthly_budgets").upsert(
            CloudUserSettings(
                userId = currentUserId(),
                monthlyBudget = amount
            )
        )
    }

    private fun currentUserId(): String = client.auth.currentUserOrNull()?.id
        ?: error("An authenticated user is required to access the monthly budget")

    private fun Expense.toCloudExpense() = CloudExpense(
        id = id,
        amount = amount,
        category = category,
        note = note,
        spentAt = Instant.ofEpochMilli(date).toString()
    )

    @Serializable
    private data class CloudUserSettings(
        @SerialName("user_id") val userId: String,
        @SerialName("monthly_budget") val monthlyBudget: Double
    )

    @Serializable
    private data class CloudExpense(
        val id: String = UUID.randomUUID().toString(),
        val amount: Double,
        val category: String,
        val note: String,
        @SerialName("spent_at") val spentAt: String
    ) {
        fun toExpense() = Expense(
            id = id,
            amount = amount,
            category = category,
            note = note,
            date = parseTimestamp(spentAt)
        )

        private fun parseTimestamp(value: String): Long {
            val normalizedValue = value.replace(Regex("([+-]\\d{2})$")) {
                "${it.groupValues[1]}:00"
            }
            return OffsetDateTime.parse(normalizedValue).toInstant().toEpochMilli()
        }
    }
}
