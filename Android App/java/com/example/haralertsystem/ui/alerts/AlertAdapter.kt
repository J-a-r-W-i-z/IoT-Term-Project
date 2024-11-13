package com.example.haralertsystem.ui.alerts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.haralertsystem.databinding.ItemAlertBinding

class AlertAdapter(private var alerts: MutableList<AlertModel>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(alert: AlertModel) {
            binding.alertTitle.text = alert.title
//            binding.alertDescription.text = alert.description
            binding.root.visibility = if (alert.title == "No Alert") View.GONE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding =
            ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alerts[position])
    }

    override fun getItemCount(): Int = alerts.size

    fun updateAlerts(newAlerts: List<AlertModel>) {
        alerts = newAlerts.toMutableList()
        notifyDataSetChanged()
    }
}
