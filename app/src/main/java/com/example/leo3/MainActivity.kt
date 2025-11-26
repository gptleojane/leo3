package com.example.leo3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

//    省去findViewById必要的宣告
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//      省去findViewById必要的宣告，後面就可以直接使用binding.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

            val toast=Toast.makeText(this,"長按進入完整記帳",Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.BOTTOM, 0, 500)  // 調整這裡
            toast.show()

            QuickAddDialog().show(
                supportFragmentManager,
                "quick_add_dialog"
            )
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
