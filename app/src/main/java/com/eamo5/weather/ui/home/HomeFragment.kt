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

        when (day) {
            Calendar.SUNDAY -> {
                day1.text = "Sunday"
                day2.text = "Monday"
            }
            Calendar.MONDAY -> {
                day1.text = "Monday"
                day2.text = "Tuesday"
            }
            Calendar.TUESDAY -> {
                day1.text = "Tuesday"
                day2.text = "Wednesday"
            }
            Calendar.WEDNESDAY -> {
                day1.text = "Wednesday"
                day2.text = "Thursday"
            }
            Calendar.THURSDAY -> {
                day1.text = "Thursday"
                day2.text = "Friday"
            }
            Calendar.FRIDAY -> {
                day1.text = "Friday"
                day2.text = "Saturday"
            }
            Calendar.SATURDAY -> {
                day1.text = "Saturday"
                day2.text = "Sunday"
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}