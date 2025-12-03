package com.example.leo3

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    //    省去findViewById必要的宣告
    private lateinit var binding: ActivityLoginBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                    val sp = getSharedPreferences("user", MODE_PRIVATE)
                    sp.edit().putString("account", account).apply()

                    Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
        }
        binding.loginBtRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
