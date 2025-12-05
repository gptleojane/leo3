package com.example.leo3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityLoginBinding
import com.example.leo3.util.UserManager
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        // 啟動APP時先套用使用者原先設定主題深淺
        val theme = UserManager.getTheme(this)
        when (theme) {
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)

        val account = UserManager.getAccount(this)
        if (account != null) {
            Toast.makeText(this, "正在載入你的資料…", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginBtLogin.setOnClickListener {
            val account = binding.loginTietAccount.text.toString()
            val password = binding.loginTietPassword.text.toString()

            if (account.isBlank() || password.isBlank()) {
                Toast.makeText(this, "帳號密碼不能空白", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(account).get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        Toast.makeText(this, "帳號不存在", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val userData = doc.data
                    if (userData?.get("password") != password) {
                        Toast.makeText(this, "密碼錯誤", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // 保存登入使用者帳號
                    UserManager.setAccount(this, account)

                    Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
        }


        binding.loginBtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
