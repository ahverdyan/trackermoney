package com.example.trackermoney.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.domain.HomeExpenseSummary
import java.util.Locale

private val expenseCategories = listOf(
    "Продукты", "Транспорт", "Покупки",
    "Развлечения", "Жильё", "Здоровье",
    "Подписки", "Топливо", "Красота", "Путешествия", "Образование"
)

private val expenseColors = listOf(
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFFEF4444),
    Color(0xFF06B6D4),
    Color(0xFFEC4899), Color(0xFF64748B), Color(0xFFEC4899),
    Color(0xFF3B82F6), Color(0xFF8B5CF6)
)

private fun expenseColor(category: String): Color {
    val index = expenseCategories.indexOf(category)
    return expenseColors[if (index < 0) 7 else index]
}

private fun formatMoney(value: Double): String =
    String.format(Locale("ru", "RU"), "%,.2f ₽", value)

@Composable
fun HomeScreen(
    summary: HomeExpenseSummary,
    onAddExpense: () -> Unit
) {
    val total = summary.todayTotal
    val colors = MaterialTheme.colorScheme
    val grouped = summary.categories

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 24.dp
        )
    ) {
        item {
            Text(
                text = "Трекер расходов",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        }

        item {
            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                if (total > 0) {
                    Canvas(Modifier.size(240.dp)) {
                        var startAngle = -90f

                        grouped.forEach { entry ->
                            val sweep =
                                (entry.amount / total * 360.0).toFloat()

                            drawArc(
                                color = expenseColor(entry.category),
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(
                                    width = 36.dp.toPx(),
                                    cap = StrokeCap.Butt
                                )
                            )

                            startAngle += sweep
                        }
                    }
                } else {
                    Canvas(Modifier.size(240.dp)) {
                        drawArc(
                            color = colors.outlineVariant,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 36.dp.toPx())
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Потрачено сегодня",
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = formatMoney(total),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(25.dp))

            Text(
                text = "Топ категорий",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.onBackground
            )

            Spacer(Modifier.height(12.dp))
        }

        if (grouped.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧾", fontSize = 32.sp)

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Пока нет расходов",
                            color = colors.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Нажми «+», чтобы добавить первую покупку",
                            color = colors.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(grouped) { entry ->
                val category = entry.category
                val amount = entry.amount
                val percent = if (total > 0) amount / total else 0.0
                val color = expenseColor(category)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color.copy(alpha = 0.15f),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categoryIcon(category),
                                color = color,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = category,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = colors.onSurface
                            )

                            Text(
                                text = "${(percent * 100).toInt()}%",
                                color = colors.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = formatMoney(amount),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colors.onSurface
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(
                                colors.surfaceVariant,
                                RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    percent.toFloat().coerceIn(0f, 1f)
                                )
                                .height(6.dp)
                                .background(
                                    color,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

private fun categoryIcon(category: String): String = when (category) {
    "Продукты" -> "🍎"
    "Транспорт" -> "🚌"
    "Покупки" -> "🛍️"
    "Развлечения" -> "🎬"
    "Жильё" -> "🏠"
    "Здоровье" -> "💊"
    "Подписки" -> "📱"
    "Топливо" -> "⛽"
    "Красота" -> "💅"
    "Путешествия" -> "✈️"
    "Образование" -> "📚"
    else -> "✦"
}
