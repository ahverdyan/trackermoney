package com.example.trackermoney.model

data class Expense(
    val id: Long = System.currentTimeMillis(),
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long = System.currentTimeMillis()
)