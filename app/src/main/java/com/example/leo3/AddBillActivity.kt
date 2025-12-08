package com.example.leo3

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityAddBillBinding
import com.example.leo3.util.UserManager
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar


class AddBillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBillBinding
    private val db = FirebaseFirestore.getInstance()

    private var categoryNameList = mutableListOf<String>()
    private var categoryIdMap = mutableMapOf<String, String>()   // name → docId

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

        // 預設日期
        setupDatePicker()

        // 數字鍵盤
        setupNumberPad()

        binding.addbillActvCategory.setOnClickListener {
            println("分類欄位被點擊了！")
            binding.addbillActvCategory.showDropDown()
        }


        // ⭐⭐⭐ 預設支出
        binding.addbillMbExpense.isChecked = true
        loadCategories("expense")

        // ⭐⭐⭐ 切換支出 / 收入自動載入分類
        binding.addbillMbtgType.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.addbill_mb_expense -> loadCategories("expense")
                R.id.addbill_mb_income -> loadCategories("income")
            }
        }

        // ⭐⭐⭐ 點分類欄位就展開選單
        binding.addbillActvCategory.setOnClickListener {
            binding.addbillActvCategory.showDropDown()
        }

        // 新增帳
        binding.addbillBtAdd.setOnClickListener { }

        // 返回
        binding.addbillBtBack.setOnClickListener { finish() }
    }

    private fun setupDatePicker() {
        val cal = Calendar.getInstance()
        val today =
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
        binding.addbillTietDate.setText(today)

        binding.addbillTietDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    binding.addbillTietDate.setText("$year-${month + 1}-$day")
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // ================= 收入 / 支出 切換 ==================
    private fun setupTypeToggle() {

        // 預設支出
        binding.addbillMbExpense.isChecked = true
        loadCategories("expense")

        binding.addbillMbtgType.addOnButtonCheckedListener { _, checkedId, _ ->
            when (checkedId) {
                R.id.addbill_mb_expense -> loadCategories("expense")
                R.id.addbill_mb_income -> loadCategories("income")
            }
        }
    }

    // ================= 讀取 Firestore 分類 ==================
    private fun loadCategories(type: String) {
        val account = UserManager.getAccount(this) ?: return

        db.collection("users")
            .document(account)
            .collection("categories")
            .whereEqualTo("type", type)
            .orderBy("sortOrder")
            .get()
            .addOnSuccessListener { result ->

                categoryNameList.clear()
                categoryIdMap.clear()

                for (doc in result) {
                    val name = doc.getString("name") ?: ""
                    categoryNameList.add(name)
                    categoryIdMap[name] = doc.id
                }

                // Adapter
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryNameList)
                binding.addbillActvCategory.setAdapter(adapter)
            }
    }


    // ----------------- 數字鍵盤 -----------------
    private fun setupNumberPad() {

        fun append(num: String) {
            val raw = binding.addbillTietAmount.text.toString().replace(",", "")

            if (raw == "" && num == "0") return

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
                binding.addbillTietAmount.setText(if (newValue == "") "" else "%,d".format(newValue.toLong()))
            }
        }

        binding.npAc.setOnClickListener {
            binding.addbillTietAmount.setText("")
        }
    }

}
