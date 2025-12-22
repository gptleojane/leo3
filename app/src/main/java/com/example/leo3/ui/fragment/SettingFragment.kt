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
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.FragmentSettingBinding
import com.example.leo3.ui.activity.ChangePasswordActivity
import com.example.leo3.ui.activity.EditCategoryActivity
import com.example.leo3.ui.activity.LoginActivity
import com.example.leo3.util.AppFlags
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


        val account = UserManager.getAccount(requireContext())
        binding.settingFragmentUserInfoEmail.text = account


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

        binding.settingFragmentPersonalizeCleanAllData.setOnClickListener {
            val account = UserManager.getAccount(requireContext()) ?: return@setOnClickListener
            AlertDialog.Builder(requireContext())
                .setTitle("確認清除資料")
                .setMessage("這個動作會刪除所有帳單，且無法復原，確定要繼續嗎？")
                .setPositiveButton("確認") { _, _ ->
                    FirestoreHelper.clearAllBills(
                        account,
                        onSuccess = {
                            Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show()

                            AppFlags.reloadData = true

                        },
                        onFail = {
                            Toast.makeText(context, "清除失敗", Toast.LENGTH_SHORT).show()
                        }

                    )

                }
                .setNegativeButton("取消", null)
                .show()
        }


        // ================= 功能設定 =================
        binding.settingFragmentSetExportData.setOnClickListener {
            // TODO: 匯出資料 (CSV)
        }

        binding.settingFragmentSetEditCategory.setOnClickListener {
            val intent = Intent(requireContext(), EditCategoryActivity::class.java)
            startActivity(intent)
        }





        binding.settingFragmentOtherAbout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("記帳APP")
                .setMessage("版本：1.0.0")
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}