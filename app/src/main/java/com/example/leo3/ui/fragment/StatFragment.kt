package com.example.leo3.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.data.model.CategoryStatItem
import com.example.leo3.databinding.FragmentStatBinding
import com.example.leo3.ui.adapter.StatCategoryAdapter
import java.util.Calendar

class StatFragment : Fragment() {

    private var _binding: FragmentStatBinding? = null
    private val binding get() = _binding!!

    private var currentYear = 0
    private var currentMonth: Int? = null // null = 全部月份

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

        val cal = Calendar.getInstance()
        currentYear = cal.get(Calendar.YEAR)

        binding.statCurrentYear.text = "${currentYear}年"
        binding.statCurrentMonth.text = "全部月份"

        // RecyclerView
        binding.statCategoryRecycler.layoutManager =
            LinearLayoutManager(requireContext())

        binding.statCategoryRecyclerIncome.layoutManager =
            LinearLayoutManager(requireContext())

        // 先顯示空資料
        showStatResult(emptyList(), emptyList())
    }

    private fun showStatResult(
        expenseList: List<CategoryStatItem>,
        incomeList: List<CategoryStatItem>
    ) {
        binding.statCategoryRecycler.adapter =
            StatCategoryAdapter(expenseList)

        binding.statCategoryRecyclerIncome.adapter =
            StatCategoryAdapter(incomeList)

        binding.statExpenseSummary.text =
            "共 ${expenseList.size} 個分類"

        binding.statIncomeSummary.text =
            "共 ${incomeList.size} 個分類"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
