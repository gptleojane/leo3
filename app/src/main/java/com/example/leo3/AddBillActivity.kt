package com.example.leo3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.leo3.databinding.ActivityAddBillBinding

class AddBillActivity : AppCompatActivity() {

    //    省去findViewById必要的宣告
    private lateinit var binding: ActivityAddBillBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_bill)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityAddBillBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.addbillBtAdd.setOnClickListener {


            finish()
        }

        binding.addbillBtBack.setOnClickListener {
            finish()
        }

    }
}