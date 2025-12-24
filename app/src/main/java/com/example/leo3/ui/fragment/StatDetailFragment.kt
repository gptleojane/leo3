package com.example.leo3.ui.fragment

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

    private val editLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                loadBills() // ★回來就重抓/重畫
            }
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStatDetailBinding.bind(view)

        setupUI()
        loadBills()

        binding.statDetailYearSelector.setOnClickListener {
            showYearPopupMenu()
        }

        binding.statDetailMonthSelector.setOnClickListener {
            showMonthPopupMenu()
        }

        binding.statDetailIv.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.statDetailTvHint.setOnClickListener {
            Snackbar.make(
                binding.root,
                "\uD83D\uDCC5 點左上角返回統計頁面\n" + "✏\uFE0F 點選項目可編輯或刪除",
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        if (AppFlags.reloadData) {
            AppFlags.reloadData = false
            loadBills()
        }
    }


    private fun loadBills() {
        val categoryId = arguments?.getString("categoryId") ?: return
        val type = arguments?.getString("type") ?: return
        val year = currentYear
        val month = currentMonth
        val account = UserManager.getAccount(requireContext()) ?: return

        FirestoreHelper.getAllCategories(account) { categories ->

            if (!isAdded || _binding == null) return@getAllCategories

            val categoryMap = categories.associate { it.id to it.name }

            // ② 再抓帳單
            val callback: (List<Bill>) -> Unit = { bills ->

                val filtered = bills
                    .filter { it.type == type }
                    .filter { it.categoryId == categoryId }
                    .sortedByDescending { it.date }

                // ③ ★關鍵：補齊每一筆 Bill 的 categoryName
                filtered.forEach { bill ->
                    bill.categoryName =
                        categoryMap[bill.categoryId] ?: "未分類"
                }

                val total = filtered.sumOf { it.amount }

                if (type == "expense") {
                    binding.statDetailTvExpenseAmount.text = "$$total"
                } else {
                    binding.statDetailTvIncomeAmount.text = "$$total"
                }
                setupRecyclerView(filtered)
            }

            if (month == null) {
                FirestoreHelper.getBillsByYear(account, year, callback)
            } else {
                FirestoreHelper.getBillsByMonth(account, year, month, callback)
            }
        }
    }

    private fun setupRecyclerView(list: List<Bill>) {

        binding.billRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        val uiList = RecordUiBuilder.build(list)

        binding.billRecyclerView.adapter =
            RecordAdapter(uiList) { bill ->
                openEditBill(bill)
            }
    }

    private fun openEditBill(bill: Bill) {
        if(bill.id.isBlank()){
            return
        }
        val intent = Intent(requireContext(), EditBillActivity::class.java)
        intent.putExtra("billId", bill.id)   // ← 一定要是 billId
        editLauncher.launch(intent)
    }



    private fun setupUI() {
        // 之後從 StatFragment 傳進來
        val category = arguments?.getString("category") ?: ""
        val type = arguments?.getString("type") ?: "expense"
        currentYear = arguments?.getInt("year") ?: 2025
        currentMonth = arguments?.getInt("month")?.takeIf { it != -1 }


        // 中間顯示分類名稱
        binding.statDetailTvCategory.text = category

        // 顯示年月
        binding.statDetailCurrentYear.text = "${currentYear}年"
        binding.statDetailCurrentMonth.text =
            currentMonth?.let { "${it}月" } ?: "全部月份"

        // 根據收入 / 支出隱藏欄位
        if (type == "expense") {
            binding.statDetailTvIncomeTitle.visibility = View.GONE
            binding.statDetailTvIncomeAmount.visibility = View.GONE

        } else {
            binding.statDetailTvExpenseTitle.visibility = View.GONE
            binding.statDetailTvExpenseAmount.visibility = View.GONE
        }
    }

    private fun showYearPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.statDetailYearSelector)
        val nowYear = Calendar.getInstance().get(Calendar.YEAR)

        (nowYear downTo nowYear - 5).forEach {
            popup.menu.add(it.toString())
        }

        popup.setOnMenuItemClickListener { item ->
            currentYear = item.title.toString().toInt()
            binding.statDetailCurrentYear.text = "${currentYear}年"
            loadBills()
            true
        }

        popup.show()
    }

    private fun showMonthPopupMenu() {
        val popup = PopupMenu(requireContext(), binding.statDetailMonthSelector)
        popup.menu.add("全部月份")
        (1..12).forEach { popup.menu.add("${it}月") }

        popup.setOnMenuItemClickListener { item ->
            val title = item.title.toString()
            currentMonth =
                if (title == "全部月份") null
                else title.replace("月", "").toInt()

            binding.statDetailCurrentMonth.text =
                currentMonth?.let { "${it}月" } ?: "全部月份"

            loadBills()
            true
        }

        popup.show()
    }


}
