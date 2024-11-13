package com.example.haralertsystem

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(private var items: List<String>, private val onClick: (Int) -> Unit) :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            onClick(position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.itemTitle)

        fun bind(value: String) {
            textView.text = value
        }
    }
}
