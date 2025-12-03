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
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                            Toast.makeText(this, "註冊成功", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
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