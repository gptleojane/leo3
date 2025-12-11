package com.example.leo3.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.RecordUiModel
import com.example.leo3.databinding.FragmentRecordBinding
import com.example.leo3.util.UserManager
import java.util.Calendar
import com.example.leo3.data.model.Bill
import com.example.leo3.ui.adapter.RecordAdapter


class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    // categoryId -> categoryName
    private var categoryMap: Map<String, String> = emptyMap()

    private var currentYear = 0
    private var currentMonth = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView
        binding.recordRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 設定目前年月
        val cal = Calendar.getInstance()
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH) + 1

        binding.recordCurrentYear.text = "${currentYear}年"
        binding.recordCurrentMonth.text = "${currentMonth}月"

        // ⭐ 先載入分類 → 再載入帳單
        loadCategories {
            loadBills(currentYear, currentMonth)
        }

        // 年月切換
        binding.recordYearSelector.setOnClickListener { showYearPopupMenu() }
        binding.recordMonthSelector.setOnClickListener { showMonthPopupMenu() }
    }

    // 給外面呼叫，例如快速記帳完成後更新
    fun refreshData() {
        loadBills(currentYear, currentMonth)
    }

    // -----------------------------
    //  下載全部分類（categoryId -> name）
    // -----------------------------
    private fun loadCategories(onLoaded: () -> Unit) {
        val account = UserManager.getAccount(requireContext()) ?: return

        FirestoreHelper.getAllCategories(account) { list ->
            categoryMap = list.associate { it.id to it.name }
            onLoaded()
        }
    }

    // -----------------------------
    //  載入指定年月的帳單
    // -----------------------------
    private fun loadBills(year: Int, month: Int) {
        val account = UserManager.getAccount(requireContext()) ?: return

        FirestoreHelper.getBillsByMonth(account, year, month) { bills ->

            // 補上分類名稱
            bills.forEach { bill ->
                bill.categoryName = categoryMap[bill.categoryId] ?: ""
            }

            val uiList = buildUiList(bills)
            binding.recordRecyclerView.adapter = RecordAdapter(uiList)
        }
    }

    // -----------------------------
    //  把帳單轉成 Header + Item UI List
    // -----------------------------
    private fun buildUiList(bills: List<Bill>): List<RecordUiModel> {
        if (bills.isEmpty()) return emptyList()

        val grouped = bills.groupBy { "${it.year}-${it.month}-${it.day}" }

        val result = mutableListOf<RecordUiModel>()

        grouped.toSortedMap(compareByDescending { it }) // 日期新 → 舊
            .forEach { (_, dayBills) ->

                val first = dayBills.first()
                val total = dayBills.sumOf { it.amount }

                // Header
                result.add(
                    RecordUiModel.Header(
                        date = "${first.year}/${first.month}/${first.day}",
                        weekDay = first.weekDay,
                        totalAmount = total
                    )
                )

                // 明細 item
                dayBills.forEach { bill ->
                    result.add(RecordUiModel.Item(bill))
                }
            }

        return result
    }

    // -----------------------------
    //  月份選單
    // -----------------------------
    private fun showMonthPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.recordMonthSelector)

        (1..12).forEach { m -> popup.menu.add(m.toString()) }

        popup.setOnMenuItemClickListener { item ->
            val selectedMonth = item.title.toString().toInt()
            currentMonth = selectedMonth
            binding.recordCurrentMonth.text = "${selectedMonth}月"

            loadBills(currentYear, currentMonth)
            true
        }

        popup.show()
    }

    // -----------------------------
    //  年份選單
    // -----------------------------
    private fun showYearPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.recordYearSelector)

        val years = listOf(2025, 2024, 2023, 2022, 2021)
        years.forEach { y -> popup.menu.add(y.toString()) }

        popup.setOnMenuItemClickListener { item ->
            val selectedYear = item.title.toString().toInt()
            currentYear = selectedYear
            binding.recordCurrentYear.text = "${selectedYear}年"

            loadBills(currentYear, currentMonth)
            true
        }

        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}