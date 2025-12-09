package com.example.leo3.ui.model

sealed class HomeUiModel {

    data class Header(
        val date: String,        // ex: 2025/12/08
        val weekDay: Int,        // 星期
        val totalAmount: Int     // 當天所有明細的加總
    ) : HomeUiModel()

    data class Item(
        val bill: Bill           // 一筆明細資料
    ) : HomeUiModel()
}
