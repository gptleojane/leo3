package com.example.leo3.ui.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.CategoryItem
import com.example.leo3.databinding.ActivityAddBillBinding
import com.example.leo3.ui.adapter.CategoryAdapter
import com.example.leo3.util.UserManager
import java.util.Calendar

class AddBillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBillBinding

    // 分類列表
    private val categoryList = mutableListOf<CategoryItem>()   // ★★★ 用 data.model.CategoryItem
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategoryId: String? = null

    private fun getToday(): Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView 設定 3 欄
        binding.addbillRecyclerView.layoutManager = GridLayoutManager(this, 3)

        setTodayDate()

        // Toggle 切換收入 / 支出
        binding.addbillMbtgType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            // 重複點選會視為無效
            if (!isChecked) return@addOnButtonCheckedListener

            val type = when (checkedId) {
                binding.addbillMbExpense.id -> "expense"
                else -> "income"
            }
            loadCategories(type)
        }

        // 日期選擇
        binding.addbillTietDate.setOnClickListener {
            val cal = getToday()

            val picker = DatePickerDialog(
                this,
                { _, y, m, d ->
                    val dateStr = "$y/${m + 1}/$d"
                    binding.addbillTietDate.setText(dateStr)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )

            picker.show()
        }

        // 數字鍵盤
        setupNumberPad()

        // 新增帳
        binding.addbillBtAdd.setOnClickListener {
            addBill()
        }

        // 返回
        binding.addbillBtBack.setOnClickListener { finish() }

        // 進入畫面預設載入 expense
        binding.addbillMbtgType.check(R.id.addbill_mb_expense)
        loadCategories("expense")
    }

    // -----------------------------
    // 新增帳單
    // -----------------------------
    private fun addBill() {
        // 1. 金額
        val amountText = binding.addbillTietAmount.text.toString().replace(",", "")
        if (amountText.isBlank()) {
            Toast.makeText(this, "請輸入金額", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountText.toLong()

        // 2. 日期
        val dateStr = binding.addbillTietDate.text.toString()
        val parts = dateStr.split("/")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val day = parts[2].toInt()

        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day)
        val timestamp = com.google.firebase.Timestamp(cal.time)
        val weekDay = cal.get(Calendar.DAY_OF_WEEK)

        // 3. 類型
        val type = if (binding.addbillMbExpense.isChecked) "expense" else "income"

        // 4. 備註
        val note = binding.addbillTietNote.text.toString()

        // 5. 分類
        val categoryId = selectedCategoryId ?: run {
            Toast.makeText(this, "請選擇分類", Toast.LENGTH_SHORT).show()
            return
        }

        // 6. 帳號
        val account = UserManager.getAccount(this) ?: return

        // 7. 建立資料 Map
        val billData = hashMapOf(
            "type" to type,
            "amount" to amount,
            "note" to note,
            "date" to timestamp,
            "year" to year,
            "month" to month,
            "day" to day,
            "weekDay" to weekDay,
            "categoryId" to categoryId
        )

        // ⭐ 使用 FirestoreHelper
        FirestoreHelper.addBill(account, billData) {
            Toast.makeText(this, "新增成功", Toast.LENGTH_SHORT).show()
            finish()
        }
    }



    private fun setTodayDate() {
        val cal = getToday()
        val dateStr =
            "${cal.get(Calendar.YEAR)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
        binding.addbillTietDate.setText(dateStr)
    }

    // -----------------------------
    // Firestore 載入分類 → 改成呼叫 CategoryRepository
    // -----------------------------
    private fun loadCategories(type: String) {
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.getCategories(account, type) { list ->
            categoryList.clear()
            categoryList.addAll(list)
            setupAdapter()
        }
    }


    private fun setupAdapter() {
        // 永遠選擇第一筆（未分類）
        if (categoryList.isEmpty()) return

        val defaultIndex = 0

        categoryAdapter = CategoryAdapter(categoryList, defaultIndex) { item ->
            selectedCategoryId = item.id
        }
        binding.addbillRecyclerView.adapter = categoryAdapter

        selectedCategoryId = categoryList[defaultIndex].id
    }

    // ----------------- 數字鍵盤 -----------------
    private fun setupNumberPad() {

        fun append(num: String) {
            val raw = binding.addbillTietAmount.text.toString().replace(",", "")

            // 不允許第一碼就是 0
            if (raw == "" && num == "0") return

            // 限制最多 12 碼
            if (raw.length >= 12) {
                Toast.makeText(this, "最多 12 碼", Toast.LENGTH_SHORT).show()
                return
            }

            val newValue = raw + num
            binding.addbillTietAmount.setText("%,d".format(newValue.toLong()))
        }

        binding.np0.setOnClickListener { append("0") }
        binding.np1.setOnClickListener { append("1") }
        binding.np2.setOnClickListener { append("2") }
        binding.np3.setOnClickListener { append("3") }
        binding.np4.setOnClickListener { append("4") }
        binding.np5.setOnClickListener { append("5") }
        binding.np6.setOnClickListener { append("6") }
        binding.np7.setOnClickListener { append("7") }
        binding.np8.setOnClickListener { append("8") }
        binding.np9.setOnClickListener { append("9") }

        binding.npDel.setOnClickListener {
            val raw = binding.addbillTietAmount.text.toString().replace(",", "")
            if (raw.isNotEmpty()) {
                val newValue = raw.dropLast(1)
                binding.addbillTietAmount.setText(
                    if (newValue == "") "" else "%,d".format(newValue.toLong())
                )
            }
        }

        binding.npAc.setOnClickListener {
            binding.addbillTietAmount.setText("")
        }
    }
}
