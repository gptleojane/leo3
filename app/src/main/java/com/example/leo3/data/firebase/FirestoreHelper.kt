package com.example.leo3.data.firebase

import com.example.leo3.data.model.Bill
import com.example.leo3.data.model.CategoryItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    // ------------------------
    // 分類
    // ------------------------
    fun getCategories(account: String, type: String, onResult: (List<CategoryItem>) -> Unit) {
        db.collection("users")
            .document(account)
            .collection("categories")
            .whereEqualTo("type", type)
            .orderBy("sortOrder")
            .get()
            .addOnSuccessListener { qs ->
                val list = qs.documents.map { doc ->
                    CategoryItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        type = type,
                        sortOrder = (doc.getLong("sortOrder") ?: 0).toInt()
                    )
                }
                onResult(list)
            }
    }

    // 取得全部分類（Home/Record 用）
    fun getAllCategories(account: String, onResult: (List<CategoryItem>) -> Unit) {
        db.collection("users")
            .document(account)
            .collection("categories")
            .orderBy("sortOrder")
            .get()
            .addOnSuccessListener { qs ->
                val list = qs.documents.map { doc ->
                    CategoryItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        sortOrder = (doc.getLong("sortOrder") ?: 0).toInt()
                    )
                }
                onResult(list)
            }
    }

    // ------------------------
    // 帳單
    // ------------------------
    fun addBill(account: String, data: Map<String, Any>, onSuccess: () -> Unit) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .add(data)
            .addOnSuccessListener { onSuccess() }
    }

    fun getBillsByMonth(account: String, year: Int, month: Int, onResult: (List<Bill>) -> Unit) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .whereEqualTo("year", year)
            .whereEqualTo("month", month)
            .orderBy("day", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { qs ->
                onResult(qs.toObjects(Bill::class.java))
            }
    }

    // 今日最新三筆
    fun getTodayThreeBills(
        account: String,
        year: Int,
        month: Int,
        day: Int,
        onResult: (List<Bill>) -> Unit
    ) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .whereEqualTo("year", year)
            .whereEqualTo("month", month)
            .whereEqualTo("day", day)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(3)
            .get()
            .addOnSuccessListener { qs ->
                onResult(qs.toObjects(Bill::class.java))
            }
    }

    // -----------------------------
// 取得使用者資料（為了讀取舊密碼）
// -----------------------------
    fun getUser(account: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users")
            .document(account)
            .get()
            .addOnSuccessListener { doc ->
                onResult(doc.data)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    // -----------------------------
// 更新使用者密碼
// -----------------------------
    fun updatePassword(account: String, newPassword: String, onSuccess: () -> Unit, onFail: (Exception) -> Unit) {
        db.collection("users")
            .document(account)
            .update("password", newPassword)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFail(e) }
    }

    fun createUser(account: String, password: String, onSuccess: () -> Unit, onFail: (Exception) -> Unit) {
        val data = mapOf(
            "account" to account,
            "password" to password
        )

        db.collection("users")
            .document(account)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFail(e) }
    }

    fun createDefaultCategories(account: String, onSuccess: () -> Unit) {

        val userCategories = db.collection("users")
            .document(account)
            .collection("categories")

        val expenseCategories = listOf(
            mapOf("name" to "未分類", "type" to "expense", "sortOrder" to 0, "fixed" to true),
            mapOf("name" to "食", "type" to "expense", "sortOrder" to 1, "fixed" to false),
            mapOf("name" to "衣", "type" to "expense", "sortOrder" to 2, "fixed" to false),
            mapOf("name" to "住", "type" to "expense", "sortOrder" to 3, "fixed" to false),
            mapOf("name" to "行", "type" to "expense", "sortOrder" to 4, "fixed" to false)
        )

        val incomeCategories = listOf(
            mapOf("name" to "未分類", "type" to "income", "sortOrder" to 0, "fixed" to true),
            mapOf("name" to "薪水", "type" to "income", "sortOrder" to 1, "fixed" to false),
            mapOf("name" to "獎金", "type" to "income", "sortOrder" to 2, "fixed" to false),
            mapOf("name" to "其他", "type" to "income", "sortOrder" to 3, "fixed" to false)
        )

        val batch = db.batch()

        (expenseCategories + incomeCategories).forEach { cat ->
            val doc = userCategories.document() // autoId
            batch.set(doc, cat)
        }

        batch.commit().addOnSuccessListener { onSuccess() }
    }



}
