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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import com.example.trackermoney.R
import com.example.trackermoney.model.Expense

private data class ExpenseCategory(
    val storageName: String,
    val labelRes: Int,
    val emoji: String,
    val colorRes: Int
)

private val addCategories = listOf(
    ExpenseCategory("Продукты", R.string.category_food, "🍎", R.color.category_food),
    ExpenseCategory("Транспорт", R.string.category_transport, "🚌", R.color.category_transport),
    ExpenseCategory("Покупки", R.string.category_shopping, "🛍️", R.color.category_shopping),
    ExpenseCategory("Развлечения", R.string.category_entertainment, "🎬", R.color.category_entertainment),
    ExpenseCategory("Жильё", R.string.category_housing, "🏠", R.color.category_housing),
    ExpenseCategory("Здоровье", R.string.category_health, "💊", R.color.category_health),
    ExpenseCategory("Подписки", R.string.category_subscriptions, "📱", R.color.category_subscriptions),
    ExpenseCategory("Топливо", R.string.category_fuel, "⛽", R.color.category_fuel),
    ExpenseCategory("Красота", R.string.category_beauty, "💅", R.color.category_beauty),
    ExpenseCategory("Путешествия", R.string.category_travel, "✈️", R.color.category_travel),
    ExpenseCategory("Образование", R.string.category_education, "📚", R.color.category_education)
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

    val currentLocale = LocalConfiguration.current.locales[0]
    val dateFormatter = remember(currentLocale) { SimpleDateFormat("d MMMM yyyy", currentLocale) }

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
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
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
            .background(
                if (isDarkTheme) colors.background else colorResource(R.color.brand_white)
            )
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
                stringResource(if (expenseToEdit == null) R.string.expense_add_title else R.string.expense_edit_title),
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
                        if (isDarkTheme) colors.surfaceVariant else colorResource(R.color.expense_panel),
                        RoundedCornerShape(22.dp)
                    )
                    .border(
                        1.dp,
                        if (isDarkTheme) colors.outlineVariant else colorResource(R.color.expense_panel_border),
                        RoundedCornerShape(22.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    stringResource(R.string.expense_amount),
                    color = if (isDarkTheme) colors.onSurfaceVariant else colorResource(R.color.expense_label),
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
                                        stringResource(R.string.expense_amount_placeholder),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) colors.onSurfaceVariant else colorResource(R.color.expense_placeholder)
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
                    stringResource(R.string.expense_information),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) colors.onSurfaceVariant else colorResource(R.color.expense_section)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.expense_note_hint)) },
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true
                )
            }

            // дата
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.expense_date),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) colors.onSurfaceVariant else colorResource(R.color.expense_section)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },   // ← открывает календарь
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDarkTheme) colors.outlineVariant else colorResource(R.color.expense_date_border)
                    ),
                    color = if (isDarkTheme) colors.surface else colorResource(R.color.brand_white)
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
                    stringResource(R.string.expense_category),
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) colors.onSurfaceVariant else colorResource(R.color.expense_section)
                )

                addCategories.chunked(3).forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { category ->
                            val selected = selectedCategory == category.storageName
                            val categoryColor = colorResource(category.colorRes)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(112.dp)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = if (selected) colorResource(R.color.brand_accent)
                                        else if (isDarkTheme) colors.outlineVariant else colorResource(R.color.expense_tile_border),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .background(
                                        when {
                                            selected && isDarkTheme -> colors.primaryContainer
                                            selected -> colorResource(R.color.expense_tile_selected)
                                            isDarkTheme -> colors.surface
                                            else -> colorResource(R.color.brand_white)
                                        },
                                        RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedCategory = category.storageName }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            categoryColor.copy(alpha = 0.14f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(category.emoji, fontSize = 21.sp, color = categoryColor)
                                }

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    stringResource(category.labelRes),
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold
                                    else FontWeight.Normal,
                                    color = if (selected && isDarkTheme) colors.onPrimaryContainer
                                    else if (isDarkTheme) colors.onSurface else colorResource(R.color.expense_tile_text),
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
                containerColor = colorResource(R.color.brand_button)
            )
        ) {
            Text(
                stringResource(if (expenseToEdit == null) R.string.expense_save else R.string.expense_save_changes),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
