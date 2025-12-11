package com.example.leo3.data.model

import com.google.firebase.Timestamp

data class Bill(
    val type: String = "",
    val amount: Int = 0,
    val note: String = "",
    val date: Timestamp? = null,
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    val weekDay: Int = 0,
    val categoryId: String = "",
    var categoryName: String = ""
)



