package com.example.trackermoney.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.model.Expense
import java.util.Locale
import androidx.compose.foundation.clickable

private val expenseCategories = listOf(
    "Продукты", "Транспорт", "Покупки",
    "Развлечения", "Жильё", "Здоровье",
    "Подписки", "Другое"
)

private val expenseColors = listOf(
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF3B82F6),
    Color(0xFF8B5CF6),
    Color(0xFFEF4444),
    Color(0xFF06B6D4),
    Color(0xFFEC4899),
    Color(0xFF64748B)
)

private fun expenseColor(category: String): Color {
    val index = expenseCategories.indexOf(category)
    return expenseColors[if (index < 0) 7 else index]
}

private fun formatMoney(value: Double): String =
    String.format(Locale("ru", "RU"), "%,.2f ₽", value)

@Composable
fun HomeScreen(
    expenses: List<Expense>,
    onAddExpense: () -> Unit
) {
    val total = expenses.sumOf { it.amount }

    val grouped = expenses
        .groupBy { it.category }
        .mapValues { (_, values) -> values.sumOf { it.amount } }
        .entries
        .sortedByDescending { it.value }

    var selectedPeriod by remember { mutableStateOf("Месяц") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC)),
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 24.dp,
            bottom = 24.dp
        )
    ) {
        // заголовок
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Трекер расходов",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF171923)
                )
//                Box(
//                    modifier = Modifier
//                        .size(44.dp)
//                        .background(Color.White, CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("👤", fontSize = 20.sp)
//                }
            }
        }

        // период
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEDEFF5), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("День", "Неделя", "Месяц", "Год").forEach { period ->
                    val selected = period == selectedPeriod
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) Color.White else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            period,
                            color = if (selected) Color(0xFF171923) else Color(0xFF737887),
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // диаграмма
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
                            val sweep = (entry.value / total * 360.0).toFloat()
                            drawArc(
                                color = expenseColor(entry.key),
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
                            color = Color(0xFFE9EBF3),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 36.dp.toPx())
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Всего потрачено",
                        color = Color(0xFF737887),
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        formatMoney(total),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171923)
                    )
                }
            }
        }

        // расход по категориям
        item {
            Spacer(Modifier.height(25.dp))
            Text(
                "Топ категорий",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF171923)
            )
            Spacer(Modifier.height(12.dp))
        }

        // плитка с категориями
        if (grouped.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
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
                        Text("Пока нет расходов")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Нажми «+», чтобы добавить первую покупку",
                            color = Color(0xFF737887)
                        )
                    }
                }
            }
        } else {
            items(grouped) { entry ->
                val category = entry.key
                val amount = entry.value
                val percent = if (total > 0) (amount / total) else 0.0
                val color = expenseColor(category)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                category.take(1),
                                color = color,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                category,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF171923)
                            )
                            Text(
                                "${(percent * 100).toInt()}%",
                                color = Color(0xFF737887),
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            formatMoney(amount),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF171923)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0xFFEDEFF5), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percent.toFloat().coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                    }
                }
            }
        }
    }
}