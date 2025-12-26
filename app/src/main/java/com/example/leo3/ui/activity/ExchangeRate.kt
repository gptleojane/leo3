package com.example.leo3.ui.activity


import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityExchangeRateBinding


class ExchangeRate : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeRateBinding

    private var amountTwd: Long = 0L

    private val currencyList = listOf(
        "台幣 (TWD)",
        "美元 (USD)",
        "日圓 (JPY)",
        "人民幣 (CNY)",
        "歐元 (EUR)",
        "港幣 (HKD)",
        "韓元 (KRW)",
        "新加坡幣 (SGD)",
        "英鎊 (GBP)",
        "澳幣 (AUD)",
        "加幣 (CAD)",
        "瑞士法郎 (CHF)",
        "泰銖 (THB)",
        "馬來西亞令吉 (MYR)",
        "菲律賓披索 (PHP)",
        "印尼盾 (IDR)",
        "越南盾 (VND)",
        "紐西蘭幣 (NZD)",
        "印度盧比 (INR)",
        "澳門幣 (MOP)"
    )

    private val rateMap = mapOf(
        "台幣 (TWD)" to 1.0,
        "美元 (USD)" to 31.5,
        "日圓 (JPY)" to 0.21,
        "人民幣 (CNY)" to 4.4,
        "歐元 (EUR)" to 34.0,
        "港幣 (HKD)" to 4.03,
        "韓元 (KRW)" to 0.024,
        "新加坡幣 (SGD)" to 23.3,
        "英鎊 (GBP)" to 39.5,
        "澳幣 (AUD)" to 21.0,
        "加幣 (CAD)" to 23.5,
        "瑞士法郎 (CHF)" to 35.8,
        "泰銖 (THB)" to 0.88,
        "馬來西亞令吉 (MYR)" to 6.7,
        "菲律賓披索 (PHP)" to 0.56,
        "印尼盾 (IDR)" to 0.0020,
        "越南盾 (VND)" to 0.0013,
        "紐西蘭幣 (NZD)" to 19.6,
        "印度盧比 (INR)" to 0.38,
        "澳門幣 (MOP)" to 3.9
    )

    private lateinit var left: Side
    private lateinit var right: Side



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityExchangeRateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // 初始化左右兩側（都預設 TWD）
        left = Side(
            spinner = binding.exchangeSpinnerFrom1,
            result = binding.exchangeTvResults1,
            favBtn = binding.exchangeTvResults1Save1
        )

        right = Side(
            spinner = binding.exchangeSpinnerFrom2,
            result = binding.exchangeTvResults2,
            favBtn = binding.exchangeTvResults2Save1
        )

        setupSpinners()
        setupFavoriteButtons()
        setupNumberPad()

        binding.exchangeBtnBack.setOnClickListener { finish() }

        renderAll()


    }

    private fun setupSpinners() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            currencyList
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        fun bind(side: Side) {
            side.spinner.adapter = adapter
            side.spinner.setSelection(0) // TWD

            side.spinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val selected = currencyList[position]
                        if (selected != side.current) {
                            side.previous = side.current
                            side.current = selected
                            updateFavButton(side)
                        }
                        renderAll()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {}
                }
        }

        bind(left)
        bind(right)
    }


    private fun setupFavoriteButtons() {
        fun bind(side: Side) {
            updateFavButton(side)

            side.favBtn.setOnClickListener {
                val prev = side.previous ?: return@setOnClickListener
                val idx = currencyList.indexOf(prev)
                if (idx >= 0) {
                    side.spinner.setSelection(idx)
                }
            }
        }

        bind(left)
        bind(right)
    }

    private fun updateFavButton(side: Side) {
        if (side.previous == null) {
            side.favBtn.text = "—"
            side.favBtn.isEnabled = false
        } else {
            side.favBtn.text = side.previous
            side.favBtn.isEnabled = true
        }
    }

    private fun setupNumberPad() {

        fun append(num: Int) {
            val next = amountTwd * 10 + num
            if (next > 999_999_999L) {
                Toast.makeText(this, "超過輸入上限", Toast.LENGTH_SHORT).show()
                return

            }
            amountTwd = next
            renderAll()
        }

        binding.np0.setOnClickListener { append(0) }
        binding.np1.setOnClickListener { append(1) }
        binding.np2.setOnClickListener { append(2) }
        binding.np3.setOnClickListener { append(3) }
        binding.np4.setOnClickListener { append(4) }
        binding.np5.setOnClickListener { append(5) }
        binding.np6.setOnClickListener { append(6) }
        binding.np7.setOnClickListener { append(7) }
        binding.np8.setOnClickListener { append(8) }
        binding.np9.setOnClickListener { append(9) }

        // 刪除一碼
        binding.npDel.setOnClickListener {
            amountTwd /= 10
            renderAll()

        }

        // AC：直接回到 $0
        binding.npAc.setOnClickListener {
            amountTwd = 0L
            renderAll()
        }
    }

    private fun renderAll() {
        // 上方永遠顯示 TWD 金額（你說最原始只有台幣）
        binding.exchangeTvAmount.text = "TWD ${"%,d".format(amountTwd)}"

        renderSide(left)
        renderSide(right)
    }

    private fun renderSide(side: Side) {
        val rate = rateMap[side.current] ?: 1.0
        val value = amountTwd.toDouble() / rate
        side.result.text = "${"%,.2f".format(value)}"
    }

    private data class Side(
        val spinner: Spinner,
        val result: TextView,
        val favBtn: Button,
        var current: String = "TWD",
        var previous: String? = null
    )
}


