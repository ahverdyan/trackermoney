package com.example.trackermoney.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackermoney.domain.HomeExpenseSummary
import com.example.trackermoney.R
import java.util.Locale

private fun formatMoney(value: Double, locale: Locale): String =
    String.format(locale, "%,.2f ₽", value)

@Composable
fun HomeScreen(
    summary: HomeExpenseSummary,
    monthlyBudget: Double,
    onAddExpense: () -> Unit
) {
    val total = summary.todayTotal
    val colors = MaterialTheme.colorScheme
    val grouped = summary.categories
    val currentLocale = LocalConfiguration.current.locales[0]
    val groupedColors = grouped.map { colorResource(categoryColorResource(it.category)) }

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
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            MonthlyBudgetCard(
                monthSpent = summary.monthTotal,
                budget = monthlyBudget
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

                        grouped.forEachIndexed { index, entry ->
                            val sweep =
                                (entry.amount / total * 360.0).toFloat()

                            drawArc(
                                color = groupedColors[index],
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
                        text = stringResource(R.string.home_spent_today),
                        color = colors.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = formatMoney(total, currentLocale),
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
                text = stringResource(R.string.home_top_categories),
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
                        Image(
                            painter = painterResource(R.drawable.ic_expense_receipt),
                            contentDescription = stringResource(R.string.expense_receipt_description),
                            modifier = Modifier.size(40.dp),
                            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colors.primary)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.home_no_expenses),
                            color = colors.onSurface
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.home_add_first_expense),
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
                val color = colorResource(categoryColorResource(category))

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
                                text = localizedCategoryName(category),
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
                            text = formatMoney(amount, currentLocale),
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

@Composable
private fun MonthlyBudgetCard(monthSpent: Double, budget: Double) {
    val colors = MaterialTheme.colorScheme
    val currentLocale = LocalConfiguration.current.locales[0]
    val progress = if (budget > 0.0) (monthSpent / budget).toFloat().coerceIn(0f, 1f) else 0f
    val remaining = budget - monthSpent
    val progressColor = if (remaining < 0.0) colors.error else colors.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.budget_month_title),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = if (budget > 0.0) {
                        if (remaining >= 0.0) stringResource(R.string.budget_remaining, formatMoney(remaining, currentLocale))
                        else stringResource(R.string.budget_exceeded, formatMoney(-remaining, currentLocale))
                    } else {
                        stringResource(R.string.budget_not_set)
                    },
                    color = if (remaining < 0.0) colors.error else colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (budget > 0.0) {
                    stringResource(R.string.budget_month_progress, formatMoney(monthSpent, currentLocale), formatMoney(budget, currentLocale))
                } else {
                    stringResource(R.string.budget_month_spent, formatMoney(monthSpent, currentLocale))
                },
                color = colors.onSurfaceVariant,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = colors.surfaceVariant
            )
            if (budget <= 0.0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.budget_set_in_settings),
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
