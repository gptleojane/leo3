package com.example.leo3.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.leo3.data.firebase.FirestoreHelper
import com.example.leo3.databinding.QuickAddDialogAddBinding
import com.example.leo3.util.UserManager
import java.util.Calendar

class QuickAddDialog : DialogFragment() {

    var onAdded: (() -> Unit)? = null
    private var _binding: QuickAddDialogAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = QuickAddDialogAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        //  設置寬度
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.7).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupNumberPad()

        binding.npExpense.setOnClickListener { save("expense") }
        binding.npIncome.setOnClickListener { save("income") }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupNumberPad() {

        fun append(num: String) {
            val raw = binding.qaddTietAmount.text.toString().replace(",", "")

            // 不允許第一碼就是 0
            if (raw == "" && num == "0") return

            // ⭐ 限制最多  碼
            if (raw.length >= 12) return


            val newValue = raw + num
            binding.qaddTietAmount.setText("%,d".format(newValue.toLong()))
        }

        binding.np0.setOnClickListener { append("0") }
        binding.np1.setOnClickListener { append("1") }
        binding.np2.setOnClickListener { append("2") }
        binding.np3.setOnClickListener { append("3") }
        binding.np4.setOnClickListener { append("4") }
        binding.np5.setOnClickListener { append("5") }
        binding.np6.setOnClickListener { append("6") }
        binding.np7.setOnClickListener { append("7") }
        binding.np8.setOnClickListener { append("8") }
        binding.np9.setOnClickListener { append("9") }
    }

    private fun save(type: String) {
        val amountStr = binding.qaddTietAmount.text.toString().replace(",", "")

        if (amountStr.isEmpty()) {
            Toast.makeText(requireContext(), "請輸入金額", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toLong()

        val cal = Calendar.getInstance()
        val timestamp = com.google.firebase.Timestamp(cal.time)
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val weekDay = cal.get(Calendar.DAY_OF_WEEK)

        val account = UserManager.getAccount(requireContext()) ?: return

        // ⭐ 第 1 步：取得同類型的所有分類（包含未分類）
        FirestoreHelper.getCategories(account, type) { list ->

            // 找到「未分類」
            val defaultCategoryId =
                list.firstOrNull { it.name == "未分類" }?.id ?: ""

            // ⭐ 第 2 步：決定要新增的資料內容
            val billData = hashMapOf(
                "type" to type,
                "amount" to amount,
                "note" to "",
                "date" to timestamp,
                "year" to year,
                "month" to month,
                "day" to day,
                "weekDay" to weekDay,
                "categoryId" to defaultCategoryId
            )

            // ⭐ 第 3 步：新增資料
            FirestoreHelper.addBill(account, billData) {
                Toast.makeText(requireContext(), "新增成功", Toast.LENGTH_SHORT).show()

                onAdded?.invoke()
                dismiss()
            }
        }
    }

}