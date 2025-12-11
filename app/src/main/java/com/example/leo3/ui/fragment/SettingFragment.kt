package com.example.leo3.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.leo3.databinding.FragmentSettingBinding
import com.example.leo3.ui.activity.ChangePasswordActivity
import com.example.leo3.ui.activity.LoginActivity
import com.example.leo3.util.UserManager

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val account= UserManager.getAccount(requireContext())
        binding.settingFragmentUserInfoEmail.text=account

        // 外觀主題
        binding.settingFragmentPersonalizeThemeSwitch.setOnClickListener {
            toggleTheme()
        }

        binding.settingFragmentPersonalizeChangePassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        binding.settingFragmentPersonalizeSignOut.setOnClickListener {
            UserManager.logout(requireContext())
            Toast.makeText(requireContext(), "登出成功", Toast.LENGTH_SHORT).show()


            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        // ================= 功能設定 =================
        binding.settingFragmentSetExportData.setOnClickListener {
            // TODO: 匯出資料 (CSV)
        }




        // 關於2
        binding.settingFragmentOtherAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun toggleTheme() {
        val theme = UserManager.getTheme(requireContext())

        if (theme=="dark") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            UserManager.setTheme(requireContext(), "light") // 淺色
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            UserManager.setTheme(requireContext(), "dark") // 深色
        }
    }



    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("APP名稱(考慮刪除)")
            .setMessage("版本：1.0.0")
            .setPositiveButton("確定", null)
            .show()
    }




}