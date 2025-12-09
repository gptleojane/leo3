package com.example.leo3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityRegisterBinding
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    //    省去findViewById必要的宣告
    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.registerBtRegister.setOnClickListener {
            val account = binding.registerTietAccount.text.toString()
            val password = binding.registerTietPassword.text.toString()

            if (account.isBlank() || password.isBlank()) {
                Toast.makeText(this, "帳號密碼不能空白", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 3) {
                Toast.makeText(this, "密碼長度至少3位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (account.length > 20) {
                Toast.makeText(this, "帳號不能超過 20 字元", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (account.contains("/")) {
                Toast.makeText(this, "帳號不能包含 / 字元", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(account).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        Toast.makeText(this, "帳號已存在", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val userData = hashMapOf(
                        "account" to account,
                        "password" to password,
                    )

                    db.collection("users")
                        .document(account)
                        .set(userData)
                        .addOnSuccessListener {

                            // =============================
                            // 註冊成功 → 建立預設分類（支出 + 收入）
                            // =============================

                            val userCategories = db.collection("users")
                                .document(account)
                                .collection("categories")

                            // 支出分類（含 未分類）
                            val expenseCategories = listOf(
                                mapOf("name" to "未分類", "type" to "expense", "sortOrder" to 0, "fixed" to true),
                                mapOf("name" to "食", "type" to "expense", "sortOrder" to 1, "fixed" to false),
                                mapOf("name" to "衣", "type" to "expense", "sortOrder" to 2, "fixed" to false),
                                mapOf("name" to "住", "type" to "expense", "sortOrder" to 3, "fixed" to false),
                                mapOf("name" to "行", "type" to "expense", "sortOrder" to 4, "fixed" to false),
                            )

                            // 收入分類（含 未分類）
                            val incomeCategories = listOf(
                                mapOf("name" to "未分類", "type" to "income", "sortOrder" to 0, "fixed" to true),
                                mapOf("name" to "薪水", "type" to "income", "sortOrder" to 1, "fixed" to false),
                                mapOf("name" to "獎金", "type" to "income", "sortOrder" to 2, "fixed" to false),
                                mapOf("name" to "其他", "type" to "income", "sortOrder" to 3, "fixed" to false),
                            )

                            // 寫入 Firestore（autoId）
                            for (c in expenseCategories) userCategories.add(c)
                            for (c in incomeCategories) userCategories.add(c)

                            // =============================

                            Toast.makeText(this, "註冊成功", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "註冊失敗", Toast.LENGTH_SHORT).show()
                        }
                }
        }
        binding.registerBtBack.setOnClickListener {
            finish()
        }
    }
}