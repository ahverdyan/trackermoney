package com.example.trackermoney.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackermoney.R

data class CategoryLegendItem(val category: String, val amount: Double, val colorResource: Int)

class CategoryLegendAdapter(private var labelColor: Int) : RecyclerView.Adapter<CategoryLegendAdapter.LegendViewHolder>() {
    private var items: List<CategoryLegendItem> = emptyList()

    fun submitItems(newItems: List<CategoryLegendItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateLabelColor(newColor: Int) {
        if (labelColor == newColor) return
        labelColor = newColor
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendViewHolder =
        LegendViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_category_legend, parent, false))

    override fun onBindViewHolder(holder: LegendViewHolder, position: Int) = holder.bind(items[position], labelColor)
    override fun getItemCount(): Int = items.size

    class LegendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: CategoryLegendItem, labelColor: Int) {
            val context = itemView.context
            itemView.findViewById<View>(R.id.category_color_dot)
                .backgroundTintList = context.getColorStateList(item.colorResource)
            itemView.findViewById<TextView>(R.id.category_label).apply {
                setTextColor(labelColor)
                text = "${context.getString(categoryNameResource(item.category))} · ${String.format(java.util.Locale.getDefault(), "%,.2f ₽", item.amount)}"
            }
        }
    }
}
