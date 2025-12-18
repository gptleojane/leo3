package com.example.leo3.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leo3.databinding.CategoryItemBinding
import com.example.leo3.data.model.CategoryItem

class CategoryAdapter(
    private val list: List<CategoryItem>,
    defaultSelectedPos: Int,
    private val onSelect: (CategoryItem) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var selectedPos = defaultSelectedPos.coerceAtMost(list.size - 1)

    // 只解析一次，效能最佳
    private val highlightColor = Color.parseColor("#FFD54F")

    inner class ViewHolder(val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoryItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val b = holder.binding

        // 顯示分類名稱
        b.tvName.text = item.name

        // 背景選中效果
        if (position == selectedPos) {
            b.cardCategory.setCardBackgroundColor(highlightColor)
        } else {
            b.cardCategory.setCardBackgroundColor(Color.TRANSPARENT)
        }

        // 點擊事件
        b.cardCategory.setOnClickListener {

            if (position == selectedPos) return@setOnClickListener

            val oldPos = selectedPos
            selectedPos = position

            // 更新舊、新兩個 item（避免大面積刷新）
            if (oldPos != -1) notifyItemChanged(oldPos)
            notifyItemChanged(position)

            onSelect(item) // 回傳選到的分類資料
        }
    }
}
