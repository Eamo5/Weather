package com.eamo5.weather.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.eamo5.weather.R

class SettingsAdapter(private val data: List<Settings>,
                      private val context: Context,
                      private val listener: (Settings) -> (Unit)) : RecyclerView.Adapter<SettingsAdapter.ViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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

        fun bind(item: Settings) {
            val settings = view.findViewById<TextView>(R.id.settings)
            val value = view.findViewById<TextView>(R.id.value)
            val icon = view.findViewById<ImageView>(R.id.icon)
            settings.text = item.settings
            value.text = item.value
            icon.setImageDrawable(AppCompatResources.getDrawable(context, item.icon))
            view.setOnClickListener {
                listener(item)
            }
        }
    }
}