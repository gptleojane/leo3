package com.example.leo3.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.Bill
import com.example.leo3.databinding.FragmentRecordBinding
import com.example.leo3.ui.activity.EditBillActivity
import com.example.leo3.ui.adapter.RecordAdapter
import com.example.leo3.util.RecordUiBuilder
import com.example.leo3.util.UserManager
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar
import kotlin.collections.filter
import androidx.core.widget.addTextChangedListener


class RecordFragment : Fragment() {

    companion object {
        private const val REQ_EDIT = 1002
    }

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private var categoryMap: Map<String, String> = emptyMap()
    private var allBills: List<Bill> = emptyList()
    private var isSearchExpanded = false



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

        binding.recordEtSearch.addTextChangedListener { text ->
            applySearch(text?.toString() ?: "")
        }
        binding.recordBtnClear.setOnClickListener {
            binding.recordEtSearch.setText("")
            updateRecordUI(allBills)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun reload() {
        loadRecordData()
    }

    private fun setupUI() {
        binding.billRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recordSearchHint.setOnClickListener {
            isSearchExpanded = !isSearchExpanded

            binding.recordSearchContainer.visibility =
                if (isSearchExpanded) View.VISIBLE else View.GONE

            if (isSearchExpanded) {
                binding.recordEtSearch.requestFocus()
            }
        }

        binding.recordYearSelector.setOnClickListener { showYearPopupMenu() }
        binding.recordMonthSelector.setOnClickListener { showMonthPopupMenu() }

        binding.recordIv.setOnClickListener {
            reload()
        }

        binding.recordTvHint.setOnClickListener {
            Snackbar.make(
                binding.root,
                "\uD83D\uDCC5 點左上角可回到今天\n" + "✏\uFE0F 點選項目可編輯或刪除",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun loadRecordData() {
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
            allBills = bills
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


        if (bills.isEmpty()) {
            binding.recordEmptyState.emptyContainer.visibility = View.VISIBLE
            binding.billRecyclerView.visibility = View.GONE
            return
        }

        binding.recordEmptyState.emptyContainer.visibility = View.GONE
        binding.billRecyclerView.visibility = View.VISIBLE
        val uiList = RecordUiBuilder.build(bills)

        binding.billRecyclerView.adapter =
            RecordAdapter(uiList) { bill ->

                val intent = Intent(requireContext(), EditBillActivity::class.java)
                intent.putExtra("billId", bill.id)

                startActivityForResult(intent, REQ_EDIT)

            }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQ_EDIT && resultCode == Activity.RESULT_OK) {
            reload()
        }
    }


    // ===== Popup =====

    private fun showYearPopupMenu() {
        val account = UserManager.getAccount(requireContext()) ?: return
        val popup = PopupMenu(requireContext(), binding.recordYearSelector)

        FirestoreHelper.getUserYears(
            account,
            onResult = { years ->
                years.sortedDescending().forEach {
                    popup.menu.add(it.toString())
                }

                popup.setOnMenuItemClickListener { item ->
                    val year = item.title.toString().toInt()
                    val month = binding.recordCurrentMonth.text
                        .toString().replace("月", "").toInt()

                    binding.recordCurrentYear.text = "${year}年"
                    loadBillsForQuery(account, year, month)
                    true
                }

                popup.show()
            },
            onFail = { }
        )
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

    private fun applySearch(keyword: String) {
        val trimmed = keyword.trim()

        val result = if (trimmed.isEmpty()) {
            allBills
        } else {
            allBills.filter { bill ->
                val categoryMatch = bill.categoryName.contains(trimmed, ignoreCase = true)
                val noteMatch = bill.note?.contains(trimmed, ignoreCase = true) ?: false
                categoryMatch || noteMatch
            }
        }
        updateRecordUI(result)
    }

}