package com.eamo5.weather.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eamo5.weather.R

class SettingsAdapter(private val data: List<String>,
                        private val listener: (String) -> (Unit)) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater
            .inflate(R.layout.row_layout, parent, false) as View
        return ViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SettingsAdapter.ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val settings = view.findViewById<TextView>(R.id.settings)

        fun bind(item: String) {
            settings.text = item
            view.setOnClickListener {
                listener(item)
            }
        }
    }
}