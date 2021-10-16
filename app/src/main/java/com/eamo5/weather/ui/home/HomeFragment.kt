package com.eamo5.weather.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.eamo5.weather.R
import com.eamo5.weather.api.ConsolidatedWeather
import com.eamo5.weather.api.LocationData
import com.eamo5.weather.api.WeatherApi
import com.eamo5.weather.api.WeatherData
import com.eamo5.weather.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val BASE_URL = "https://www.metaweather.com/api/location/"

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var currentWeatherIcon: ImageView
    private lateinit var location: TextView
    private lateinit var currentTemp: TextView
    private lateinit var homeTextViews: List<TextView>
    private lateinit var homeImageViews: List<ImageView>
    private var consolidatedWeather = mutableListOf<ConsolidatedWeather>()
    private var states = arrayOfNulls<String>(6)

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

        // Current Weather
        currentWeatherIcon = root.findViewById(R.id.weatherIcon)
        location = root.findViewById(R.id.location)
        currentTemp = root.findViewById(R.id.temperature)

        // Weather TextViews
        homeTextViews = listOf(
            root.findViewById(R.id.day1Weather), root.findViewById(R.id.day2Weather),
            root.findViewById(R.id.day3Weather), root.findViewById(R.id.day4Weather),
            root.findViewById(R.id.day5Weather), root.findViewById(R.id.day6Weather))

        // Weather ImageViews
        homeImageViews = listOf(
            root.findViewById(R.id.day1WeatherIcon), root.findViewById(R.id.day2WeatherIcon),
            root.findViewById(R.id.day3WeatherIcon), root.findViewById(R.id.day4WeatherIcon),
            root.findViewById(R.id.day5WeatherIcon), root.findViewById(R.id.day6WeatherIcon)
        )

        // Cards
        val cards = listOf<CardView>(
            root.findViewById(R.id.card1), root.findViewById(R.id.card2),
            root.findViewById(R.id.card3), root.findViewById(R.id.card4),
            root.findViewById(R.id.card5), root.findViewById(R.id.card6)
        )

        // Days of Week
        val days = listOf<TextView>(
            root.findViewById(R.id.day1), root.findViewById(R.id.day2),
            root.findViewById(R.id.day3), root.findViewById(R.id.day4),
            root.findViewById(R.id.day5), root.findViewById(R.id.day6),
        )

        // API calls
        activity?.let {
            getLocationData(it)
            getWeatherData(it)
        }

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

        // Consolidated Weather List for Bottom Sheet
        if (sharedPref?.getString("consolidatedWeatherList", "") != ""){
            val json = sharedPref?.getString("consolidatedWeatherList", null)
            val type = object : TypeToken<List<ConsolidatedWeather>>() {}.type
            consolidatedWeather = Gson().fromJson(json, type)
        }

        // Today
        location.text = sharedPref?.getString("currentLocation", "-")
        currentTemp.text = sharedPref?.getString("currentTemp", "-")

        // This weeks temperature
        homeTextViews.forEachIndexed { index, textView ->
            textView.text = sharedPref?.getString("day${index}Weather", "-")
        }

        // Corresponding ImageViews
        homeImageViews.forEachIndexed { index, imageView ->
            val string = sharedPref?.getString("state${index}", "-")
            if (string != "-" && string != null) {
                states[index] = string
                setWeatherIcon(imageView, string)
                if (index == 0) {
                    setWeatherIcon(currentWeatherIcon, string)
                }

            }
        }

        // Observers
        homeViewModel.daysList.forEachIndexed { index, mutableLiveData ->
            mutableLiveData.observe(viewLifecycleOwner, { dayWeather ->
                homeTextViews[index].text = dayWeather
            })
        }

        homeViewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            this.location.text = location
        })
        homeViewModel.currentTemperature.observe(viewLifecycleOwner, { temperature ->
            currentTemp.text = temperature
        })

        // Card set on click listeners
        cards.forEachIndexed { index, cardView ->
            cardView.setOnClickListener {
                if (!states.isNullOrEmpty()) {
                    states[index]?.let { it1 ->
                        showBottomSheetDialog(
                            consolidatedWeather[index],
                            location.text.toString(),
                            it1
                        )
                    }
                } else {
                    Toast.makeText(activity, "Please wait...", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

    private fun getLocationData(context: Context) {

        val okHttpClient = retrofitCache(context, 5)

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(WeatherApi::class.java)

        val retrofitGetData = retrofitBuilder.getLocationData()

        retrofitGetData.enqueue(object : Callback<List<LocationData>?> {
            override fun onResponse(
                call: Call<List<LocationData>?>,
                response: Response<List<LocationData>?>
            ) {
                val responseBody = response.body()

                if (responseBody != null) {
                    for (myData in responseBody) {
                        location.text = myData.location
                    }
                }
            }

            override fun onFailure(call: Call<List<LocationData>?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun getWeatherData(context: Context) {

        val okHttpClient = retrofitCache(context, 5)

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
            .create(WeatherApi::class.java)

        val retrofitGetData = retrofitBuilder.getWeatherData()

        retrofitGetData.enqueue(object : Callback<WeatherData?> {
            override fun onResponse(call: Call<WeatherData?>, response: Response<WeatherData?>) {
                val responseBody = response.body()
                responseBody?.let {
                    val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                    // Update weather images / states for each day
                    homeImageViews.forEachIndexed { index, imageView ->
                        // If today, set weather in overview
                        if (index == 0) {
                            setWeatherIcon(currentWeatherIcon,
                                it.consolidated_weather[index].weather_state_abbr)
                            currentTemp.text = formatTemp(it.consolidated_weather[0].the_temp)
                        }

                        setWeatherIcon(imageView, it.consolidated_weather[index].weather_state_abbr)
                        homeTextViews[index].text = formatTemp(it.consolidated_weather[index].max_temp)
                        consolidatedWeather.add(it.consolidated_weather[index])

                        states[index] = (it.consolidated_weather[index].weather_state_abbr)
                        with (sharedPref.edit()) {
                            putString("state${index}", states[index])
                            apply()
                        }
                    }

                    // Save associated weather abbreviation for restoring ImageView
                    with (sharedPref.edit()) {
                        putString("consolidatedWeatherList", Gson().toJson(consolidatedWeather))
                        apply()
                    }
                }
            }

            override fun onFailure(call: Call<WeatherData?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    private fun retrofitCache(context: Context, size: Int): OkHttpClient {
        // 5mb cache
        val cacheSize = (size * 1024 * 1024).toLong()
        val myCache = Cache(context.cacheDir, cacheSize)

        val okHttpClient = OkHttpClient.Builder()
            .cache(myCache)
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork(context))
                    request.newBuilder().header("Cache-Control", "public, max-age=" + 5).build()
                else
                    request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
                chain.proceed(request)
            }
            .build()
        return okHttpClient
    }

    private fun hasNetwork(context: Context): Boolean {
        val isConnected: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val connManager = connectivityManager.activeNetwork?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(connManager)?: return false
        isConnected = when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return isConnected
    }

    fun setWeatherIcon(image: ImageView, state: String) {
        activity?.let {
            when (state) {
                "c" -> image.setImageDrawable(AppCompatResources.getDrawable
                    (it, R.drawable.ic_clear))
                "hc" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_heavy_cloud)))
                "hr" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_heavy_rain)))
                "lc" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_light_cloud)))
                "lr" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_light_rain)))
                "s" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_showers)))
                "sl" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_sleet)))
                "sn" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_snow)))
                "t" -> image.setImageDrawable((AppCompatResources.getDrawable
                    (it, R.drawable.ic_thunderstorm)))
            }
        }
    }

    // Format temperature for displaying in TextView as Celsius
    fun formatTemp(temp: Double): String {
        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        return if (prefManager.getString("temperature", "") != "fahrenheit"){
            temp.roundToInt().toString() + "°C"
        } else {
            convertToFahrenheit(temp.roundToInt()).toString() + "°F"
        }
    }

    // Convert from celsius to fahrenheit
    private fun convertToFahrenheit(temp: Int): Int {
        return (temp * 9/5) + 32
    }

    // Show BottomSheetDialogFragment
    private fun showBottomSheetDialog(data: ConsolidatedWeather, location: String, state: String) {
        val bottomSheet = WeatherBottomSheetDialog(data, location, state)
        bottomSheet.show(childFragmentManager, "medallistBottomSheet")
    }
}