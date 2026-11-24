package com.example.trackermoney.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.trackermoney.R
import com.example.trackermoney.model.Expense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** RecyclerView adapter for the expense list; tapping a row opens its actions. */
class ExpenseRecyclerAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseRecyclerAdapter.ExpenseViewHolder>(ExpenseDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder =
        ExpenseViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false))

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(expense: Expense) {
            val context = itemView.context
            val categoryName = context.getString(categoryNameResource(expense.category))
            itemView.findViewById<TextView>(R.id.category_icon).text = categoryIcon(expense.category)
            itemView.findViewById<TextView>(R.id.expense_title).text =
                expense.note.ifBlank { categoryName }
            itemView.findViewById<TextView>(R.id.expense_category).text = categoryName
            itemView.findViewById<TextView>(R.id.expense_date).text =
                SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(expense.date))
            itemView.findViewById<TextView>(R.id.expense_amount).text =
                String.format(Locale.getDefault(), "-%,.2f ₽", expense.amount)

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
