package com.example.leo3.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.FragmentHomeBinding
import com.example.leo3.databinding.HomeItemBillBinding
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

        loadTodaySummary(account, year, month, day)

        loadTodayLatestThree(account, year, month, day)


    }

    private fun loadTodaySummary(account: String, year: Int, month: Int, day: Int) {

        FirestoreHelper.getBillsByMonth(account, year, month) { monthBills ->

            val todayBills = monthBills.filter { it.day == day }

            val incomeTotal = todayBills
                .filter { it.type == "income" }
                .sumOf { it.amount }

            val expenseTotal = todayBills
                .filter { it.type == "expense" }
                .sumOf { it.amount }

            val totalCount = todayBills.size

            val balance = incomeTotal - expenseTotal

            binding.homeExpenseAmount.text = "$ $expenseTotal"
            binding.homeIncomeAmount.text = "$ $incomeTotal"
            binding.homeExpenseLabel.text = "$ $expenseTotal"
            binding.homeIncomeLabel.text = "$ $incomeTotal"
            binding.homeBalanceLabel.text = "$ $balance"

            if (totalCount == 0) {
                binding.homeRecordHint.text = "今日還沒記帳哦 !"
            } else {
                binding.homeRecordHint.text = "今日已記帳 ${totalCount} 筆"
            }

            // ===== STEP 3：更新收支比例條（Guideline） =====

            val totalAmount = incomeTotal + expenseTotal

            val expensePercent = if (totalAmount == 0L) {
                0.5f   // 沒有任何帳時，分界線置中
            } else {
                expenseTotal.toFloat() / totalAmount.toFloat()
            }

            // 動態更新 Guideline 位置
            val params = binding.homeBarGuideline.layoutParams
                    as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams

            params.guidePercent = expensePercent
            binding.homeBarGuideline.layoutParams = params



        }
    }
    private fun loadTodayLatestThree(account: String, year: Int, month: Int, day: Int) {

        // 1️⃣ 先清空（避免重新進頁時重複加）
        binding.homeLatest3Container.removeAllViews()

        // 2️⃣ 先抓分類（為了顯示分類名稱）
        FirestoreHelper.getAllCategories(account) { categories ->

            // categoryId -> CategoryItem
            val categoryMap = categories.associateBy { it.id }

            // 3️⃣ 再抓今天最新三筆
            FirestoreHelper.getTodayThreeBills(account, year, month, day) { bills ->
                android.util.Log.d("HomeFragment", "Today bills size = ${bills.size}")

                if (bills.isEmpty()) {
                    // 今天沒帳 → 整塊隱藏
                    binding.homeRecentTitle.visibility = View.GONE
                    binding.homeLatest3Container.visibility = View.GONE
                    return@getTodayThreeBills
                }

                binding.homeRecentTitle.visibility = View.VISIBLE
                binding.homeLatest3Container.visibility = View.VISIBLE

                // 4️⃣ 一筆一筆用 ViewBinding 塞進來
                for (bill in bills) {

                    // ★ 用 ViewBinding inflate item
                    val itemBinding = HomeItemBillBinding.inflate(
                        layoutInflater,
                        binding.homeLatest3Container,
                        false
                    )

                    // ===== 沿用 RecordFragment Adapter 的邏輯 =====

                    // 1. 分類文字
                    val categoryName =
                        categoryMap[bill.categoryId]?.name ?: "未分類"
                    itemBinding.homeBillCategory.text = categoryName

                    // 2. note（空白就顯示空）
                    itemBinding.homeBillName.text = bill.note.ifBlank { "" }

                    // 3. 金額
                    itemBinding.homeBillAmount.text = "$${bill.amount}"

                    // 4. 根據支出 / 收入 套背景
                    val bgRes = if (bill.type == "expense") {
                        R.drawable.bg_expense_circle
                    } else {
                        R.drawable.bg_income_circle
                    }
                    itemBinding.homeBillIcon.setBackgroundResource(bgRes)

                    // 5️⃣ 加進容器
                    binding.homeLatest3Container.addView(itemBinding.root)
                }
            }
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
