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

        binding.cpBtBack.setOnClickListener { finish() }

        binding.cpBtConfirm.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPwd = binding.cpTietOldpassword.text.toString()
        val newPwd = binding.cpTietNewpassword.text.toString()
        val confirmPwd = binding.cpTietConfirmnewpassword.text.toString()

        // 基本驗證
        if (oldPwd.isBlank() || newPwd.isBlank() || confirmPwd.isBlank()) {
            toast("不能為空白")
            return
        }
        if (newPwd.length < 3) {
            toast("密碼至少 3 字")
            return
        }
        if (newPwd != confirmPwd) {
            toast("兩次新密碼不同")
            return
        }
        if (newPwd == oldPwd) {
            toast("新舊密碼不能相同")
            return
        }

        val account = UserManager.getAccount(this) ?: return

        // ⭐ 第一步：從 Firestore 取得使用者資料
        FirestoreHelper.getUser(account) { data ->
            if (data == null) {
                toast("無法取得使用者資料")
                return@getUser
            }

            val currentPwd = data["password"]?.toString() ?: ""

            if (currentPwd != oldPwd) {
                toast("舊密碼錯誤")
                return@getUser
            }

            // ⭐ 第二步：更新新密碼
            FirestoreHelper.updatePassword(
                account = account,
                newPassword = newPwd,
                onSuccess = {
                    toast("修改成功")
                    finish()
                },
                onFail = { e ->
                    toast("修改失敗：${e.message}")
                }
            )
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
