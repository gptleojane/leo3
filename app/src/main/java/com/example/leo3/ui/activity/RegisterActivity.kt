package com.example.leo3.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

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

        binding.registerBtBack.setOnClickListener { finish() }

        binding.registerBtRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val account = binding.registerTietAccount.text.toString()
        val password = binding.registerTietPassword.text.toString()

        val passwordRegex = Regex("^[A-Za-z0-9]+$")




        if (account.isBlank() || password.isBlank()) {
            Toast.makeText(this, "帳號密碼不能空白", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 3) {
            Toast.makeText(this, "密碼至少 3 字", Toast.LENGTH_SHORT).show()
            return
        }
        if (!passwordRegex.matches(password)) {
            Toast.makeText(this, "密碼只能包含英文與數字", Toast.LENGTH_SHORT).show()
            return
        }

        if (account.length > 20) {
            Toast.makeText(this, "帳號不能超過 20 字", Toast.LENGTH_SHORT).show()
            return
        }
        if (account.contains("/")) {
            Toast.makeText(this, "帳號不能包含 / 字元", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐ 第一步：檢查帳號是否已存在
        FirestoreHelper.getUser(account, onResult = { data ->
            if (data != null) {
                Toast.makeText(this, "帳號已存在", Toast.LENGTH_SHORT).show()
                return@getUser
            }

            //  第二步：建立使用者帳號
            FirestoreHelper.createUser(
                account,
                password,
                onResult = {
                    //  第三步：建立預設分類
                    FirestoreHelper.createDefaultCategories(account) {

                        Toast.makeText(this, "註冊成功", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                },
                onFail = { e ->
                    Toast.makeText(this, "註冊失敗：${e.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }, onFail = {
            Toast.makeText(this, "網路錯誤，請稍後再試", Toast.LENGTH_SHORT).show()
        })
    }
}
