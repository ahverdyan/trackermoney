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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trackermoney.domain.AnalyticsPeriod
import com.example.trackermoney.domain.ExpenseChartData
import com.example.trackermoney.R
import java.util.Locale

@Composable
fun AnalyticsScreen(
    data: ExpenseChartData,
    selectedPeriod: AnalyticsPeriod,
    onPeriodChange: (AnalyticsPeriod) -> Unit
) {
    val categories = data.categories
    val buckets = data.buckets
    val amounts = data.amounts
    val currentLocale = LocalConfiguration.current.locales[0]
    val chartColorResources = categories.mapIndexed { index, category ->
        val fallbackColors = listOf(
            R.color.category_extra_teal,
            R.color.category_extra_orange,
            R.color.category_extra_lime,
            R.color.category_extra_pink,
            R.color.category_extra_indigo
        )
        if (categoryNameResource(category) == R.string.category_other_legacy) {
            fallbackColors[index % fallbackColors.size]
        } else {
            categoryColorResource(category)
        }
    }
    val chartColors = chartColorResources.map { colorId -> colorResource(colorId) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(stringResource(R.string.analytics_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.analytics_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnalyticsPeriod.values().forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { onPeriodChange(period) },
                        label = {
                            Text(
                                stringResource(
                                    when (period) {
                                        AnalyticsPeriod.WEEK -> R.string.period_week
                                        AnalyticsPeriod.MONTH -> R.string.period_month
                                        AnalyticsPeriod.YEAR -> R.string.period_year
                                    }
                                )
                            )
                        }
                    )
                }
            }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.analytics_period_spending), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(String.format(currentLocale, "%,.2f ₽", data.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (categories.isEmpty()) {
                        Text(stringResource(R.string.analytics_add_expenses_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                            color = chartColors[categoryIndex],
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
                Text(stringResource(R.string.categories_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                val legendItems = categories.mapIndexed { index, category ->
                    CategoryLegendItem(
                        category = category,
                        amount = amounts.sumOf { it.getOrElse(index) { 0.0 } },
                        colorResource = chartColorResources[index]
                    )
                }
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((((categories.size + 1) / 2) * 36).dp),
                    factory = { context ->
                        RecyclerView(context).apply {
                            layoutManager = GridLayoutManager(context, 2)
                            adapter = CategoryLegendAdapter().also { it.submitItems(legendItems) }
                            isNestedScrollingEnabled = false
                        }
                    },
                    update = { recycler ->
                        (recycler.adapter as? CategoryLegendAdapter)?.submitItems(legendItems)
                    }
                )
            }
        }
    }
}
