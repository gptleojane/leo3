package com.example.leo3.data.model

sealed class RecordUiModel {

    data class Header(
        val date: String,        // ex: 2025/12/08
        val weekDay: Int,        // 星期
        val totalAmount: Long     // 當天所有明細的加總
    ) : RecordUiModel()

    data class Item(
        val bill: Bill           // 一筆明細資料
    ) : RecordUiModel()
}
