package com.example.leo3.util

object DataVersionManager {

    private var dataVersion: Int = 0

    fun updateDataVersion() {
        dataVersion++
    }

    fun getVersion(): Int {
        return dataVersion
    }
}

