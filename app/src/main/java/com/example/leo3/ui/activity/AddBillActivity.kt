package com.example.leo3.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.CategoryItem
import com.example.leo3.databinding.ActivityAddBillBinding
import com.example.leo3.ui.adapter.CategoryAdapter
import com.example.leo3.util.AppFlags
import com.example.leo3.util.UserManager
import java.util.Calendar

class AddBillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBillBinding

    // 分類列表
    private val categoryList = mutableListOf<CategoryItem>()
    private lateinit var categoryAdapter: CategoryAdapter
    private var selectedCategory: CategoryItem? = null
    private var amount: Long = 0L
    private fun today(): Calendar = Calendar.getInstance()

    private val editCategoryLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // 👉 分類有變，重新載入分類
                val type =
                    if (binding.addbillMbExpense.isChecked) "expense" else "income"
                loadCategories(type)
            }
        }


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

        //預設日期為今天，金額$0，類型為支出
        setupDefaultState()

        // Toggle 切換收入 / 支出
        setupTypeToggle()

        setupDatePicker()

        setupNumberPad()

        binding.addbillBtAddCategory.setOnClickListener {
            val intent = Intent(this, EditCategoryActivity::class.java)
            editCategoryLauncher.launch(intent)
        }

        binding.addbillBtAdd.setOnClickListener { addBill() }

        binding.addbillBtBack.setOnClickListener { finish() }
    }

    private fun setupDefaultState() {
        // 預設日期
        val cal = today()
        binding.addbillTietDate.setText(
            "${cal.get(Calendar.YEAR)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
        )

        // 預設金額
        renderAmount()

        // 預設支出
        binding.addbillMbtgType.check(R.id.addbill_mb_expense)
        loadCategories("expense")
    }

    private fun setupTypeToggle() {
        binding.addbillMbtgType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val type = if (checkedId == R.id.addbill_mb_expense) "expense" else "income"
            loadCategories(type)
        }
    }

    private fun setupDatePicker() {
        binding.addbillTietDate.setOnClickListener {
            val cal = today()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    binding.addbillTietDate.setText("$y/${m + 1}/$d")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // 新增帳單
    private fun addBill() {

        val category = selectedCategory ?: return

        //  日期
        val dateStr = binding.addbillTietDate.text.toString().trim()
        val parts = dateStr.split("/")

        if (parts.size != 3) {
            Toast.makeText(this, "日期格式錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        val year = parts[0].toIntOrNull()
        val month = parts[1].toIntOrNull()
        val day = parts[2].toIntOrNull()

        if (year == null || month == null || day == null) {
            Toast.makeText(this, "日期格式錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }
        val timestamp = com.google.firebase.Timestamp(cal.time)
        val weekDay = cal.get(Calendar.DAY_OF_WEEK)

        // 3. 類型
        val type = if (binding.addbillMbExpense.isChecked) "expense" else "income"


        //  備註
        val noteShow = binding.addbillTietNote.text.toString()
        val note = noteShow.ifBlank { category.name }


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
            "categoryId" to category.id
        )


        val account = UserManager.getAccount(this) ?: return
        FirestoreHelper.addBill(account, billData) {

            if (isFinishing || isDestroyed) return@addBill

            AppFlags.reloadData = true

            finish()
        }
    }


    private fun loadCategories(type: String) {
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.getCategories(account, type, onResult = { list ->
            categoryList.clear()
            categoryList.addAll(list)
            setupCategoryAdapter()
        }, onFail = {
            Toast.makeText(this, "讀取分類失敗，請檢查網路", Toast.LENGTH_SHORT).show()

        })
    }


    private fun setupCategoryAdapter() {
        // 永遠選擇第一筆（未分類）
        if (categoryList.isEmpty()) return

        //預設已選定第一項分類
        categoryAdapter = CategoryAdapter(categoryList, 0) { item ->
            selectedCategory = item
        }
        binding.addbillRecyclerView.adapter = categoryAdapter

        // 預設顯示選則第一個（未分類）
        selectedCategory = categoryList.first()
    }

    // ----------------- 數字鍵盤 -----------------
    private fun setupNumberPad() {

        fun append(num: Int) {
            val next = amount * 10 + num
            if (next > 9_999_999_999L) {
                Toast.makeText(this, "超過輸入上限", Toast.LENGTH_SHORT).show()
                return

            }
            amount = next
            renderAmount()
        }

        binding.np0.setOnClickListener { append(0) }
        binding.np1.setOnClickListener { append(1) }
        binding.np2.setOnClickListener { append(2) }
        binding.np3.setOnClickListener { append(3) }
        binding.np4.setOnClickListener { append(4) }
        binding.np5.setOnClickListener { append(5) }
        binding.np6.setOnClickListener { append(6) }
        binding.np7.setOnClickListener { append(7) }
        binding.np8.setOnClickListener { append(8) }
        binding.np9.setOnClickListener { append(9) }

        // 刪除一碼
        binding.npDel.setOnClickListener {
            amount /= 10
            renderAmount()
        }

        // AC：直接回到 $0
        binding.npAc.setOnClickListener {
            amount = 0L
            renderAmount()
        }
    }

    private fun renderAmount() {
        binding.addbillTvAmount.text = "$" + "%,d".format(amount)
    }

}
