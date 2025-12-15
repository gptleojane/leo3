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
import com.example.leo3.util.DataVersionManager
import com.example.leo3.util.UserManager
import java.util.Calendar

class RecordFragment : Fragment() {
    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private var lastDataVersion = -1

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

        setupUI()
        refreshIfVersionChanged()
    }

    override fun onResume() {
        super.onResume()
        refreshIfVersionChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 初始化 UI
    private fun setupUI(){
        binding.recordRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        setToTodayYearMonth(updateUI = true)

        binding.recordYearSelector.setOnClickListener {
            showYearPopupMenu()
        }

        binding.recordMonthSelector.setOnClickListener {
            showMonthPopupMenu()
        }

        binding.recordIv.setOnClickListener {
            setToTodayYearMonth(updateUI = true)
            loadRecordData()
        }
    }

    //    版本更改後刷新
    fun refreshIfVersionChanged() {
        val dataVersion = DataVersionManager.getVersion()

        if (lastDataVersion != dataVersion) {
            loadRecordData()
        }
    }


    private fun loadRecordData() {
        val account = UserManager.getAccount(requireContext()) ?: return

        loadCategories(account) {
            loadBills(account, currentYear, currentMonth)
        }
    }

    // 分類
    private fun loadCategories(
        account: String,
        onLoaded: () -> Unit
    ) {
        FirestoreHelper.getAllCategories(account) { list ->
            categoryMap = list.associate { it.id to it.name }
            onLoaded()
        }
    }


    private fun loadBills(
        account: String,
        year: Int,
        month: Int
    ) {
        FirestoreHelper.getBillsByMonth(account, year, month) { bills ->

            if (!isAdded || _binding == null) return@getBillsByMonth

            // 1️⃣ 補上分類名稱
            bills.forEach { bill ->
                bill.categoryName = categoryMap[bill.categoryId] ?: ""
            }

            // 2️⃣ === 月統計計算 ===
            val totalExpense = bills
                .filter { it.type == "expense" }
                .sumOf { it.amount }

            val totalIncome = bills
                .filter { it.type == "income" }
                .sumOf { it.amount }

            val balance = totalIncome - totalExpense

            // 3️⃣ 更新上方 summary UI
            binding.recordSummaryExpenseAmount.text = "$$totalExpense"
            binding.recordSummaryIncomeAmount.text = "$$totalIncome"
            binding.recordSummaryBalanceAmount.text = "$$balance"

            // 4️⃣ RecyclerView
            val uiList = buildUiList(bills)
            binding.recordRecyclerView.adapter = RecordAdapter(uiList)

            // 5️⃣ 同步版本（唯一位置）
            lastDataVersion = DataVersionManager.getVersion()
        }
    }



    // UI Model 組裝（Header + Item）
    private fun buildUiList(
        bills: List<Bill>
    ): List<RecordUiModel> {

        if (bills.isEmpty()) return emptyList()

        val grouped = bills.groupBy {
            it.year * 10_000 + it.month * 100 + it.day
        }

        val result = mutableListOf<RecordUiModel>()

        grouped
            .toSortedMap(compareByDescending { it })
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

                // Items
                dayBills
                    .sortedByDescending{it.date}
                    .forEach { bill ->
                    result.add(RecordUiModel.Item(bill))
                }
            }
        return result
    }


    private fun showYearPopupMenu() {
        val popup =
            PopupMenu(requireContext(), binding.recordYearSelector)

        val currentYearData = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYearData downTo currentYearData - 5).toList()

        years.forEach { y ->
            popup.menu.add(y.toString())
        }

        popup.setOnMenuItemClickListener { item ->

            currentYear = item.title.toString().toInt()
            binding.recordCurrentYear.text = "${currentYear}年"

            loadRecordData()
            true
        }
        popup.show()
    }

    private fun showMonthPopupMenu() {
        val popup =
            PopupMenu(requireContext(), binding.recordMonthSelector)

        (1..12).forEach { m ->
            popup.menu.add(m.toString())
        }

        popup.setOnMenuItemClickListener { item ->

            currentMonth = item.title.toString().toInt()
            binding.recordCurrentMonth.text = "${currentMonth}月"

            loadRecordData()
            true
        }
        popup.show()
    }
    private fun setToTodayYearMonth(updateUI: Boolean) {
        val cal = Calendar.getInstance()
        currentYear = cal.get(Calendar.YEAR)
        currentMonth = cal.get(Calendar.MONTH) + 1

        if (updateUI) {
            binding.recordCurrentYear.text = "${currentYear}年"
            binding.recordCurrentMonth.text = "${currentMonth}月"
        }
    }
}
