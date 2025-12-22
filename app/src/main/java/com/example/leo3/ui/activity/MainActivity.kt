package com.example.leo3.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.leo3.util.AppFlags

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val recordFragment = RecordFragment()
    private val statFragment = StatFragment()
    private val settingFragment = SettingFragment()

    //預設首頁
    private var currentFragment: Fragment? = null


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

        //第一次進入main，show/hide建立全部fragment，並預設首頁home
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.main_fragment, homeFragment)
                .add(R.id.main_fragment, recordFragment).hide(recordFragment)
                .add(R.id.main_fragment, statFragment).hide(statFragment)
                .add(R.id.main_fragment, settingFragment).hide(settingFragment)
                .commit()

            currentFragment = homeFragment
        }

        setupBottomNavUI()
        setupFabUI()

    }

    override fun onResume() {
        super.onResume()

        reloadAllIfNeeded()
    }

    private fun setupBottomNavUI(){
        // Bottom Nav 切換 Fragment
        binding.mainBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showHideFragment(homeFragment)
                R.id.nav_record -> showHideFragment(recordFragment)
                R.id.nav_stat -> showHideFragment(statFragment)
                R.id.nav_setting -> showHideFragment(settingFragment)
            }
            true
        }
    }
    private fun setupFabUI(){
        // FAB 短按：快速記帳
        binding.mainFabAdd.setOnClickListener {

            Toast.makeText(this, "長按進入完整記帳", Toast.LENGTH_SHORT).show()

            val dialog = QuickAddDialog()
            dialog.onAdded = {

                AppFlags.reloadData=true
                reloadAllIfNeeded()

            }
            dialog.show(supportFragmentManager, "quick_add_dialog")
        }

        // FAB 長按：完整記帳
        binding.mainFabAdd.setOnLongClickListener {
            AppFlags.reloadData = true
            val intent = Intent(this, AddBillActivity::class.java)
            startActivity(intent)
            true
        }
    }

    private fun showHideFragment(target: Fragment) {
        if (currentFragment === target) return

        supportFragmentManager.beginTransaction().apply {
            currentFragment?.let { hide(it) }
            show(target)
        }.commit()

        currentFragment = target

        reloadAllIfNeeded()
    }

    private fun reloadAllIfNeeded() {
        if (AppFlags.reloadData) {
            AppFlags.reloadData = false
            homeFragment.reload()
            recordFragment.reload()
            statFragment.reload()
        }
    }




}