package com.example.leo3.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.Bill
import com.example.leo3.databinding.FragmentStatDetailBinding
import com.example.leo3.ui.activity.EditBillActivity
import com.example.leo3.ui.adapter.RecordAdapter
import com.example.leo3.util.AppFlags
import com.example.leo3.util.RecordUiBuilder
import com.example.leo3.util.UserManager
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class StatDetailFragment : Fragment(R.layout.fragment_stat_detail) {

    private var _binding: FragmentStatDetailBinding? = null
    private val binding get() = _binding!!

    private var currentYear = 0
    private var currentMonth: Int? = null

    private var categoryMap: Map<String, String> = emptyMap()

    private val editLauncher =
        registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                reload()
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStatDetailBinding.bind(view)

        setupUI()
        reload()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if (AppFlags.reloadData) {
            AppFlags.reloadData = false
            reload()
        }
    }

    private fun reload() {
        loadDetailData()
    }

    private fun loadDetailData() {
        if (currentYear == 0) {
            currentYear = arguments?.getInt("year") ?: Calendar.getInstance().get(Calendar.YEAR)
            currentMonth = arguments?.getInt("month")?.takeIf { it != -1 }
        }

        binding.statDetailCurrentYear.text = "${currentYear}年"
        binding.statDetailCurrentMonth.text =
            currentMonth?.let { "${it}月" } ?: "全部月份"

        val account = UserManager.getAccount(requireContext()) ?: return
        loadCategories(account)
    }

    private fun loadCategories(account: String) {
        FirestoreHelper.getAllCategories(account) { list ->
            if (!isAdded || _binding == null) return@getAllCategories
            categoryMap = list.associate { it.id to it.name }
            loadBills(account)
        }
    }

    private fun loadBills(account: String) {
        val categoryId = arguments?.getString("categoryId") ?: return
        val type = arguments?.getString("type") ?: return

        val callback: (List<Bill>) -> Unit = { bills ->
            val filtered = bills
                .filter { it.type == type && it.categoryId == categoryId }
                .sortedByDescending { it.date }

            filtered.forEach {
                it.categoryName = categoryMap[it.categoryId] ?: "未分類"
            }

            updateUI(filtered, type)
        }

        if (currentMonth == null) {
            FirestoreHelper.getBillsByYear(account, currentYear, callback)
        } else {
            FirestoreHelper.getBillsByMonth(account, currentYear, currentMonth!!, callback)
        }
    }

    private fun updateUI(list: List<Bill>, type: String) {
        val total = list.sumOf { it.amount }

        if (type == "expense") {
            binding.statDetailTvExpenseAmount.text = "$$total"
        } else {
            binding.statDetailTvIncomeAmount.text = "$$total"
        }

        binding.billRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.billRecyclerView.adapter =
            RecordAdapter(RecordUiBuilder.build(list)) { bill ->
                openEditBill(bill)
            }
    }

    private fun openEditBill(bill: Bill) {
        if (bill.id.isBlank()) return
        val intent = Intent(requireContext(), EditBillActivity::class.java)
        intent.putExtra("billId", bill.id)
        editLauncher.launch(intent)
    }

    private fun setupUI() {
        val category = arguments?.getString("category") ?: ""
        val type = arguments?.getString("type") ?: "expense"

        binding.statDetailTvCategory.text = category

        if (type == "expense") {
            binding.statDetailTvIncomeTitle.visibility = View.GONE
            binding.statDetailTvIncomeAmount.visibility = View.GONE
        } else {
            binding.statDetailTvExpenseTitle.visibility = View.GONE
            binding.statDetailTvExpenseAmount.visibility = View.GONE
        }

        binding.statDetailIv.setOnClickListener {
            requireParentFragment()
                .childFragmentManager
                .popBackStack()
        }

        binding.statDetailYearSelector.setOnClickListener { showYearPopupMenu() }
        binding.statDetailMonthSelector.setOnClickListener { showMonthPopupMenu() }

        binding.statDetailTvHint.setOnClickListener {
            Snackbar.make(
                binding.root,
                "📅 可切換年月\n✏ 點選項目可編輯",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun showYearPopupMenu() {
        val account = UserManager.getAccount(requireContext()) ?: return
        val popup = PopupMenu(requireContext(), binding.statDetailYearSelector)

        FirestoreHelper.getUserYears(
            account,
            onResult = { years ->
                years.sortedDescending().forEach {
                    popup.menu.add(it.toString())
                }

                popup.setOnMenuItemClickListener { item ->
                    currentYear = item.title.toString().toInt()
                    reload()
                    true
                }

                popup.show()
            },
            onFail = {}
        )
    }

    private fun showMonthPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.statDetailMonthSelector)
        popup.menu.add("全部月份")
        (1..12).forEach { popup.menu.add("${it}月") }

        popup.setOnMenuItemClickListener { item ->
            currentMonth =
                if (item.title == "全部月份") null
                else item.title.toString().replace("月", "").toInt()
            reload()
            true
        }

        popup.show()
    }


}
