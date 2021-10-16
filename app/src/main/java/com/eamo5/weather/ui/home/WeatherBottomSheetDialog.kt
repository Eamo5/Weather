package com.eamo5.weather.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.eamo5.weather.R
import com.eamo5.weather.api.ConsolidatedWeather
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WeatherBottomSheetDialog(private val data: ConsolidatedWeather,
                               private val location: String,
                               private val state: String) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        Log.d("TEST", state)
        (parentFragment as HomeFragment).setWeatherIcon(
            binding.findViewById(R.id.bottomSheetWeatherIcon), state)
        binding.findViewById<TextView>(R.id.bottomSheetLocation).text = location
        binding.findViewById<TextView>(R.id.bottomSheetSummary).text = data.weather_state_name
        binding.findViewById<TextView>(R.id.bottomSheetTempMax).text =
            "Max: ${(parentFragment as HomeFragment).formatTemp(data.max_temp)}"
        binding.findViewById<TextView>(R.id.bottomSheetTempMin).text =
            "Min: ${(parentFragment as HomeFragment).formatTemp(data.min_temp)}"
        binding.findViewById<TextView>(R.id.bottomSheetSpeed).text =
            "Wind: ${(parentFragment as HomeFragment).formatWindSpeed(data.wind_speed)}"
        return binding
    }

}