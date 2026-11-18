package com.example.trackermoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.trackermoney.model.Expense

private data class ExpenseCategory(
    val name: String,
    val emoji: String,
    val color: Color
)

private val addCategories = listOf(
    ExpenseCategory("Продукты", "🍎", Color(0xFFF59E0B)),
    ExpenseCategory("Транспорт", "🚌", Color(0xFF10B981)),
    ExpenseCategory("Покупки", "🛍️", Color(0xFF3B82F6)),
    ExpenseCategory("Развлечения", "🎬", Color(0xFF8B5CF6)),
    ExpenseCategory("Жильё", "🏠", Color(0xFFEF4444)),
    ExpenseCategory("Здоровье", "💊", Color(0xFF06B6D4)),
    ExpenseCategory("Подписки", "📱", Color(0xFFEC4899)),
    ExpenseCategory("Топливо", "⛽", Color(0xFF64748B)),
    ExpenseCategory("Красота", "💅", Color(0xFFEC4899)),
    ExpenseCategory("Путешествия", "✈️", Color(0xFF3B82F6)),
    ExpenseCategory("Образование", "📚", Color(0xFF8B5CF6))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    onSave: (Double, String, String, Long) -> Unit,
    expenseToEdit: Expense? = null
) {
    var amount by remember(expenseToEdit?.id) { mutableStateOf(expenseToEdit?.amount?.toString() ?: "") }
    var title by remember(expenseToEdit?.id) { mutableStateOf(expenseToEdit?.note ?: "") }
    var selectedCategory by remember(expenseToEdit?.id) { mutableStateOf(expenseToEdit?.category ?: "Продукты") }

    // состояние даты
    var selectedDateMillis by remember(expenseToEdit?.id) { mutableLongStateOf(expenseToEdit?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedAmount = amount
        .replace(',', '.')
        .toDoubleOrNull()

    val colors = MaterialTheme.colorScheme
    // Определяем активную тему по цвету фона, чтобы учитывать и ручное переключение в настройках.
    val isDarkTheme = colors.background.luminance() < 0.5f

    val dateFormatter = remember { SimpleDateFormat("d MMMM yyyy", Locale("ru", "RU")) }

    // диалог календаря
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDateMillis = millis
                        }
                        showDatePicker = false
                    }
                ) { Text("ОК") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // колонка с плитками
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) colors.background else Color.White)
            .padding(horizontal = 20.dp)
    ) {
        // заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "‹",
                fontSize = 36.sp,
                color = colors.onBackground,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(end = 22.dp)
            )
            Text(
                if (expenseToEdit == null) "Добавить расход" else "Изменить расход",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Прокручиваемое содержимое
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // сумма
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDarkTheme) colors.surfaceVariant else Color(0xFFF5F4FF),
                        RoundedCornerShape(22.dp)
                    )
                    .border(
                        1.dp,
                        if (isDarkTheme) colors.outlineVariant else Color(0xFFDFDCFF),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    "Сумма",
                    color = if (isDarkTheme) colors.onSurfaceVariant else Color(0xFF737887),
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₽",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(12.dp))

                    BasicTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        textStyle = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            Box {
                                if (amount.isEmpty()) {
                                    Text(
                                        "0,00",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) colors.onSurfaceVariant else Color(0xFFB9BBC5)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // информация
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Информация о расходе",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) colors.onSurfaceVariant else Color(0xFF555967)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Например: Обед в кафе") },
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true
                )
            }

            // дата
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Дата",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) colors.onSurfaceVariant else Color(0xFF555967)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },   // ← открывает календарь
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkTheme) colors.outlineVariant else Color(0xFFE1E2E8)
                    ),
                    color = if (isDarkTheme) colors.surface else Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📅", fontSize = 22.sp)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            dateFormatter.format(Date(selectedDateMillis))
                                .replaceFirstChar { it.uppercase() },
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )
                        Text("›", fontSize = 28.sp, color = colors.onSurfaceVariant)
                    }
                }
            }

            // категории
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Категория",
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) colors.onSurfaceVariant else Color(0xFF555967)
                )

                addCategories.chunked(3).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { category ->
                            val selected = selectedCategory == category.name

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(112.dp)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) Color(0xFF6275ED)
                                        else if (isDarkTheme) colors.outlineVariant else Color(0xFFD8DAE1),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .background(
                                        when {
                                            selected && isDarkTheme -> colors.primaryContainer
                                            selected -> Color(0xFFF2F1FF)
                                            isDarkTheme -> colors.surface
                                            else -> Color.White
                                        },
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedCategory = category.name }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            category.color.copy(alpha = 0.14f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(category.emoji, fontSize = 21.sp)
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    category.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold
                                    else FontWeight.Normal,
                                    color = if (selected && isDarkTheme) colors.onPrimaryContainer
                                    else if (isDarkTheme) colors.onSurface else Color(0xFF292C38),
                                    maxLines = 1
                                )
                            }
                        }

                        repeat(3 - rowCategories.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // кнопка сохранения
        Button(
            onClick = {
                if (parsedAmount != null && parsedAmount > 0) {
                    onSave(
                        parsedAmount,
                        selectedCategory,
                        title.trim(),
                        selectedDateMillis
                    )
                }
            },
            enabled = parsedAmount != null && parsedAmount > 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 12.dp)
                .height(62.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6860F5)
            )
        ) {
            Text(
                if (expenseToEdit == null) "Сохранить" else "Сохранить изменения",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
