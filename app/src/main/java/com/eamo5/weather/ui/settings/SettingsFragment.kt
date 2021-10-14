package com.eamo5.weather.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eamo5.weather.R
import com.eamo5.weather.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val settingsRecyclerView = root.findViewById<RecyclerView>(R.id.settingsRecyclerView)
        val sharedPref = this.activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            if (it.getString("locationSettings", "?") == "?") {
                with(sharedPref.edit()) {
                    this.putString("locationSettings", "Melbourne")
                    this.apply()
                }
            }

            if (it.getString("temperatureMetric", "?") == "?") {
                with(sharedPref.edit()) {
                    this.putString("temperatureMetric", "°C")
                    this.apply()
                }
            }

            val settingsList = mutableListOf(
                Settings(
                    "Location", it.getString("locationSettings", "?"),
                    R.drawable.ic_location_on_black_24dp
                ),
                Settings(
                    "Metric", it.getString("temperatureMetric", "?"),
                    R.drawable.ic_thermostat_black_24dp
                )
            )
            // RecyclerView
            settingsRecyclerView.adapter = activity?.let { context ->
                SettingsAdapter(settingsList, context) {
                }
            }
            settingsRecyclerView.layoutManager = LinearLayoutManager(activity)
            val dividerItemDecoration =
                DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
            settingsRecyclerView.addItemDecoration(dividerItemDecoration)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}