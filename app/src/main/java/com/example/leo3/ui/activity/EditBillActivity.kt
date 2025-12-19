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
import com.example.leo3.data.model.Bill
import com.example.leo3.data.model.CategoryItem
import com.example.leo3.databinding.ActivityEditBillBinding
import com.example.leo3.ui.adapter.CategoryAdapter
import com.example.leo3.util.UserManager
import com.google.firebase.Timestamp
import java.util.Calendar

class EditBillActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBillBinding

    // 分類列表
    private val categoryList = mutableListOf<CategoryItem>()
    private lateinit var categoryAdapter: CategoryAdapter

    //利用bill自動產生的autoID來指定該筆資料，進行雲端查詢
    private var billId: String = ""
    private var currentBill: Bill? = null

    private var selectedCategory: CategoryItem? = null
    private var amount: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        billId = intent.getStringExtra("billId") ?: run {
            finish()
            return
        }

        // RecyclerView 設定 3 欄
        binding.editbillRecyclerView.layoutManager = GridLayoutManager(this, 3)

        setupTypeToggle()
        setupDatePicker()
        setupNumberPad()


        binding.editbillBtEdit.setOnClickListener { updateBill() }

        binding.editbillBtDelete.setOnClickListener { deleteBill() }

        loadBillFromCloud()

    }

    private fun loadBillFromCloud() {
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.getBillById(account, billId) { bill ->
            if (bill == null) {
                Toast.makeText(this, "資料不存在", Toast.LENGTH_SHORT).show()
                finish()
                return@getBillById
            }

            currentBill = bill
            amount = bill.amount

            renderBillToUI(bill)
        }
    }


    private fun renderBillToUI(bill: Bill) {
        // 類型
        if (bill.type == "expense") {
            binding.editbillMbtgType.check(R.id.editbill_mb_expense)
        } else {
            binding.editbillMbtgType.check(R.id.editbill_mb_income)
        }

        // 日期
        binding.editbillTietDate.setText(
            "${bill.year}/${bill.month}/${bill.day}"
        )

        // 金額
        renderAmount()

        // 備註
        binding.editbillTietNote.setText(bill.note)

        // 分類
        loadCategories(bill.type, bill.categoryId)
    }


    // 2️⃣ UI 設定
    // -----------------------------
    private fun setupTypeToggle() {
        binding.editbillMbtgType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener

            val type =
                if (checkedId == R.id.editbill_mb_expense) "expense" else "income"

            loadCategories(type, null)
        }
    }

    private fun setupDatePicker() {
        binding.editbillTietDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    binding.editbillTietDate.setText("$y/${m + 1}/$d")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun loadCategories(type: String, preselectId: String?) {
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.getCategories(account, type) { list ->
            categoryList.clear()
            categoryList.addAll(list)

            val defaultIndex = list.indexOfFirst { it.id == preselectId }
                .takeIf { it >= 0 } ?: 0

            categoryAdapter =
                CategoryAdapter(categoryList, defaultIndex) { item ->
                    selectedCategory = item
                }

            binding.editbillRecyclerView.adapter = categoryAdapter
            selectedCategory = categoryList[defaultIndex]
        }
    }


    // 3️⃣ 修改
    // -----------------------------
    private fun updateBill() {
        val category = selectedCategory ?: return
        val account = UserManager.getAccount(this) ?: return

        val dateParts = binding.editbillTietDate.text.toString().split("/")
        val year = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val day = dateParts[2].toInt()

        val cal = Calendar.getInstance().apply {
            set(year, month - 1, day)
        }

        val noteInput = binding.editbillTietNote.text.toString()
        val note = noteInput.ifBlank { category.name }

        val data = mapOf(
            "type" to (if (binding.editbillMbExpense.isChecked) "expense" else "income"),
            "amount" to amount,
            "note" to note,
            "date" to Timestamp(cal.time),
            "year" to year,
            "month" to month,
            "day" to day,
            "weekDay" to cal.get(Calendar.DAY_OF_WEEK),
            "categoryId" to category.id
        )

        FirestoreHelper.updateBill(account, billId, data) {
            setResult(RESULT_OK)
            finish()
        }
    }

    // 4️⃣ 刪除
    // -----------------------------
    private fun deleteBill() {
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.deleteBill(account, billId) {
            setResult(RESULT_OK)
            finish()
        }
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
        binding.editbillTvAmount.text = "$" + "%,d".format(amount)
    }

}
