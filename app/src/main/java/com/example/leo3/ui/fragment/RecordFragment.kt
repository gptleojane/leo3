package com.example.leo3.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.Bill
import com.example.leo3.data.model.RecordUiModel
import com.example.leo3.databinding.FragmentRecordBinding
import com.example.leo3.ui.adapter.RecordAdapter
import com.example.leo3.util.UserManager
import java.util.Calendar
import kotlin.collections.filter

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private var categoryMap: Map<String, String> = emptyMap()


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
        setupUI()
        reload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun reload() {
        loadRecordData()
    }

    private fun setupUI() {
        binding.recordRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recordYearSelector.setOnClickListener { showYearPopupMenu() }
        binding.recordMonthSelector.setOnClickListener { showMonthPopupMenu() }

        binding.recordIv.setOnClickListener {
            reload()
        }
    }
    private fun loadRecordData(){
        val account = UserManager.getAccount(requireContext()) ?: return
        val (year, month) = getTodayYearMonth()

        // UI 永遠顯示當月
        binding.recordCurrentYear.text = "${year}年"
        binding.recordCurrentMonth.text = "${month}月"

        loadCategories(account) {
            loadBills(account, year, month)
        }
    }


    private fun loadCategories(account: String, onLoaded: () -> Unit) {
        FirestoreHelper.getAllCategories(account) { list ->
            if (!isAdded || _binding == null) return@getAllCategories
            categoryMap = list.associate { it.id to it.name }
            onLoaded()
        }
    }

    private fun loadBills(account: String, year: Int, month: Int) {
        FirestoreHelper.getBillsByMonth(account, year, month) { bills ->
            if (!isAdded || _binding == null) return@getBillsByMonth

            bills.forEach {
                it.categoryName = categoryMap[it.categoryId] ?: ""
            }

            updateRecordUI(bills)
        }
    }

    // ===== 查詢用（不影響 reload 規則） =====

    private fun loadBillsForQuery(account: String, year: Int, month: Int) {
        loadCategories(account) {
            loadBills(account, year, month)
        }
    }

    // 查詢其它月份：你要也可以保留，但「不存 cache」，且切回來會回當月
    // ===== UI =====

    private fun updateRecordUI(bills: List<Bill>) {
        val totalExpense = bills.filter { it.type == "expense" }.sumOf { it.amount }
        val totalIncome = bills.filter { it.type == "income" }.sumOf { it.amount }

        binding.recordSummaryExpenseAmount.text = "$$totalExpense"
        binding.recordSummaryIncomeAmount.text = "$$totalIncome"
        binding.recordSummaryBalanceAmount.text =
            "$${totalIncome - totalExpense}"

        binding.recordRecyclerView.adapter =
            RecordAdapter(buildUiList(bills))
    }

    private fun buildUiList(bills: List<Bill>): List<RecordUiModel> {
        if (bills.isEmpty()) return emptyList()

        val grouped = bills.groupBy {
            it.year * 10_000 + it.month * 100 + it.day
        }

        val result = mutableListOf<RecordUiModel>()

        grouped.toSortedMap(compareByDescending { it }).forEach { (_, dayBills) ->
            val first = dayBills.first()
            val total = dayBills.sumOf { it.amount }

            result.add(
                RecordUiModel.Header(
                    date = "${first.year}/${first.month}/${first.day}",
                    weekDay = first.weekDay,
                    totalAmount = total
                )
            )

            dayBills.sortedByDescending { it.date }.forEach {
                result.add(RecordUiModel.Item(it))
            }
        }
        return result
    }

    // ===== Popup =====

    private fun showYearPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.recordYearSelector)
        val nowYear = Calendar.getInstance().get(Calendar.YEAR)

        (nowYear downTo nowYear - 5).forEach {
            popup.menu.add(it.toString())
        }

        popup.setOnMenuItemClickListener { item ->
            val account =
                UserManager.getAccount(requireContext()) ?: return@setOnMenuItemClickListener true
            val year = item.title.toString().toInt()
            val month = binding.recordCurrentMonth.text
                .toString().replace("月", "").toInt()

            binding.recordCurrentYear.text = "${year}年"
            loadBillsForQuery(account, year, month)
            true
        }

        popup.show()
    }

    private fun showMonthPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.recordMonthSelector)
        (1..12).forEach { popup.menu.add(it.toString()) }

        popup.setOnMenuItemClickListener { item ->
            val account =
                UserManager.getAccount(requireContext()) ?: return@setOnMenuItemClickListener true
            val year = binding.recordCurrentYear.text
                .toString().replace("年", "").toInt()
            val month = item.title.toString().toInt()

            binding.recordCurrentMonth.text = "${month}月"
            loadBillsForQuery(account, year, month)
            true
        }

        popup.show()
    }

    private fun getTodayYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
    }
}