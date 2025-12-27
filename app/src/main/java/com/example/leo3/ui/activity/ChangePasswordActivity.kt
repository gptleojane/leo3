package com.example.leo3.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.ActivityChangePasswordBinding
import com.example.leo3.util.UserManager

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadOldPassword()

        binding.cpTietOldpassword.apply {
            isEnabled = false
            isFocusable = false
        }

        binding.cpBtBack.setOnClickListener { finish() }

        binding.cpBtConfirm.setOnClickListener {
            changePassword()
        }

    }

    private fun changePassword() {
        val oldPwd = binding.cpTietOldpassword.text.toString()
        val newPwd = binding.cpTietNewpassword.text.toString()
        val confirmPwd = binding.cpTietConfirmnewpassword.text.toString()


        if (newPwd.isBlank() || confirmPwd.isBlank()) {
            Toast.makeText(this, "不能為空白", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPwd.length < 3) {
            Toast.makeText(this, "密碼至少 3 字", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPwd != confirmPwd) {
            Toast.makeText(this, "兩次新密碼不同", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPwd == oldPwd) {
            Toast.makeText(this, "新舊密碼不能相同", Toast.LENGTH_SHORT).show()
            return
        }

        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.updatePassword(
            account = account,
            newPassword = newPwd,
            onResult = {
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFail = { e ->
                Toast.makeText(this, "修改失敗", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun loadOldPassword() {
        val account = UserManager.getAccount(this) ?: return

        FirestoreHelper.getUser(account,onResult = { data ->
            val oldPwd = data?.get("password")?.toString() ?: return@getUser
            binding.cpTietOldpassword.setText(oldPwd)
        },onFail = {
            Toast.makeText(this,"網路錯誤，請稍後再試",Toast.LENGTH_SHORT).show()
        }
        )
    }

}

