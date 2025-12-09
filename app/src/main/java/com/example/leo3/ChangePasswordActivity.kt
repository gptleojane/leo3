package com.example.leo3

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityChangePasswordBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.example.leo3.util.UserManager

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.cpBtConfirm.setOnClickListener {
            val oldPwd = binding.cpTietOldpassword.text.toString()
            val newPwd = binding.cpTietNewpassword.text.toString()
            val confirmPwd = binding.cpTietConfirmnewpassword.text.toString()

            if (oldPwd.isBlank() || newPwd.isBlank() || confirmPwd.isBlank()) {
                Toast.makeText(this, "不能為空白", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPwd.length < 3) {
                Toast.makeText(this, "密碼長度至少3位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (oldPwd == newPwd) {
                Toast.makeText(this, "新舊密碼不能相同", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPwd != confirmPwd) {
                Toast.makeText(this, "兩次新密碼不相同", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 取得使用者帳號
            val account = UserManager.getAccount(this) ?: return@setOnClickListener
            // 取得 Firestore 使用者文件
            val userRef = db.collection("users").document(account)

            userRef.get().addOnSuccessListener { doc ->
                val currentPwd = doc.getString("password") ?: ""
                // 檢查舊密碼是否正確
                if (currentPwd != oldPwd) {
                    Toast.makeText(this, "舊密碼錯誤", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                // 更新密碼
                userRef.update("password", newPwd)
                    .addOnSuccessListener {
                        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }

            binding.cpBtBack.setOnClickListener {
                finish()
            }
        }
    }
}