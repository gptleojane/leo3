package com.example.leo3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityAddBillBinding

class AddBillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBillBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAddBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.addbillBtAdd.setOnClickListener {


            finish()
        }

        binding.addbillBtBack.setOnClickListener {
            finish()
        }

        // 數字按鍵（0～9）
        binding.np0.setOnClickListener { appendNumber("0") }
        binding.np1.setOnClickListener { appendNumber("1") }
        binding.np2.setOnClickListener { appendNumber("2") }
        binding.np3.setOnClickListener { appendNumber("3") }
        binding.np4.setOnClickListener { appendNumber("4") }
        binding.np5.setOnClickListener { appendNumber("5") }
        binding.np6.setOnClickListener { appendNumber("6") }
        binding.np7.setOnClickListener { appendNumber("7") }
        binding.np8.setOnClickListener { appendNumber("8") }
        binding.np9.setOnClickListener { appendNumber("9") }

// AC 清除
        binding.npAc.setOnClickListener { clearAll() }

//  刪除倒退
        binding.npDel.setOnClickListener { deleteLast() }


    }

//    建立千分為小數點
    private fun formatAmount(raw: String): String {
        if (raw.isEmpty()) return ""
        val num = raw.toLong()
        return "%,d".format(num)   // 會輸出 1,234 形式
    }

//新增的數字會加入在最後方，疊加方式
    private fun appendNumber(num: String) {
    // 取目前金額文字並移除逗號
    val current = binding.addbillTietAmount.text.toString().replace(",", "")
    val newValue = current + num

    // 格式化（加入千分位）
    val formatted = formatAmount(newValue)

    binding.addbillTietAmount.setText(formatted)
    }

    private fun deleteLast() {
        val current = binding.addbillTietAmount.text.toString().replace(",", "")
        if (current.isNotEmpty()) {
            val newValue = current.dropLast(1)
            binding.addbillTietAmount.setText(formatAmount(newValue))
        }
    }

    private fun clearAll() {
        binding.addbillTietAmount.setText("")
    }

}