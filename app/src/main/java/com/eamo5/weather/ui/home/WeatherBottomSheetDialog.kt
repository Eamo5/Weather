package com.eamo5.weather.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.eamo5.weather.R
import com.eamo5.weather.api.ConsolidatedWeather
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.math.roundToInt

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
            "Wind: ${data.wind_direction_compass + " " + formatWindSpeed(data.wind_speed)}"
        binding.findViewById<TextView>(R.id.bottomSheetHumidity).text =
            "Humidity: ${data.humidity}%"
        binding.findViewById<TextView>(R.id.bottomSheetAirPressure).text =
            "Pressure: ${convertAirPressure(data.air_pressure)}"
        binding.findViewById<TextView>(R.id.bottomSheetAirPredictability).text =
            "Predictability: ${data.predictability}%"
        return binding
    }

    private fun formatWindSpeed(speed: Double): String {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        return if (prefManager.getString("speed", "") != "kilometers"){
            speed.roundToInt().toString() + " mph"
        } else {
            convertToKilometers(speed).toString() + " km/h"
        }
    }

    private fun convertToKilometers(miles: Double): Int  {
        return (miles * 1.603).roundToInt()
    }

    private fun convertAirPressure(pressure: Double): String {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        return when {
            prefManager.getString("pressure", "") == "pascal" ->
                (pressure * 100).roundToInt().toString() + " pa"

            prefManager.getString("pressure", "") == "psi" ->
                (pressure / 68.948).roundToInt().toString() + " psi"

            else -> pressure.roundToInt().toString() + " mbar"
        }
    }

}