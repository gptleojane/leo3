package com.example.leo3.util

import android.content.Context

object UserManager {
    private const val PREF = "user"
    private const val KEY_ACCOUNT = "account"

    // 儲存目前登入的帳號
    fun setAccount(context: Context, account: String) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_ACCOUNT, account).apply()
    }

    // 取得目前登入的帳號
    fun getAccount(context: Context): String? {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.getString(KEY_ACCOUNT, null)
    }

    // 登出（清除帳號資料）
    fun logout(context: Context) {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        sp.edit().remove(KEY_ACCOUNT).apply()
    }
}
