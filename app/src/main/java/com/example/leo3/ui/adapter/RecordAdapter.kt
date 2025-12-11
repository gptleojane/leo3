package com.example.leo3.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.leo3.R
import com.example.leo3.databinding.HomeItemHeaderBinding
import com.example.leo3.databinding.HomeItemBillBinding
import com.example.leo3.data.model.Bill
import com.example.leo3.data.model.RecordUiModel

class RecordAdapter(
    private val list: List<RecordUiModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is RecordUiModel.Header -> TYPE_HEADER
            is RecordUiModel.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_HEADER -> {
                val binding = HomeItemHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            else -> {
                val binding = HomeItemBillBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = list[position]) {

            is RecordUiModel.Header ->
                (holder as HeaderViewHolder).bind(item)

            is RecordUiModel.Item ->
                (holder as ItemViewHolder).bind(item.bill)
        }
    }

    // ------------------------
    //   Header ViewHolder
    // ------------------------
    inner class HeaderViewHolder(
        private val binding: HomeItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: RecordUiModel.Header) {
            binding.homeHeaderDate.text = "${data.date} 星期${weekName(data.weekDay)}"
            binding.homeHeaderTotal.text =
                if (data.totalAmount >= 0) "$${data.totalAmount}"
                else "$-${kotlin.math.abs(data.totalAmount)}"
        }
    }

    // ------------------------
    //   Item ViewHolder
    // ------------------------
    inner class ItemViewHolder(
        private val binding: HomeItemBillBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bill: Bill) {

            // ★ 1. 左邊分類文字
            binding.homeBillCategory.text = bill.categoryName.ifBlank { "?" }

            // ★ 2. note 空白 → 顯示空白（不顯示 categoryId）
            binding.homeBillName.text = bill.note.ifBlank { "" }

            // ★ 3. 金額
            binding.homeBillAmount.text = "$${bill.amount}"

            // ★ 4. 根據支出 / 收入 套不同背景顏色
            val bgRes = if (bill.type == "expense") {
                R.drawable.bg_expense_circle   // 你 XML 做的那兩個
            } else {
                R.drawable.bg_income_circle
            }

            binding.homeBillIcon.setBackgroundResource(bgRes)
        }
    }

    private fun weekName(day: Int): String {
        return when (day) {
            1 -> "日"
            2 -> "一"
            3 -> "二"
            4 -> "三"
            5 -> "四"
            6 -> "五"
            7 -> "六"
            else -> ""
        }
    }
}
