package com.example.trackermoney.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ExpenseDao {
    @Query("SELECT * FROM expenses WHERE ownerUserId = :userId AND COALESCE(pendingOperation, '') != 'DELETE' ORDER BY date DESC")
    abstract suspend fun getVisible(userId: String): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE ownerUserId = :userId AND pendingOperation IS NOT NULL")
    abstract suspend fun getPending(userId: String): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAll(expenses: List<ExpenseEntity>)

    @Query("UPDATE expenses SET pendingOperation = NULL WHERE id = :id AND ownerUserId = :userId")
    abstract suspend fun markSynced(id: String, userId: String)

    @Query("DELETE FROM expenses WHERE id = :id AND ownerUserId = :userId")
    abstract suspend fun delete(id: String, userId: String)

    @Query("DELETE FROM expenses WHERE ownerUserId = :userId AND pendingOperation IS NULL")
    abstract suspend fun deleteSynced(userId: String)

    @Transaction
    open suspend fun replaceSynced(userId: String, remoteExpenses: List<ExpenseEntity>) {
        deleteSynced(userId)
        upsertAll(remoteExpenses)
    }

    @Query("DELETE FROM expenses WHERE ownerUserId = :userId")
    abstract suspend fun clearUser(userId: String)

    @Query("UPDATE expenses SET pendingOperation = 'DELETE' WHERE ownerUserId = :userId")
    abstract suspend fun markAllForDeletion(userId: String)

    @Query("SELECT * FROM monthly_budgets WHERE ownerUserId = :userId LIMIT 1")
    abstract suspend fun getMonthlyBudget(userId: String): MonthlyBudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun saveMonthlyBudget(budget: MonthlyBudgetEntity)

    @Query("UPDATE monthly_budgets SET pendingUpload = 0 WHERE ownerUserId = :userId")
    abstract suspend fun markMonthlyBudgetSynced(userId: String)
}
