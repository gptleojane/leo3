package com.example.leo3.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.Bill
import com.example.leo3.databinding.FragmentStatDetailBinding
import com.example.leo3.ui.activity.EditBillActivity
import com.example.leo3.ui.adapter.RecordAdapter
import com.example.leo3.util.RecordUiBuilder
import com.example.leo3.util.UserManager

class StatDetailFragment : Fragment(R.layout.fragment_stat_detail) {

    private var _binding: FragmentStatDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentStatDetailBinding.bind(view)

        initArgs()
        loadBills()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadBills() {
        val category = arguments?.getString("category") ?: return
        val type = arguments?.getString("type") ?: return
        val year = arguments?.getInt("year") ?: return
        val month = arguments?.getInt("month") ?: -1

        val account = UserManager.getAccount(requireContext()) ?: return

        val callback: (List<Bill>) -> Unit = { bills ->
            val filtered = bills
                .filter { it.type == type }
                .filter { it.categoryId == category }
                .sortedByDescending { it.date }

            setupRecyclerView(filtered)
        }

        if (month == -1) {
            FirestoreHelper.getBillsByYear(account, year, callback)
        } else {
            FirestoreHelper.getBillsByMonth(account, year, month, callback)
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
        val intent = Intent(requireContext(), EditBillActivity::class.java)
        intent.putExtra("bill_id", bill.id)
        startActivity(intent)
    }



    private fun initArgs() {
        // 之後從 StatFragment 傳進來
        val category = arguments?.getString("category") ?: ""
        val type = arguments?.getString("type") ?: "expense"
        val year = arguments?.getInt("year") ?: 2025
        val month = arguments?.getInt("month") ?: -1 // -1 = 全部月份

        // 中間顯示分類名稱
        binding.statCategoryTitle.text = category

        // 顯示年月
        binding.statCurrentYear.text = "${year}年"
        binding.statCurrentMonth.text =
            if (month == -1) "全部月份" else "${month}月"

        // 根據收入 / 支出隱藏欄位
        if (type == "expense") {
            binding.statTotalIncome.visibility = View.GONE
            binding.statTotalIncomeTitle.visibility = View.GONE

        } else {
            binding.statTotalExpense.visibility = View.GONE
            binding.statTotalExpenseTitle.visibility = View.GONE
        }
    }

}
