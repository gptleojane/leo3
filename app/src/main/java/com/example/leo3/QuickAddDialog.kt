package com.example.leo3

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import com.example.leo3.databinding.ActivityMainBinding
import com.example.leo3.databinding.QuickAddDialogAddBinding

class QuickAddDialog : DialogFragment() {

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

        binding.qaddBtOk.setOnClickListener {
            val amount = binding.qaddTietAmount.text.toString()

            if (amount.isNotEmpty()) {
                // TODO: 寫入資料
            }

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
