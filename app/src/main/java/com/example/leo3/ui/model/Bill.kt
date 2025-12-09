package com.example.leo3.ui.model

data class Bill(
    val type: String = "",
    val amount: Int = 0,
    val note: String = "",
    val date: com.google.firebase.Timestamp? = null,
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    val weekDay: Int = 0,
    val categoryId: String = "",
    var categoryName: String = ""
)



