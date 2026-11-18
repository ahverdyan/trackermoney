package com.example.trackermoney.model

data class Expense(
    val id: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val category: String,
    val note: String,
    val date: Long = System.currentTimeMillis()
)
