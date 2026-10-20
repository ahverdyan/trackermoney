package com.example.trackermoney.model

data class Expense(
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long = System.currentTimeMillis()
)
