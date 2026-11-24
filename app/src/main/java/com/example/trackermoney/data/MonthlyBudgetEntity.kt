package com.example.trackermoney.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_budgets")
data class MonthlyBudgetEntity(
    @PrimaryKey val ownerUserId: String,
    val amount: Double,
    val pendingUpload: Boolean = false
)
