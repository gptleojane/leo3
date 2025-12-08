package com.example.leo3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.leo3.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeSwitchMode.setOnClickListener {

        }

        binding.homeYearSelector.setOnClickListener {
            showYearPopupMenu()
        }

        binding.homeMonthSelector.setOnClickListener {
                showMonthPopupMenu()
        }


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMonthPopupMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeMonthSelector)

        val months = listOf(1,2,3,4,5,6,7,8,9,10,11,12)

        months.forEach { month ->
            popup.menu.add(month.toString())
        }

        popup.show()
    }


    private fun showYearPopupMenu() {
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), binding.homeYearSelector)

        val years = listOf(2025,2024,2023,2022,2021)

        years.forEach { year ->
            popup.menu.add(year.toString())
        }
        popup.show()
    }

}


