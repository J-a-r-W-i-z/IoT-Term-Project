package com.example.haralertsystem.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.haralertsystem.databinding.HomeDetailBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color

class HomeAdapter(
    private var homeItems: List<String>,
    private var sensorNames:  List<String>,
    private var sensorValues: List<List<String>>
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(private val binding: HomeDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, position: Int) {
            binding.gasTitle.text = sensorNames[position]
            binding.gasValue.text = "PPM: $item"
            setData(binding.gasChart, sensorValues[position])
        }

        private fun setData(chart: LineChart, values: List<String>) {
            val chartValues = ArrayList<Entry>()
            for (i in values.indices) {
                val `val` = values[i].toFloat()
                chartValues.add(Entry(i.toFloat(), `val`))
            }
            val set1 = LineDataSet(chartValues, "PPM Values")
            set1.color = Color.BLUE
            set1.lineWidth = 1.5f
            set1.setDrawValues(false)
            set1.setDrawCircles(false)
            set1.mode = LineDataSet.Mode.CUBIC_BEZIER

            chart.data = LineData(set1)

            chart.xAxis.setDrawGridLines(false)
            chart.axisLeft.setDrawGridLines(false)
            chart.axisRight.setDrawGridLines(false)

            chart.axisLeft.axisMinimum = 0f
            chart.axisLeft.axisMaximum = 1000f

            chart.description.isEnabled = false

            chart.axisRight.isEnabled = false
            chart.xAxis.setDrawLabels(false)
            chart.axisLeft.setDrawLabels(true)

            chart.invalidate()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val binding = HomeDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.bind(homeItems[position], position)
    }

    override fun getItemCount(): Int = homeItems.size

    fun updateNewItems(newItems: List<String>, newChartData: List<List<String>>) {
        homeItems = newItems
        sensorValues = newChartData
        notifyDataSetChanged()
    }
}

