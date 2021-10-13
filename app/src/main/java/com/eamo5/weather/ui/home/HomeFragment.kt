package com.eamo5.weather.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.eamo5.weather.R
import com.eamo5.weather.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var location: TextView
    private lateinit var currentTemp: TextView
    private lateinit var day1Weather: TextView
    private lateinit var day2Weather: TextView
    private lateinit var day3Weather: TextView
    private lateinit var day4Weather: TextView
    private lateinit var day5Weather: TextView
    private lateinit var day6Weather: TextView

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/

        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val day1 = root.findViewById<TextView>(R.id.day1)
        val day2 = root.findViewById<TextView>(R.id.day2)
        val day3 = root.findViewById<TextView>(R.id.day3)
        val day4 = root.findViewById<TextView>(R.id.day4)
        val day5 = root.findViewById<TextView>(R.id.day5)
        val day6 = root.findViewById<TextView>(R.id.day6)

        when (day) {
            Calendar.SUNDAY -> {
                day1.text = "Today"
                day2.text = "Monday"
                day3.text = "Tuesday"
                day4.text = "Wednesday"
                day5.text = "Thursday"
                day6.text = "Friday"
            }
            Calendar.MONDAY -> {
                day1.text = "Today"
                day2.text = "Tuesday"
                day3.text = "Wednesday"
                day4.text = "Thursday"
                day5.text = "Friday"
                day6.text = "Saturday"
            }
            Calendar.TUESDAY -> {
                day1.text = "Today"
                day2.text = "Wednesday"
                day3.text = "Thursday"
                day4.text = "Friday"
                day5.text = "Saturday"
                day6.text = "Sunday"
            }
            Calendar.WEDNESDAY -> {
                day1.text = "Today"
                day2.text = "Thursday"
                day3.text = "Friday"
                day4.text = "Saturday"
                day5.text = "Sunday"
                day6.text = "Monday"
            }
            Calendar.THURSDAY -> {
                day1.text = "Today"
                day2.text = "Friday"
                day3.text = "Saturday"
                day4.text = "Sunday"
                day5.text = "Monday"
                day6.text = "Tuesday"
            }
            Calendar.FRIDAY -> {
                day1.text = "Today"
                day2.text = "Saturday"
                day3.text = "Sunday"
                day4.text = "Monday"
                day5.text = "Tuesday"
                day6.text = "Wednesday"
            }
            Calendar.SATURDAY -> {
                day1.text = "Today"
                day2.text = "Sunday"
                day3.text = "Monday"
                day4.text = "Tuesday"
                day5.text = "Wednesday"
                day6.text = "Thursday"
            }
        }

        // Get element IDs
        location = root.findViewById(R.id.location)
        currentTemp = root.findViewById(R.id.temperature)
        day1Weather = root.findViewById(R.id.day1Weather)
        day2Weather = root.findViewById(R.id.day2Weather)
        day3Weather = root.findViewById(R.id.day3Weather)
        day4Weather = root.findViewById(R.id.day4Weather)
        day5Weather = root.findViewById(R.id.day5Weather)
        day6Weather = root.findViewById(R.id.day6Weather)


        // Get Shared Preferences
        val sharedPref = this.activity?.getPreferences(Context.MODE_PRIVATE)
        location.text = sharedPref?.getString("currentLocation", "-")
        currentTemp.text = sharedPref?.getString("currentTemp", "-")
        day1Weather.text = sharedPref?.getString("day1Weather", "-")
        day2Weather.text = sharedPref?.getString("day2Weather", "-")
        day3Weather.text = sharedPref?.getString("day3Weather", "-")
        day4Weather.text = sharedPref?.getString("day4Weather", "-")
        day5Weather.text = sharedPref?.getString("day5Weather", "-")
        day6Weather.text = sharedPref?.getString("day6Weather", "-")

        return root
    }

    override fun onPause() {
        super.onPause()
        val sharedPref: SharedPreferences? = this.activity?.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            this?.putString("currentLocation", location.text.toString())
            this?.putString("currentTemp", currentTemp.text.toString())
            this?.putString("day1Weather", day1Weather.text.toString())
            this?.putString("day2Weather", day2Weather.text.toString())
            this?.putString("day3Weather", day3Weather.text.toString())
            this?.putString("day4Weather", day4Weather.text.toString())
            this?.putString("day5Weather", day5Weather.text.toString())
            this?.putString("day6Weather", day6Weather.text.toString())
            this?.apply()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}