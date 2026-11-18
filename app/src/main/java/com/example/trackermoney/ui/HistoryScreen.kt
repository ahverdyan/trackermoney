package com.example.trackermoney.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.model.Expense
import com.example.trackermoney.R
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    sortedExpenses: List<Expense>,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit
) {
    val historyAccent = androidx.compose.ui.res.colorResource(R.color.brand_accent)
    val currentLocale = LocalConfiguration.current.locales[0]
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.history_total_operations, sortedExpenses.size),
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
                    Image(
                        painter = painterResource(R.drawable.ic_expense_receipt),
                        contentDescription = stringResource(R.string.expense_receipt_description),
                        modifier = Modifier.size(48.dp),
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.primary)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.history_empty_title),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = stringResource(R.string.history_empty_hint),
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
                    val expenseTitle = if (expense.note.isBlank()) {
                        localizedCategoryName(expense.category)
                    } else {
                        expense.note
                    }
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
                                        historyAccent.copy(alpha = 0.12f),
                                        RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = categoryIcon(expense.category),
                                    color = historyAccent,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = expenseTitle,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    text = localizedCategoryName(expense.category),
                                    color = historyAccent,
                                    fontSize = 13.sp
                                )

                                val timeFormat = SimpleDateFormat("HH:mm", currentLocale)
                                val dateFormat = SimpleDateFormat("dd.MM.yyyy", currentLocale)
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
                                    currentLocale,
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
                                text = { Text(stringResource(R.string.edit)) },
                                onClick = { showActions = false; onEdit(expense) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.delete)) },
                                onClick = { showActions = false; onDelete(expense) }
                            )
                        }
                    }
                }
            }
        }
    }
}
