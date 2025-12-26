package com.example.leo3.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.CategoryItem
import com.example.leo3.databinding.ActivityEditCategoryBinding
import com.example.leo3.ui.adapter.CategoryAdapter
import com.example.leo3.util.UserManager


class EditCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditCategoryBinding

    private val categoryList = mutableListOf<CategoryItem>()
    private lateinit var categoryAdapter: CategoryAdapter

    private var selectedType: String? = null // Toggle 點到的
    private var selectedCategory: CategoryItem? = null // RecyclerView 點到的

    private var callReload = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView 設定 3 欄
        binding.editcateRecyclerView.layoutManager = GridLayoutManager(this, 3)

        binding.editcateBtnUpdate.setOnClickListener {
            updateCategory()
        }

        binding.editcateBtnDelete.setOnClickListener {
            deleteCategory()
        }


        binding.editcateBtnAdd.setOnClickListener {
            addCategory()
        }


        binding.editcateBtnReset.setOnClickListener {
            setupDefaultState()
        }


        binding.editcateBtnBack.setOnClickListener {
            finish()
        }

        setupDefaultState()
        setupTypeToggle()
    }

    // 初始狀態（不選任何東西）
    // -------------------------
    private fun setupDefaultState() {
        binding.editcateMbtgType.clearChecked()
        binding.editcateRecyclerView.adapter = null

        selectedType = null
        selectedCategory = null

        binding.editcateTvCategoryHint.text = "尚未選擇分類"
        binding.editcateTvTypeHint.text = "尚未選擇類別 (支出 / 收入)"

        // ⭐ 關鍵：先鎖住按鈕
        binding.editcateBtnUpdate.isEnabled = false
        binding.editcateBtnDelete.isEnabled = false
        binding.editcateBtnAdd.isEnabled = false


    }

    private fun setupTypeToggle() {
        binding.editcateMbtgType.addOnButtonCheckedListener { _, checkedId, isChecked ->


            if (!isChecked) return@addOnButtonCheckedListener

            selectedType = when (checkedId) {
                R.id.editcate_mb_expense -> "expense"
                R.id.editcate_mb_income -> "income"
                else -> null

            }

            var showType = if (selectedType == "expense") "支出" else "收入"
            binding.editcateTvTypeHint.text = "正在選擇：$showType"

            binding.editcateBtnAdd.isEnabled = true

            selectedCategory = null
            loadCategories()


        }
    }


    private fun loadCategories() {
        val type = selectedType ?: return
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.getCategories(account, type) { list ->
            categoryList.clear()
            categoryList.addAll(list)
            setupAdapter()
        }
    }

    private fun setupAdapter() {
        categoryAdapter = CategoryAdapter(categoryList, -1) { item ->
            selectedCategory = item

            if (item.fixed) {
                binding.editcateTvCategoryHint.text =
                    "正在編輯：${item.name}（系統預設無法編輯）"
            } else {
                binding.editcateTvCategoryHint.text =
                    "正在編輯：${item.name}"

                binding.editcateTietRename.setText(item.name)
            }

            // ⭐ 解鎖
            binding.editcateBtnUpdate.isEnabled = !item.fixed
            binding.editcateBtnDelete.isEnabled = !item.fixed
        }
        binding.editcateRecyclerView.adapter = categoryAdapter
    }


    // 修改分類
    private fun updateCategory() {
        val category = selectedCategory ?: run {
            Toast.makeText(this, "請先選擇要修改的分類", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.fixed) {
            Toast.makeText(this, "此分類不可修改", Toast.LENGTH_SHORT).show()
            return
        }

        val newName = binding.editcateTietRename.text.toString().trim()
        if (newName.isBlank()) {
            Toast.makeText(this, "請輸入新的分類名稱", Toast.LENGTH_SHORT).show()
            return
        }

        if (newName.length > 4) {
            Toast.makeText(this, "分類名稱最多 4 個字", Toast.LENGTH_SHORT).show()
            return
        }

        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.updateCategory(account, category.id, newName) {
            binding.editcateTietRename.setText("")
            callReload = true
            loadCategories()
        }
    }

    private fun addCategory() {
        val type = when (binding.editcateMbtgType.checkedButtonId) {
            R.id.editcate_mb_expense -> "expense"
            R.id.editcate_mb_income -> "income"
            else -> null
        }
        val name = binding.editcateTietAddname.text.toString().trim()

        if (type == null) {
            Toast.makeText(this, "請先選擇類別（支出 / 收入）", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isBlank()) {
            Toast.makeText(this, "請輸入分類名稱", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.length > 4) {
            Toast.makeText(this, "分類名稱最多 4 個字", Toast.LENGTH_SHORT).show()
            return
        }


        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.addCategory(account, type, name) {
            binding.editcateTietAddname.setText("")
            callReload = true
            loadCategories()

        }
    }

    // 刪除分類
    // -------------------------
    private fun deleteCategory() {
        val category = selectedCategory ?: run {
            Toast.makeText(this, "請先選擇要刪除的分類", Toast.LENGTH_SHORT).show()
            return
        }

        if (category.fixed) {
            Toast.makeText(this, "此分類不可刪除", Toast.LENGTH_SHORT).show()
            return
        }

        val account = UserManager.getAccount(this) ?: return

        AlertDialog.Builder(this)
            .setTitle("確認刪除分類")
            .setMessage("刪除後，該分類底下的所有帳目將移至『未分類』\n此操作無法復原，確定要刪除嗎？")
            .setPositiveButton("確認") { _, _ ->
                FirestoreHelper.deleteCategory(account, category.id) {
                    selectedCategory = null
                    callReload = true
                    loadCategories()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun finish() {
        if (callReload) {
            setResult(RESULT_OK)
        }
        super.finish()
    }

}




