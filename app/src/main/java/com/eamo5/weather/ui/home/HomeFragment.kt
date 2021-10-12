package com.eamo5.weather.ui.home

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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}