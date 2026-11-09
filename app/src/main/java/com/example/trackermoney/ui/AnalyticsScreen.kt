package com.example.trackermoney.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.model.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class ChartBucket(val label: String, val start: Long, val end: Long)

private val categoryColors = mapOf(
    "Продукты" to Color(0xFFF59E0B), "Транспорт" to Color(0xFF10B981),
    "Покупки" to Color(0xFF3B82F6), "Развлечения" to Color(0xFF8B5CF6),
    "Жильё" to Color(0xFFEF4444), "Здоровье" to Color(0xFF06B6D4),
    "Подписки" to Color(0xFFEC4899), "Топливо" to Color(0xFF64748B),
    "Красота" to Color(0xFFE11D8D), "Путешествия" to Color(0xFF2563EB),
    "Образование" to Color(0xFF9333EA)
)

private fun categoryChartColor(category: String, index: Int): Color =
    categoryColors[category] ?: listOf(
        Color(0xFF14B8A6), Color(0xFFF97316), Color(0xFF84CC16),
        Color(0xFFEC4899), Color(0xFF6366F1)
    )[index % 5]

@Composable
fun AnalyticsScreen(expenses: List<Expense>) {
    var selectedPeriod by remember { mutableStateOf("Неделя") }
    val categories = expenses.map { it.category }.distinct().sorted()
    val buckets = remember(selectedPeriod) { createBuckets(selectedPeriod) }
    val amounts = buckets.map { bucket ->
        categories.map { category ->
            expenses.filter { it.category == category && it.date >= bucket.start && it.date < bucket.end }
                .sumOf { it.amount }
        }
    }
    val maxBucketTotal = amounts.maxOfOrNull { row -> row.sum() }?.coerceAtLeast(1.0) ?: 1.0
    val displayedTotal = amounts.sumOf { it.sum() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Аналитика", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Сравнение расходов по категориям", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Неделя", "Месяц", "Год").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) }
                    )
                }
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Потрачено за выбранный период", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale("ru", "RU"), "%,.2f ₽", displayedTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (categories.isEmpty()) {
                        Text("Добавь расходы, чтобы увидеть диаграмму", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Canvas(Modifier.fillMaxWidth().height(190.dp)) {
                            val slotWidth = size.width / buckets.size
                            val barWidth = slotWidth * 0.58f
                            amounts.forEachIndexed { bucketIndex, row ->
                                var y = size.height
                                val x = bucketIndex * slotWidth + (slotWidth - barWidth) / 2f
                                row.forEachIndexed { categoryIndex, amount ->
                                    val height = (amount / maxBucketTotal).toFloat() * size.height * 0.88f
                                    if (height > 0f) {
                                        y -= height
                                        drawRect(
                                            color = categoryChartColor(categories[categoryIndex], categoryIndex),
                                            topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                            size = androidx.compose.ui.geometry.Size(barWidth, height)
                                        )
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth()) {
                            buckets.forEach { bucket ->
                                Text(
                                    text = bucket.label,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            if (categories.isNotEmpty()) {
                Text("Категории", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                categories.chunked(2).forEach { rowCategories ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowCategories.forEach { category ->
                            val index = categories.indexOf(category)
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(12.dp).background(categoryChartColor(category, index), CircleShape))
                                Spacer(Modifier.width(6.dp))
                                Text(category, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                        if (rowCategories.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun createBuckets(period: String): List<ChartBucket> {
    val today = Calendar.getInstance().apply { clearTime() }
    return when (period) {
        "Неделя" -> (6 downTo 0).map { daysBack ->
            val start = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -daysBack) }
            val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            ChartBucket(SimpleDateFormat("EE", Locale("ru", "RU")).format(start.time), start.timeInMillis, end.timeInMillis)
        }
        "Месяц" -> (3 downTo 0).map { weeksBack ->
            val start = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -weeksBack * 7 - 6) }
            val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
            ChartBucket(SimpleDateFormat("dd.MM", Locale("ru", "RU")).format(start.time), start.timeInMillis, end.timeInMillis)
        }
        else -> (11 downTo 0).map { monthsBack ->
            val start = (today.clone() as Calendar).apply {
                add(Calendar.MONTH, -monthsBack)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            ChartBucket(SimpleDateFormat("MMM", Locale("ru", "RU")).format(start.time), start.timeInMillis, end.timeInMillis)
        }
    }
}

private fun Calendar.clearTime() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}
