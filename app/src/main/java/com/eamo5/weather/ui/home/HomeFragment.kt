package com.eamo5.weather.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.eamo5.weather.MainActivity
import com.eamo5.weather.R
import com.eamo5.weather.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var currentWeatherIcon: ImageView
    private lateinit var location: TextView
    private lateinit var currentTemp: TextView
    private lateinit var homeTextViews: List<TextView>
    private lateinit var homeImageViews: List<ImageView>

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // ViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get IDs
        currentWeatherIcon = root.findViewById(R.id.weatherIcon)
        location = root.findViewById(R.id.location)
        currentTemp = root.findViewById(R.id.temperature)

        homeTextViews = listOf(
            root.findViewById(R.id.day1Weather), root.findViewById(R.id.day2Weather),
            root.findViewById(R.id.day3Weather), root.findViewById(R.id.day4Weather),
            root.findViewById(R.id.day5Weather), root.findViewById(R.id.day6Weather))

        homeImageViews = listOf(
            root.findViewById(R.id.day1WeatherIcon), root.findViewById(R.id.day2WeatherIcon),
            root.findViewById(R.id.day3WeatherIcon), root.findViewById(R.id.day4WeatherIcon),
            root.findViewById(R.id.day5WeatherIcon), root.findViewById(R.id.day6WeatherIcon)
        )

        val days = listOf<TextView>(
            root.findViewById(R.id.day1), root.findViewById(R.id.day2),
            root.findViewById(R.id.day3), root.findViewById(R.id.day4),
            root.findViewById(R.id.day5), root.findViewById(R.id.day6),
        )

        // Set days of week
        days.forEachIndexed { index, textView ->
            val date = SimpleDateFormat("EEEE", Locale.ENGLISH)
            val calendar = GregorianCalendar()
            calendar.add(Calendar.DATE, index)
            val day = date.format(calendar.time)
            textView.text = day
        }

        // Restore shared preferences
        val sharedPref = this.activity?.getPreferences(Context.MODE_PRIVATE)

        // Today
        location.text = sharedPref?.getString("currentLocation", "-")
        currentTemp.text = sharedPref?.getString("currentTemp", "-")

        // This weeks temperature
        homeTextViews.forEachIndexed { index, textView ->
            textView.text = sharedPref?.getString("day${index}Weather", "-")
        }

        // Corresponding ImageViews
        homeImageViews.forEachIndexed { index, imageView ->
            sharedPref?.getString("state${index}", "-")?.let {
                if (index == 0) {
                    (activity as MainActivity).setWeatherIcon(currentWeatherIcon, it)
                }
                (activity as MainActivity).setWeatherIcon(imageView, it)
            }
        }

        // Observers
        homeViewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            this.location.text = location
        })
        homeViewModel.currentTemperature.observe(viewLifecycleOwner, { temperature ->
            currentTemp.text = temperature
        })
        homeViewModel.day1Weather.observe(viewLifecycleOwner, { day1Weather ->
            homeTextViews[0].text = day1Weather
        })
        homeViewModel.day2Weather.observe(viewLifecycleOwner, { day2Weather ->
            homeTextViews[1].text = day2Weather
        })
        homeViewModel.day3Weather.observe(viewLifecycleOwner, { day3Weather ->
            homeTextViews[2].text = day3Weather
        })
        homeViewModel.day4Weather.observe(viewLifecycleOwner, { day4Weather ->
            homeTextViews[3].text = day4Weather
        })
        homeViewModel.day5Weather.observe(viewLifecycleOwner, { day5Weather ->
            homeTextViews[4].text = day5Weather
        })
        homeViewModel.day6Weather.observe(viewLifecycleOwner, { day6Weather ->
            homeTextViews[5].text = day6Weather
        })

        return root
    }

    override fun onPause() {
        super.onPause()

        // Saved shared preferences
        val sharedPref: SharedPreferences? = this.activity?.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString("currentLocation", location.text.toString())
            this?.putString("currentTemp", currentTemp.text.toString())

            // Weather for days of week
            homeTextViews.forEachIndexed { index, textView ->
                this?.putString("day${index}Weather", textView.text.toString())
            }
            this?.apply()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}