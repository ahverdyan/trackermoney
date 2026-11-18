package com.example.trackermoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HistoryAccent = Color(0xFF6275ED)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    sortedExpenses: List<Expense>,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Text(
            text = "История операций",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Всего операций: ${sortedExpenses.size}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(20.dp))

        if (sortedExpenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🧾", fontSize = 42.sp)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Пока нет операций",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Добавленные расходы появятся здесь",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(sortedExpenses) { expense ->
                    var showActions by remember(expense.id) { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = { showActions = true },
                            onLongClick = { showActions = true }
                        ),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        HistoryAccent.copy(alpha = 0.12f),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = categoryIcon(expense.category),
                                    color = HistoryAccent,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = expense.note.ifBlank {
                                        expense.category
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = expense.category,
                                    color = HistoryAccent,
                                    fontSize = 13.sp
                                )

                                val timeFormat = SimpleDateFormat("HH:mm", Locale("ru", "RU"))
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru", "RU"))
                                val calendar = java.util.Calendar.getInstance().apply {
                                    timeInMillis = expense.date
                                }
                                val hasSavedTime = calendar.get(java.util.Calendar.HOUR_OF_DAY) != 0 ||
                                        calendar.get(java.util.Calendar.MINUTE) != 0 ||
                                        calendar.get(java.util.Calendar.SECOND) != 0
                                Text(
                                    text = if (hasSavedTime) {
                                        "${dateFormat.format(Date(expense.date))}, ${timeFormat.format(Date(expense.date))}"
                                    } else {
                                        dateFormat.format(Date(expense.date))
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = String.format(
                                    Locale("ru", "RU"),
                                    "-%,.2f ₽",
                                    expense.amount
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )

                        }
                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Изменить") },
                                onClick = { showActions = false; onEdit(expense) }
                            )
                            DropdownMenuItem(
                                text = { Text("Удалить") },
                                onClick = { showActions = false; onDelete(expense) }
                            )
                        }
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
