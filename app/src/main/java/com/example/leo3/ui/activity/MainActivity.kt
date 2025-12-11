package com.example.leo3.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.leo3.R
import com.example.leo3.databinding.ActivityMainBinding
import com.example.leo3.ui.dialog.QuickAddDialog
import com.example.leo3.ui.fragment.HomeFragment
import com.example.leo3.ui.fragment.RecordFragment
import com.example.leo3.ui.fragment.SettingFragment
import com.example.leo3.ui.fragment.StatFragment

class MainActivity : AppCompatActivity() {

    //    省去findViewById必要的宣告
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            v.setPadding(0, topInset, 0, 0)
            insets
        }
//        預設首頁
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }


        // Bottom Nav 切換 Fragment
        binding.mainBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_record -> replaceFragment(RecordFragment())
                R.id.nav_stat -> replaceFragment(StatFragment())
                R.id.nav_setting -> replaceFragment(SettingFragment())
            }
            true
        }

        // FAB 短按：快速記帳
        binding.mainFabAdd.setOnClickListener {
            Toast.makeText(this, "長按進入完整記帳", Toast.LENGTH_SHORT).show()

            val dialog = QuickAddDialog()
            dialog.onAdded = {

                // 找出目前顯示的 Fragment
                val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment)

                // 如果目前頁面是 RecordFragment → 重新載入資料
                if (currentFragment is RecordFragment) {
                    currentFragment.refreshData()
                }
            }

            dialog.show(supportFragmentManager, "quick_add_dialog")
        }


        // FAB 長按：完整記帳
        binding.mainFabAdd.setOnLongClickListener {
            val intent = Intent(this, AddBillActivity::class.java)
            startActivity(intent)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_fragment, fragment)
            .commit()
    }
}