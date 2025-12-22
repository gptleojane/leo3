package com.example.leo3.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.ActivityAddBillBinding
import com.example.leo3.databinding.ActivityLoginBinding
import com.example.leo3.util.UserManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 若已登入 → 直接跳 MainActivity
        val currentAccount = UserManager.getAccount(this)
        if (currentAccount != null) {
            Toast.makeText(this, "正在載入你的資料…", Toast.LENGTH_SHORT).show()
            goMain()
            return
        }



        // 登入按鈕
        binding.loginBtLogin.setOnClickListener {
            login()
        }

        // 跳到註冊
        binding.loginBtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val account = binding.loginTietAccount.text.toString()
        val password = binding.loginTietPassword.text.toString()

        if (account.isBlank() || password.isBlank()) {
            toast("帳號或密碼不能空白")
            return
        }

        // ⭐ 使用 FirestoreHelper，而非直接用 FirebaseFirestore
        FirestoreHelper.getUser(account) { data ->

            if (data == null) {
                toast("帳號不存在")
                return@getUser
            }

            val pwd = data["password"]?.toString() ?: ""

            if (pwd != password) {
                toast("密碼錯誤")
                return@getUser
            }

            // ⭐ 登入成功 → 保存帳號
            UserManager.setAccount(this, account)

            toast("登入成功")
            goMain()
        }
    }

    private fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
