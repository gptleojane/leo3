package com.example.leo3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.leo3.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private var selectedYear = 2025     // 初始顯示年
    private var selectedMonth = 11      // 初始顯示月 (1~12)
    private var isMonthMode = true      // 初始為月模式

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

        binding.homeSwitchMode.setOnClickListener {
            isMonthMode = !isMonthMode

            if (isMonthMode) {
                binding.homeSwitchMode.setImageResource(R.drawable.month)
                binding.homeSummaryExpenseTitle.text = "月支出"
                binding.homeSummaryBalanceTitle.text = "月結餘"
                binding.homeSummaryIncomeTitle.text = "月收入"
                binding.homeMonthSelector.visibility = View.VISIBLE
            } else {
                binding.homeSwitchMode.setImageResource(R.drawable.year)
                binding.homeSummaryExpenseTitle.text = "年支出"
                binding.homeSummaryBalanceTitle.text = "年結餘"
                binding.homeSummaryIncomeTitle.text = "年收入"
                binding.homeMonthSelector.visibility = View.GONE
            }
            refreshHomeData()
        }

        binding.homeYearSelector.setOnClickListener {
            showYearPopupMenu()
        }

        binding.homeMonthSelector.setOnClickListener {
            if (isMonthMode) {
                showMonthPopupMenu()
            }
        }





    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMonthPopupMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeMonthSelector)

        val months = listOf(1,2,3,4,5,6,7,8,9,10,11,12)

        months.forEach { month ->
            popup.menu.add(month.toString())
        }

        popup.setOnMenuItemClickListener { item ->
            selectedMonth = item.title.toString().toInt()
            binding.homeCurrentMonth.text = "${selectedMonth}月"
            refreshHomeData()
            true
        }

        popup.show()
    }


    private fun showYearPopupMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeYearSelector)

        val years = listOf(2025,2024,2023,2022,2021)

        years.forEach { year ->
            popup.menu.add(year.toString())
        }

        popup.setOnMenuItemClickListener { item ->
            selectedYear = item.title.toString().toInt()
            binding.homeCurrentYear.text = "${selectedYear}年"
            refreshHomeData()
            true
        }

        popup.show()
    }


    private fun loadMonthData(year: Int, month: Int) {
        // 月份 summary
        val expense = 1
        val income = 2
        val balance = income - expense

        binding.homeSummaryExpenseAmount.text = "$$expense"
        binding.homeSummaryIncomeAmount.text = "$$income"
        binding.homeSummaryBalanceAmount.text = "$$balance"

        // Month RecyclerView list
//        adapter.submitList(getRecordsByYearMonth(year, month))
    }

    private fun loadYearData(year: Int) {
        val expense = 4
        val income = 5
        val balance = income - expense

        binding.homeSummaryExpenseAmount.text = "$$expense"
        binding.homeSummaryIncomeAmount.text = "$$income"
        binding.homeSummaryBalanceAmount.text = "$$balance"

//        adapter.submitList(getRecordsByYear(year))
    }



    private fun refreshHomeData() {
        if (isMonthMode) {
            loadMonthData(selectedYear, selectedMonth)
        } else {
            loadYearData(selectedYear)
        }
    }

}


