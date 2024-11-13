package com.example.haralertsystem.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.haralertsystem.databinding.FragmentHomeBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.haralertsystem.shared.SharedViewModel
import com.example.haralertsystem.R
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.haralertsystem.ui.alerts.AlertAdapter
import android.graphics.Color

class HomeFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels() // Shared ViewModel
    private lateinit var alertAdapter: AlertAdapter
    private lateinit var homeAdapter: HomeAdapter

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sensorNames = sharedViewModel.sensorNames
        homeAdapter = HomeAdapter(listOf(), sensorNames, listOf())

        val alertDescription = binding.root.findViewById<TextView>(R.id.alertDescription)

        alertAdapter = AlertAdapter(mutableListOf())
        binding.recyclerViewGasDetails.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewGasDetails.adapter = homeAdapter

        sharedViewModel.alert.observe(viewLifecycleOwner) { desc ->
            alertDescription.text = desc.description
        }

        sharedViewModel.sensorData.observe(viewLifecycleOwner) { data ->
            val latestValues = sensorNames.map { sensorName ->
                data[sensorName]?.lastOrNull() ?: "N/A"
            }

            val chartData = sensorNames.map { sensorName ->
                data[sensorName] ?: emptyList()
            }

            homeAdapter.updateNewItems(latestValues, chartData)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
