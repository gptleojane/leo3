package com.example.leo3.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.Bill
import com.example.leo3.data.model.CategoryStatItem
import com.example.leo3.databinding.FragmentStatBinding
import com.example.leo3.ui.adapter.StatCategoryAdapter
import com.example.leo3.util.AppFlags
import com.example.leo3.util.UserManager
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class StatFragment : Fragment() {

    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!

    private var currentYear = 0
    private var currentMonth: Int? = null
    private var categoryMap: Map<String, String> = emptyMap()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        reload()

        binding.statIv.setOnClickListener {
            reload()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        childFragmentManager.addOnBackStackChangedListener {
            if (childFragmentManager.backStackEntryCount == 0) {
                binding.statDetailContainer.visibility = View.GONE

                if (AppFlags.reloadData) {
                    AppFlags.reloadData = false
                    reload() // 重新抓 Stat
                }
            }
        }
    }


    fun reload() {
        loadStatData()
    }

    private fun setupUI() {
        val cal = Calendar.getInstance()
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = null

        binding.statCurrentYear.text = "${currentYear}年"
        binding.statCurrentMonth.text = "全部月份"

        binding.statCategoryRecyclerExpense.layoutManager =
            LinearLayoutManager(requireContext())

        binding.statCategoryRecyclerIncome.layoutManager =
            LinearLayoutManager(requireContext())

        binding.statYearSelector.setOnClickListener {
            showYearPopupMenu()
        }

        binding.statMonthSelector.setOnClickListener {
            showMonthPopupMenu()
        }

        binding.statTvHint.setOnClickListener {
            Snackbar.make(
                binding.root,
                "\uD83D\uDCC5 點左上角可回到今天\n" + "✏\uFE0F 點選項目可編輯或刪除\n",
                Snackbar.LENGTH_LONG
            ).show()
        }
        showStatResult(emptyList(), emptyList(), 0, 0)
    }


    private fun loadStatData() {
        val account = UserManager.getAccount(requireContext()) ?: return

        FirestoreHelper.getAllCategories(account) { categories ->

            if (!isAdded || _binding == null) return@getAllCategories

            categoryMap = categories.associate { it.id to it.name }

            //  再抓帳單
            if (currentMonth == null) {
                loadYearBills(account, currentYear)
            } else {
                loadMonthBills(account, currentYear, currentMonth!!)
            }
        }
    }

    private fun loadMonthBills(
        account: String,
        year: Int,
        month: Int
    ) {
        FirestoreHelper.getBillsByMonth(account, year, month) { bills ->
            if (!isAdded || _binding == null) return@getBillsByMonth
            calculateAndShowStat(bills)
        }

    }

    private fun loadYearBills(
        account: String,
        year: Int
    ) {
        FirestoreHelper.getBillsByYear(account, year) { bills ->
            if (!isAdded || _binding == null) return@getBillsByYear
            calculateAndShowStat(bills)
        }
    }

    private fun calculateAndShowStat(bills: List<Bill>) {

        val expenseBills = bills.filter { it.type == "expense" }
        val incomeBills = bills.filter { it.type == "income" }

        val totalExpense = expenseBills.sumOf { it.amount }
        val totalIncome = incomeBills.sumOf { it.amount }

        val expenseStatList = expenseBills
            .groupBy { it.categoryId }
            .map { (categoryId, list) ->
                CategoryStatItem(
                    categoryId = categoryId,
                    categoryName = categoryMap[categoryId] ?: "未分類",
                    totalAmount = list.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.totalAmount }

        val incomeStatList = incomeBills
            .groupBy { it.categoryId }
            .map { (categoryId, list) ->
                CategoryStatItem(
                    categoryId = categoryId,
                    categoryName = categoryMap[categoryId] ?: "未分類",
                    totalAmount = list.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.totalAmount }

        showStatResult(
            expenseStatList,
            incomeStatList,
            totalExpense,
            totalIncome
        )
    }

    private fun showStatResult(
        expenseList: List<CategoryStatItem>,
        incomeList: List<CategoryStatItem>,
        totalExpense: Long,
        totalIncome: Long
    ) {
        binding.statCategoryRecyclerExpense.adapter =
            StatCategoryAdapter(expenseList) { item ->
                openStatDetail(
                    categoryId = item.categoryId,
                    categoryName = item.categoryName,
                    type = "expense"
                )
            }

        binding.statCategoryRecyclerIncome.adapter =
            StatCategoryAdapter(incomeList) { item ->
                openStatDetail(
                    categoryId = item.categoryId,
                    categoryName = item.categoryName,
                    type = "income"
                )
            }

        binding.statExpenseSummary.text =
            "共 ${expenseList.size} 個分類"

        binding.statIncomeSummary.text =
            "共 ${incomeList.size} 個分類"

        binding.statTotalExpense.text = "$$totalExpense"
        binding.statTotalIncome.text = "$$totalIncome"
        binding.statTotalBalance.text = "$${totalIncome - totalExpense}"
    }


    private fun showYearPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.statYearSelector)

        val currentYearData = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYearData downTo currentYearData - 5).toList()
        years.forEach { popup.menu.add(it.toString()) }

        popup.setOnMenuItemClickListener { item ->
            currentYear = item.title.toString().toInt()
            binding.statCurrentYear.text = "${currentYear}年"

            reload()
            true
        }

        popup.show()
    }

    private fun showMonthPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.statMonthSelector)

        popup.menu.add("全部月份")
        (1..12).forEach { popup.menu.add("${it}月") }

        popup.setOnMenuItemClickListener { item ->
            val title = item.title.toString()

            currentMonth = if (title == "全部月份") {
                binding.statCurrentMonth.text = "全部月份"
                null
            } else {
                val m = title.replace("月", "").toInt()
                binding.statCurrentMonth.text = "${m}月"
                m
            }
            reload()
            true
        }

        popup.show()
    }

    private fun openStatDetail(
        categoryId: String,
        categoryName: String,
        type: String
    ) {
        binding.statDetailContainer.visibility = View.VISIBLE

        val fragment = StatDetailFragment().apply {
            arguments = Bundle().apply {
                putString("categoryId", categoryId)
                putString("category", categoryName)
                putString("type", type)
                putInt("year", currentYear)
                putInt("month", currentMonth ?: -1)
            }
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.statDetailContainer, fragment)
            .addToBackStack(null)
            .commit()
    }



}