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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.toArgb
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    val colors = MaterialTheme.colorScheme
    val rowColors = ExpenseRowColors(
        card = colors.surface.toArgb(),
        primaryText = colors.onSurface.toArgb(),
        secondaryText = colors.onSurfaceVariant.toArgb(),
        accentText = historyAccent.toArgb()
    )
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
            AndroidView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                factory = { context ->
                    RecyclerView(context).apply {
                        layoutManager = LinearLayoutManager(context)
                        setBackgroundColor(colors.background.toArgb())
                        adapter = ExpenseRecyclerAdapter(onEdit, onDelete, rowColors).also { it.submitList(sortedExpenses) }
                        clipToPadding = false
                        setPadding(0, 0, 0, 20)
                    }
                },
                update = { recycler ->
                    recycler.setBackgroundColor(colors.background.toArgb())
                    (recycler.adapter as? ExpenseRecyclerAdapter)?.apply {
                        updateColors(rowColors)
                        submitList(sortedExpenses)
                    }
                }
            )
        }
    }
}
