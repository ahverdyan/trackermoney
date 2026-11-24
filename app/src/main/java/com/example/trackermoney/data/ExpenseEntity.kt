package com.example.trackermoney.data

import androidx.room.Entity
import androidx.room.Index
import com.example.trackermoney.model.Expense

@Entity(
    tableName = "expenses",
    primaryKeys = ["id", "ownerUserId"],
    indices = [Index(value = ["ownerUserId", "date"]) ]
)
data class ExpenseEntity(
    val id: String,
    val ownerUserId: String,
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long,
    val pendingOperation: String? = null
) {
    fun toExpense() = Expense(id, amount, category, note, date)

    companion object {
        const val UPSERT = "UPSERT"
        const val DELETE = "DELETE"
        fun from(expense: Expense, ownerUserId: String, pendingOperation: String? = null) =
            ExpenseEntity(
                id = expense.id,
                ownerUserId = ownerUserId,
                amount = expense.amount,
                category = expense.category,
                note = expense.note,
                date = expense.date,
                pendingOperation = pendingOperation
            )
    }
}
