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

    private var amount: Long = 0L

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
            (resources.displayMetrics.widthPixels * 0.8).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        renderAmount()
        setupNumberPad()

        binding.npExpense.setOnClickListener { addBill("expense") }
        binding.npIncome.setOnClickListener { addBill("income") }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupNumberPad() {

        fun append(digit: Int) {
            val newValue =
                if (amount == 0L) digit.toLong()
                else amount * 10 + digit

            if (newValue > 9_999_999_999L) {
                Toast.makeText(requireContext(), "超過輸入上限", Toast.LENGTH_SHORT).show()
                return
            }

            amount = newValue
            renderAmount()
        }

        binding.np0.setOnClickListener { append(0) }
        binding.np1.setOnClickListener { append(1) }
        binding.np2.setOnClickListener { append(2) }
        binding.np3.setOnClickListener { append(3) }
        binding.np4.setOnClickListener { append(4) }
        binding.np5.setOnClickListener { append(5) }
        binding.np6.setOnClickListener { append(6) }
        binding.np7.setOnClickListener { append(7) }
        binding.np8.setOnClickListener { append(8) }
        binding.np9.setOnClickListener { append(9) }
    }

    private fun addBill(type: String) {

        val cal = Calendar.getInstance()
        val timestamp = com.google.firebase.Timestamp(cal.time)

        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val weekDay = cal.get(Calendar.DAY_OF_WEEK)

        val account = UserManager.getAccount(requireContext()) ?: return

        //  取得「未分類」的autoID
        FirestoreHelper.getCategories(account, type) { list ->
            val defaultCategoryId =
                list.firstOrNull { it.name == "未分類" }?.id ?: return@getCategories

            //  第 2 步：決定要新增的資料內容
            val billData = hashMapOf(
                "type" to type,
                "amount" to amount,
                "note" to "未分類",
                "date" to timestamp,
                "year" to year,
                "month" to month,
                "day" to day,
                "weekDay" to weekDay,
                "categoryId" to defaultCategoryId
            )

            // ⭐ 第 3 步：新增資料
            FirestoreHelper.addBill(account, billData) {
                if (!isAdded) return@addBill

                context?.let {
                    Toast.makeText(it, "新增成功", Toast.LENGTH_SHORT).show()
                }
                onAdded?.invoke()
                if (isAdded) dismissAllowingStateLoss()
            }
        }
    }

    private fun renderAmount() {
        binding.qaddTvAmount.setText(
            "$" + "%,d".format(amount)
        )
    }

}