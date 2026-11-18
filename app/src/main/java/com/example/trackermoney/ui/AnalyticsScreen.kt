package com.example.trackermoney.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.domain.AnalyticsPeriod
import com.example.trackermoney.domain.ExpenseChartData
import java.util.Locale

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
fun AnalyticsScreen(
    data: ExpenseChartData,
    selectedPeriod: AnalyticsPeriod,
    onPeriodChange: (AnalyticsPeriod) -> Unit
) {
    val categories = data.categories
    val buckets = data.buckets
    val amounts = data.amounts

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
                AnalyticsPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodChange(period) },
                        label = { Text(period.title) }
                    )
                }
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Потрачено за выбранный период", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(Locale("ru", "RU"), "%,.2f ₽", data.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
                                    val height = (amount / data.maxBucketTotal).toFloat() * size.height * 0.88f
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
