package com.example.trackermoney.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trackermoney.R
import com.example.trackermoney.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseRecyclerAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit,
    private var rowColors: ExpenseRowColors
) : ListAdapter<Expense, ExpenseRecyclerAdapter.ExpenseViewHolder>(ExpenseDiffCallback) {

    fun updateColors(newColors: ExpenseRowColors) {
        if (rowColors == newColors) return
        rowColors = newColors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder =
        ExpenseViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false))

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(expense: Expense) {
            val context = itemView.context
            val categoryName = context.getString(categoryNameResource(expense.category))
            (itemView as MaterialCardView).setCardBackgroundColor(rowColors.card)
            itemView.findViewById<TextView>(R.id.category_icon).text = categoryIcon(expense.category)
            itemView.findViewById<TextView>(R.id.expense_title).apply {
                setTextColor(rowColors.primaryText)
                text = expense.note.ifBlank { categoryName }
            }
            itemView.findViewById<TextView>(R.id.expense_category).apply {
                setTextColor(rowColors.accentText)
                text = categoryName
            }
            itemView.findViewById<TextView>(R.id.expense_date).apply {
                setTextColor(rowColors.secondaryText)
                text = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(expense.date))
            }
            itemView.findViewById<TextView>(R.id.expense_amount).apply {
                setTextColor(rowColors.primaryText)
                text = String.format(Locale.getDefault(), "-%,.2f ₽", expense.amount)
            }

            val showActions = View.OnClickListener { anchor ->
                PopupMenu(context, anchor).apply {
                    menuInflater.inflate(R.menu.menu_expense_actions, menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.action_edit -> onEdit(expense)
                            R.id.action_delete -> onDelete(expense)
                            else -> return@setOnMenuItemClickListener false
                        }
                        true
                    }
                    show()
                }
            }
            itemView.setOnClickListener(showActions)
            itemView.setOnLongClickListener { showActions.onClick(itemView); true }
        }
    }

    private companion object {
        val ExpenseDiffCallback = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
        }
    }
}

data class ExpenseRowColors(
    val card: Int,
    val primaryText: Int,
    val secondaryText: Int,
    val accentText: Int
)
