package com.example.leo3.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerBtBack.setOnClickListener { finish() }

        binding.registerBtRegister.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val account = binding.registerTietAccount.text.toString()
        val password = binding.registerTietPassword.text.toString()

        // 基本驗證
        if (account.isBlank() || password.isBlank()) {
            toast("帳號密碼不能空白")
            return
        }
        if (password.length < 3) {
            toast("密碼至少 3 字")
            return
        }
        if (account.length > 20) {
            toast("帳號不能超過 20 字")
            return
        }
        if (account.contains("/")) {
            toast("帳號不能包含 / 字元")
            return
        }

        // ⭐ 第一步：檢查帳號是否已存在
        FirestoreHelper.getUser(account) { userData ->
            if (userData != null) {
                toast("帳號已存在")
                return@getUser
            }

            // ⭐ 第二步：建立使用者帳號
            FirestoreHelper.createUser(
                account = account,
                password = password,
                onSuccess = {
                    // ⭐ 第三步：建立預設分類
                    FirestoreHelper.createDefaultCategories(account) {

                        toast("註冊成功")

                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                },
                onFail = { e ->
                    toast("註冊失敗：${e.message}")
                }
            )
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
