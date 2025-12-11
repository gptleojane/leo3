package com.example.leo3.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.Bill
import com.example.leo3.databinding.FragmentHomeBinding
import com.example.leo3.util.UserManager
import java.util.Calendar
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 顯示使用者名稱
        val account = UserManager.getAccount(requireContext()) ?: ""
        binding.homeUserName.text = account

        // 顯示今天日期
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val week = weekName(cal.get(Calendar.DAY_OF_WEEK))

        binding.homeShowToday.text = "今天是 ${year}年${month}月${day}日 星期${week}"

        // 取得收支資料
        loadTodayBills(account, year, month, day)
    }

    // -----------------------
// 取得今日所有帳單（修正版）
// -----------------------
    private fun loadTodayBills(account: String, year: Int, month: Int, day: Int) {

        // 第 1 步：先抓分類 map（id -> name）
        FirestoreHelper.getAllCategories(account) { categories ->

            val categoryMap = categories.associate { it.id to it.name }

            // 第 2 步：抓今日最新三筆
            FirestoreHelper.getTodayThreeBills(account, year, month, day) { latestThree ->

                // 替最新三筆補上 categoryName
                val latestFixed = latestThree.map { b ->
                    b.categoryName = categoryMap[b.categoryId] ?: ""
                    b
                }

                // 第 3 步：抓這個月全部帳單
                FirestoreHelper.getBillsByMonth(account, year, month) { monthBills ->

                    // 補上分類名稱
                    val fixedBills = monthBills.map { b ->
                        b.categoryName = categoryMap[b.categoryId] ?: ""
                        b
                    }

                    val todayBills = fixedBills.filter { it.day == day }

                    val totalExpense = todayBills.filter { it.type == "expense" }.sumOf { it.amount }
                    val totalIncome = todayBills.filter { it.type == "income" }.sumOf { it.amount }
                    val balance = totalIncome - totalExpense

                    // 更新數字
                    binding.homeExpenseAmount.text = "$$totalExpense"
                    binding.homeIncomeAmount.text = "$$totalIncome"
                    binding.homeBalanceLabel.text = "$balance"

                    // 更新提示文字
                    binding.homeRecordHint.text = if (todayBills.isEmpty()) {
                        "今日還沒有記帳喔！"
                    } else {
                        "今日已記帳 ${todayBills.size} 筆"
                    }

                    // 更新比例條
                    updateBar(totalExpense, totalIncome)

                    // 更新最新三筆
                    showLatestThree(latestFixed)
                }
            }
        }
    }


    // -----------------------
    // 更新收支比例條
    // -----------------------
    private fun updateBar(expense: Int, income: Int) {
        val total = expense + income
        val percent = if (total == 0) 0.5f else expense.toFloat() / total

        val params =
            binding.homeBarGuideline.layoutParams as ConstraintLayout.LayoutParams

        params.guidePercent = percent
        binding.homeBarGuideline.layoutParams = params
    }



    // -----------------------
    // 顯示今日最新三筆（非 RecyclerView）
    // -----------------------
    private fun showLatestThree(list: List<Bill>) {
        val container = binding.homeLatest3Container
        container.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())

        list.forEach { bill ->
            val view = inflater.inflate(com.example.leo3.R.layout.item_home_latest3, container, false)

            view.findViewById<TextView>(com.example.leo3.R.id.itemLatestName).text =
                bill.note.ifBlank { bill.categoryName }
            view.findViewById<TextView>(com.example.leo3.R.id.itemLatestAmount).text =
                if (bill.type == "expense") "-${bill.amount}" else "+${bill.amount}"

            container.addView(view)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
