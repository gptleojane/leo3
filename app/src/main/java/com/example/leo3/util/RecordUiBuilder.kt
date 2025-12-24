package com.example.leo3.util

import com.example.leo3.data.model.Bill
import com.example.leo3.data.model.RecordUiModel

object RecordUiBuilder {
    fun build(bills: List<Bill>): List<RecordUiModel> {
        if (bills.isEmpty()) return emptyList()

        val grouped = bills.groupBy {
            it.year * 10_000 + it.month * 100 + it.day
        }

        val result = mutableListOf<RecordUiModel>()

        grouped.toSortedMap(compareByDescending { it }).forEach { (_, dayBills) ->
            val first = dayBills.first()
            val total = dayBills.sumOf { it.amount }

            result.add(
                RecordUiModel.Header(
                    date = "${first.year}/${first.month}/${first.day}",
                    weekDay = first.weekDay,
                    totalAmount = total
                )
            )

            dayBills.sortedByDescending { it.date }.forEach {
                result.add(RecordUiModel.Item(it))
            }
        }
        return result
    }
}
