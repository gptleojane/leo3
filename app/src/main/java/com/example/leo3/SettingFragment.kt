package com.example.leo3

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.leo3.databinding.FragmentSettingBinding

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

        // ================= 個人化設定 =================
        binding.settingFragmentPersonalizeThemeSwitch.setOnClickListener {
            toggleTheme()
        }

        binding.settingFragmentPersonalizeChangePassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        binding.settingFragmentPersonalizeSignOut.setOnClickListener {
            Toast.makeText(requireContext(), "登出成功", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        // ================= 功能設定 =================
        binding.settingFragmentSetExportData.setOnClickListener {
            // TODO: 匯出資料 (CSV)
        }

        binding.settingFragmentSetRemind.setOnClickListener {
            // TODO: 記帳提醒
        }

        binding.settingFragmentSetFeedBack.setOnClickListener {
            // TODO: 意見回饋 Email
        }

        // ================= 其他 =================
        binding.settingFragmentOtherAbout.setOnClickListener {
            // TODO: 前往「關於」頁面
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun toggleTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (currentMode) {
            Configuration.UI_MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }


}
