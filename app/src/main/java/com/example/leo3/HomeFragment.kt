package com.example.leo3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.leo3.databinding.FragmentHomeBinding
import com.example.leo3.ui.HomeAdapter
import com.example.leo3.ui.model.Bill
import com.example.leo3.ui.model.HomeUiModel
import com.example.leo3.util.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    private var categoryMapCache: Map<String, String> = emptyMap()   // ⭐分類快取，避免每次都重抓

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

        binding.homeRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 設定當下年月
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1

        binding.homeCurrentYear.text = "${y}年"
        binding.homeCurrentMonth.text = "${m}月"

        // ⭐首次載入 → 先載分類，再載帳單
        loadCategories { map ->
            categoryMapCache = map
            loadBills(y, m, map)
        }

        // 年份切換
        binding.homeYearSelector.setOnClickListener { showYearPopupMenu() }

        // 月份切換
        binding.homeMonthSelector.setOnClickListener { showMonthPopupMenu() }
    }

    // ⭐每次回到頁面(包含新增帳後)，自動重新讀取資料
    override fun onResume() {
        super.onResume()

        if (!isAdded || _binding == null) return

        val year = binding.homeCurrentYear.text.toString().replace("年", "").toInt()
        val month = binding.homeCurrentMonth.text.toString().replace("月", "").toInt()

        // 如果分類快取還沒載入 → 重新載分類，再載帳單
        if (categoryMapCache.isEmpty()) {
            loadCategories { map ->
                categoryMapCache = map
                loadBills(year, month, map)
            }
        } else {
            loadBills(year, month, categoryMapCache)
        }
    }

    // ------------------------------------------------------------
    //  下載分類 (categoryId → categoryName)
    // ------------------------------------------------------------
    private fun loadCategories(onLoaded: (Map<String, String>) -> Unit) {
        val account = UserManager.getAccount(requireContext()) ?: return

        db.collection("users")
            .document(account)
            .collection("categories")
            .get()
            .addOnSuccessListener { qs ->

                if (!isAdded || _binding == null) return@addOnSuccessListener

                val map = qs.associate { doc ->
                    doc.id to (doc.getString("name") ?: "")
                }
                onLoaded(map)
            }
    }

    // ------------------------------------------------------------
    //  載入指定年月的帳單
    // ------------------------------------------------------------
    private fun loadBills(year: Int, month: Int, categoryMap: Map<String, String>) {
        val account = UserManager.getAccount(requireContext()) ?: return

        db.collection("users")
            .document(account)
            .collection("bills")
            .whereEqualTo("year", year)
            .whereEqualTo("month", month)
            .orderBy("day")
            .get()
            .addOnSuccessListener { qs ->

                if (!isAdded || _binding == null) return@addOnSuccessListener

                val bills = qs.documents.mapNotNull { doc ->
                    val bill = doc.toObject(Bill::class.java)
                    if (bill != null) {
                        bill.categoryName = categoryMap[bill.categoryId] ?: ""   // ⭐補上分類名稱
                    }
                    bill
                }

                val uiList = buildUiList(bills)
                binding.homeRecyclerView.adapter = HomeAdapter(uiList)
            }
    }

    // ------------------------------------------------------------
    //  將帳單轉換成 UI list（含 Header + Items）
    // ------------------------------------------------------------
    private fun buildUiList(bills: List<Bill>): List<HomeUiModel> {
        if (bills.isEmpty()) return emptyList()

        val grouped = bills.groupBy { "${it.year}-${it.month}-${it.day}" }

        val result = mutableListOf<HomeUiModel>()

        grouped.toSortedMap(compareByDescending { it })  // 日期新 → 舊
            .forEach { (_, dayBills) ->

                val first = dayBills.first()
                val total = dayBills.sumOf { it.amount }

                // Header
                result.add(
                    HomeUiModel.Header(
                        date = "${first.year}/${first.month}/${first.day}",
                        weekDay = first.weekDay,
                        totalAmount = total
                    )
                )

                // 每筆資料
                dayBills.forEach { bill ->
                    result.add(HomeUiModel.Item(bill))
                }
            }

        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ------------------------------------------------------------
    //  月份選單
    // ------------------------------------------------------------
    private fun showMonthPopupMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeMonthSelector)

        (1..12).forEach { m -> popup.menu.add(m.toString()) }

        popup.setOnMenuItemClickListener { item ->
            val selectedMonth = item.title.toString().toInt()
            val currentYear = binding.homeCurrentYear.text.toString().replace("年", "").toInt()

            binding.homeCurrentMonth.text = "${selectedMonth}月"

            loadBills(currentYear, selectedMonth, categoryMapCache)
            true
        }

        popup.show()
    }

    // ------------------------------------------------------------
    //  年份選單
    // ------------------------------------------------------------
    private fun showYearPopupMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeYearSelector)

        val years = listOf(2025, 2024, 2023, 2022, 2021)

        years.forEach { y -> popup.menu.add(y.toString()) }

        popup.setOnMenuItemClickListener { item ->
            val selectedYear = item.title.toString().toInt()
            val currentMonth = binding.homeCurrentMonth.text.toString().replace("月", "").toInt()

            binding.homeCurrentYear.text = "${selectedYear}年"

            loadBills(selectedYear, currentMonth, categoryMapCache)
            true
        }

        popup.show()
    }

    fun refreshData() {
        val year = binding.homeCurrentYear.text.toString().replace("年", "").toInt()
        val month = binding.homeCurrentMonth.text.toString().replace("月", "").toInt()

        if (categoryMapCache.isEmpty()) {
            loadCategories { map ->
                categoryMapCache = map
                loadBills(year, month, categoryMapCache)
            }
        } else {
            loadBills(year, month, categoryMapCache)
        }
    }

}
