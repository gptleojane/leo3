package com.example.leo3.ui.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.leo3.R
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.data.model.CategoryItem
import com.example.leo3.databinding.ActivityAddBillBinding
import com.example.leo3.databinding.ActivityExchangeRateBinding
import com.example.leo3.ui.adapter.CategoryAdapter
import com.example.leo3.util.AppFlags
import com.example.leo3.util.UserManager
import com.google.firebase.Timestamp
import java.util.Calendar

class ExchangeRate : AppCompatActivity() {

    private lateinit var binding: ActivityExchangeRateBinding



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



    }
}
