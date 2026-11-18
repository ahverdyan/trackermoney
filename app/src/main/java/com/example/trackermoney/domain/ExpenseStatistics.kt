package com.example.trackermoney.domain

import com.example.trackermoney.model.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class AnalyticsPeriod(val title: String) {
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год")
}

data class CategoryExpenseTotal(
    val category: String,
    val amount: Double
)

data class HomeExpenseSummary(
    val todayTotal: Double = 0.0,
    val categories: List<CategoryExpenseTotal> = emptyList()
)

data class ExpenseChartBucket(
    val label: String,
    val start: Long,
    val end: Long
)

data class ExpenseChartData(
    val categories: List<String> = emptyList(),
    val buckets: List<ExpenseChartBucket> = emptyList(),
    val amounts: List<List<Double>> = emptyList(),
    val total: Double = 0.0,
    val maxBucketTotal: Double = 1.0
)

object ExpenseStatistics {
    fun homeSummary(expenses: List<Expense>, now: Long = System.currentTimeMillis()): HomeExpenseSummary {
        val today = Calendar.getInstance().apply {
            timeInMillis = now
            clearTime()
        }
        val nextDay = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        val todayExpenses = expenses.filter {
            it.date >= today.timeInMillis && it.date < nextDay.timeInMillis
        }
        val categoryTotals = todayExpenses
            .groupBy(Expense::category)
            .map { (category, categoryExpenses) ->
                CategoryExpenseTotal(category, categoryExpenses.sumOf(Expense::amount))
            }
            .sortedByDescending(CategoryExpenseTotal::amount)

        return HomeExpenseSummary(
            todayTotal = todayExpenses.sumOf(Expense::amount),
            categories = categoryTotals
        )
    }

    fun chart(
        expenses: List<Expense>,
        period: AnalyticsPeriod,
        now: Long = System.currentTimeMillis()
    ): ExpenseChartData {
        val categories = expenses.map(Expense::category).distinct().sorted()
        val buckets = createBuckets(period, now)
        val amounts = buckets.map { bucket ->
            categories.map { category ->
                expenses.asSequence()
                    .filter {
                        it.category == category && it.date >= bucket.start && it.date < bucket.end
                    }
                    .sumOf(Expense::amount)
            }
        }
        val bucketTotals = amounts.map(List<Double>::sum)

        return ExpenseChartData(
            categories = categories,
            buckets = buckets,
            amounts = amounts,
            total = bucketTotals.sum(),
            maxBucketTotal = bucketTotals.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        )
    }

    private fun createBuckets(period: AnalyticsPeriod, now: Long): List<ExpenseChartBucket> {
        val today = Calendar.getInstance().apply {
            timeInMillis = now
            clearTime()
        }

        return when (period) {
            AnalyticsPeriod.WEEK -> (6 downTo 0).map { daysBack ->
                val start = (today.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -daysBack)
                }
                val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
                ExpenseChartBucket(
                    label = SimpleDateFormat("EE", Locale("ru", "RU")).format(start.time),
                    start = start.timeInMillis,
                    end = end.timeInMillis
                )
            }

            AnalyticsPeriod.MONTH -> (3 downTo 0).map { weeksBack ->
                val start = (today.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -weeksBack * 7 - 6)
                }
                val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
                ExpenseChartBucket(
                    label = SimpleDateFormat("dd.MM", Locale("ru", "RU")).format(start.time),
                    start = start.timeInMillis,
                    end = end.timeInMillis
                )
            }

            AnalyticsPeriod.YEAR -> (11 downTo 0).map { monthsBack ->
                val start = (today.clone() as Calendar).apply {
                    add(Calendar.MONTH, -monthsBack)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                ExpenseChartBucket(
                    label = SimpleDateFormat("MMM", Locale("ru", "RU")).format(start.time),
                    start = start.timeInMillis,
                    end = end.timeInMillis
                )
            }
        }
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}
