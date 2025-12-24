package com.example.leo3.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.leo3.R
import com.example.leo3.data.model.CategoryStatItem

class StatCategoryAdapter(
    private val list: List<CategoryStatItem>,
    private val onItemClick: (CategoryStatItem) -> Unit
) : RecyclerView.Adapter<StatCategoryAdapter.VH>() {
    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemCategoryName)
        val amount: TextView = view.findViewById(R.id.itemCategoryAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stat_category, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.name.text = item.categoryName
        holder.amount.text = "$${item.totalAmount}"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = list.size
}
