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
                        sortOrder = (doc.getLong("sortOrder") ?: 0).toInt(),
                        fixed = doc.getBoolean("fixed") ?: false
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
                val list = qs.documents.mapNotNull { doc ->
                    doc.toObject(Bill::class.java)?.apply { id = doc.id }
                }
                onResult(list)
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
                val list = qs.documents.mapNotNull { doc ->
                    doc.toObject(Bill::class.java)?.apply { id = doc.id }
                }
                onResult(list)
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
    fun updatePassword(
        account: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
        db.collection("users")
            .document(account)
            .update("password", newPassword)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFail(e) }
    }

    fun createUser(
        account: String,
        password: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
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

            mapOf("name" to "餐飲", "type" to "expense", "sortOrder" to 1, "fixed" to false),
            mapOf("name" to "交通", "type" to "expense", "sortOrder" to 2, "fixed" to false),
            mapOf("name" to "購物", "type" to "expense", "sortOrder" to 3, "fixed" to false),
            mapOf("name" to "住房", "type" to "expense", "sortOrder" to 4, "fixed" to false),
            mapOf("name" to "生活用品", "type" to "expense", "sortOrder" to 5, "fixed" to false),
            mapOf("name" to "娛樂", "type" to "expense", "sortOrder" to 6, "fixed" to false),
            mapOf("name" to "醫療", "type" to "expense", "sortOrder" to 7, "fixed" to false),
            mapOf("name" to "教育", "type" to "expense", "sortOrder" to 8, "fixed" to false),
            mapOf("name" to "通訊", "type" to "expense", "sortOrder" to 9, "fixed" to false),
            mapOf("name" to "其他支出", "type" to "expense", "sortOrder" to 10, "fixed" to false)
        )

        val incomeCategories = listOf(
            mapOf("name" to "未分類", "type" to "income", "sortOrder" to 0, "fixed" to true),

            mapOf("name" to "薪資收入", "type" to "income", "sortOrder" to 1, "fixed" to false),
            mapOf("name" to "獎金", "type" to "income", "sortOrder" to 2, "fixed" to false),
            mapOf("name" to "兼職收入", "type" to "income", "sortOrder" to 3, "fixed" to false),
            mapOf("name" to "投資收入", "type" to "income", "sortOrder" to 4, "fixed" to false),
            mapOf("name" to "其他收入", "type" to "income", "sortOrder" to 5, "fixed" to false)
        )

        val batch = db.batch()

        (expenseCategories + incomeCategories).forEach { cat ->
            val doc = userCategories.document() // autoId
            batch.set(doc, cat)
        }

        batch.commit().addOnSuccessListener { onSuccess() }
    }

    fun getBillsByYear(
        account: String,
        year: Int,
        onResult: (List<Bill>) -> Unit
    ) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { qs ->
                val list = qs.documents.mapNotNull { doc ->
                    doc.toObject(Bill::class.java)?.apply {
                        id = doc.id   // ★ 關鍵：補上 Firestore documentId
                    }
                }
                onResult(list)
            }
    }


    // ------------------------
// EditBill 專用
// ------------------------

    fun getBillById(account: String, billId: String, onResult: (Bill?) -> Unit) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .document(billId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }
                val bill = doc.toObject(Bill::class.java)
                bill?.id = doc.id
                onResult(bill)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }


    fun updateBill(
        account: String,
        billId: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit
    ) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .document(billId)
            .update(data)
            .addOnSuccessListener { onSuccess() }
    }

    fun deleteBill(
        account: String,
        billId: String,
        onSuccess: () -> Unit
    ) {
        db.collection("users")
            .document(account)
            .collection("bills")
            .document(billId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }

    fun addCategory(
        account: String,
        type: String,
        name: String,
        onSuccess: () -> Unit
    ) {
        val categoriesRef = db.collection("users")
            .document(account)
            .collection("categories")

        // 先找該 type 的最大 sortOrder
        categoriesRef
            .whereEqualTo("type", type)
            .orderBy("sortOrder", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { qs ->
                val maxSortOrder =
                    qs.documents.firstOrNull()?.getLong("sortOrder")?.toInt() ?: 0

                val data = mapOf(
                    "name" to name,
                    "type" to type,
                    "sortOrder" to (maxSortOrder + 1),
                    "fixed" to false
                )

                categoriesRef
                    .add(data)
                    .addOnSuccessListener { onSuccess() }
            }
    }

    fun updateCategory(
        account: String,
        categoryId: String,
        newName: String,
        onSuccess: () -> Unit
    ) {
        db.collection("users")
            .document(account)
            .collection("categories")
            .document(categoryId)
            .update("name", newName)
            .addOnSuccessListener { onSuccess() }
    }

    fun deleteCategory(
        account: String,
        categoryId: String,
        onSuccess: () -> Unit
    ) {
        db.collection("users")
            .document(account)
            .collection("categories")
            .document(categoryId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }

    fun deleteCategoryAndMoveBills(
        account: String,
        categoryId: String,
        categoryType: String,
        onSuccess: () -> Unit
    ) {
        val userRef = db.collection("users").document(account)
        val categoriesRef = userRef.collection("categories")
        val billsRef = userRef.collection("bills")

        // Step 1：找同 type 的「未分類」（fixed == true）
        categoriesRef
            .whereEqualTo("type", categoryType)
            .whereEqualTo("fixed", true)
            .limit(1)
            .get()
            .addOnSuccessListener { catSnap ->

                val unclassifiedId = catSnap.documents.first().id

                // Step 2：找出所有使用該分類的 bills
                billsRef
                    .whereEqualTo("categoryId", categoryId)
                    .get()
                    .addOnSuccessListener { billSnap ->

                        // Step 3：逐筆更新 bills（forEach）
                        val tasks = billSnap.documents.map { doc ->
                            doc.reference.update("categoryId", unclassifiedId)
                        }

                        // Step 4：等所有 bills 更新完成後，再刪分類
                        com.google.android.gms.tasks.Tasks
                            .whenAllComplete(tasks)
                            .addOnSuccessListener {
                                categoriesRef
                                    .document(categoryId)
                                    .delete()
                                    .addOnSuccessListener { onSuccess() }
                            }
                    }
            }
    }


    fun clearAllBills(
        account: String,
        onSuccess: () -> Unit,
        onFail: (Exception) -> Unit
    ) {
        val billsRef = db.collection("users")
            .document(account)
            .collection("bills")

        billsRef.get()
            .addOnSuccessListener { qs ->
                if (qs.isEmpty) {
                    onSuccess()
                    return@addOnSuccessListener
                }

                val batch = db.batch()
                qs.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }

                batch.commit()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFail(e) }
            }
            .addOnFailureListener { e ->
                onFail(e)
            }
    }


}
