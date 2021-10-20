package com.eamo5.weather.ui.home

import android.content.Context
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

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var currentWeatherIcon: ImageView
    private lateinit var location: TextView
    private lateinit var currentTemp: TextView
    private lateinit var homeTextViews: List<TextView>
    private lateinit var homeImageViews: List<ImageView>
    private var consolidatedWeather = arrayOfNulls<ConsolidatedWeather>(6)
    private val baseUrl = "https://www.metaweather.com/api/location/"

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

        // Current weather
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
        activity?.let { getLocationData(it) }

        // Set days of week
        days.forEachIndexed { index, textView ->
            val date = SimpleDateFormat("EEEE", Locale.ENGLISH)
            val calendar = GregorianCalendar()
            calendar.add(Calendar.DATE, index)
            val day = date.format(calendar.time)
            textView.text = day
        }

        // Restore shared preferences
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)

        // Retrieve consolidated weather json
        if (sharedPref?.getString("consolidatedWeatherList", "") != "") {
            val json = sharedPref?.getString("consolidatedWeatherList", null)
            val type = object : TypeToken<Array<ConsolidatedWeather>>() {}.type
            consolidatedWeather = Gson().fromJson(json, type)
        }

        // Today
        location.text = sharedPref?.getString("currentLocation", "-")
        currentTemp.text = consolidatedWeather[0]?.let { formatTemp(it.max_temp) }

        // Set values for each day
        // These can be separated into different loops but they have the same index
        homeImageViews.forEachIndexed { index, imageView ->
            // Set weather icons
            consolidatedWeather[index]?.let {
                setWeatherIcon(imageView, it.weather_state_abbr)
                if (index == 0) {
                    setWeatherIcon(currentWeatherIcon, it.weather_state_abbr)
                }
                // Set temperature values
                homeTextViews[index].text = formatTemp(it.max_temp)

                // Add observers for days of week TextViews
                homeViewModel.daysList[index].observe(viewLifecycleOwner, { dayWeather ->
                    homeTextViews[index].text = dayWeather
                })
            }
        }

        // Current location observers
        homeViewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            this.location.text = location
        })
        homeViewModel.currentTemperature.observe(viewLifecycleOwner, { temperature ->
            currentTemp.text = temperature
        })

        // Card set on click listeners
        cards.forEachIndexed { index, cardView ->
            cardView.setOnClickListener {
                consolidatedWeather[index]?.let { consolidatedWeather ->
                    val bottomSheet = WeatherBottomSheetDialog(consolidatedWeather,
                        location.text.toString())
                    bottomSheet.show(childFragmentManager, "weatherBottomSheet")
                }
            }
        }
        return root
    }

    override fun onPause() {
        super.onPause()

        // Saved shared preferences
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            with (it.edit()) {
                // Save current location and temperature
                putString("currentLocation", location.text.toString())

                // Save JSON to SharedPreferences
                putString("consolidatedWeatherList", Gson().toJson(consolidatedWeather))
                apply()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Get id for location
    private fun getLocationData(context: Context) {

        val okHttpClient = retrofitCache(context)

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
            .create(WeatherApi::class.java)

        val prefManager = PreferenceManager.getDefaultSharedPreferences(context)
        val locationSetting = prefManager.getString("location", "Melbourne").toString()

        val retrofitGetData = retrofitBuilder.getLocationData(locationSetting)

        retrofitGetData.enqueue(object : Callback<List<LocationData>?> {
            override fun onResponse(
                call: Call<List<LocationData>?>,
                response: Response<List<LocationData>?>
            ) {
                val responseBody = response.body()
                var woeid = 0

                responseBody?.let {
                    for (myData in responseBody) {
                        location.text = myData.location
                        woeid = myData.cityID
                        getWeatherData(context, woeid)
                    }
                }

                if (woeid == 0 ||
                    locationSetting.lowercase() != location.text.toString().lowercase())
                    Toast.makeText(context, "Invalid location", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<List<LocationData>?>, t: Throwable) {
                Toast.makeText(context, "Unable to reach API", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Get weather data for location
    private fun getWeatherData(context: Context, woeid: Int) {

        val okHttpClient = retrofitCache(context)

        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
            .create(WeatherApi::class.java)

        val retrofitGetData = retrofitBuilder.getWeatherData(woeid)

        retrofitGetData.enqueue(object : Callback<WeatherData?> {
            override fun onResponse(call: Call<WeatherData?>, response: Response<WeatherData?>) {
                val responseBody = response.body()
                responseBody?.let {
                    // Prevent NPE on Context.getPackageName()
                    activity ?: return

                    // Update weather images / states for each day
                    homeImageViews.forEachIndexed { index, imageView ->

                        // If today, set weather in overview
                        if (index == 0) {
                            setWeatherIcon(currentWeatherIcon,
                                it.consolidated_weather[index].weather_state_abbr)
                            currentTemp.text = formatTemp(it.consolidated_weather[index].max_temp)
                        }

                        // Set weather icons and temperature
                        setWeatherIcon(imageView, it.consolidated_weather[index].weather_state_abbr)
                        homeTextViews[index].text =
                            formatTemp(it.consolidated_weather[index].max_temp)

                        // Add consolidated weather for BottomSheetDialog
                        consolidatedWeather[index] = it.consolidated_weather[index]
                    }
                }
            }

            override fun onFailure(call: Call<WeatherData?>, t: Throwable) {
                Toast.makeText(context, "Unable to reach API", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Create cache for data retrieved
    private fun retrofitCache(context: Context): OkHttpClient {
        // 1mb cache
        val cacheSize = (1 * 1024 * 1024).toLong()
        val myCache = Cache(context.cacheDir, cacheSize)

        val okHttpClient = OkHttpClient.Builder()
            .cache(myCache)
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork(context))
                    /* If internet is available, get the cached stored in the last 60 seconds.
                    Otherwise discard and update it again */
                    request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build()
                else
                /* If internet is not available, get the cached stored in the last 24 hours.
                Otherwise error */
                    request.newBuilder().header("Cache-Control",
                        "public, only-if-cached, max-stale=" + 60 * 60 * 24).build()
                chain.proceed(request)
            }
            .build()
        return okHttpClient
    }

    // Check for network connectivity
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
                "c" -> image.setImageDrawable(AppCompatResources.getDrawable(
                    it, R.drawable.ic_clear))
                "hc" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_heavy_cloud)))
                "hr" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_heavy_rain)))
                "lc" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_light_cloud)))
                "lr" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_light_rain)))
                "s" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_showers)))
                "sl" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_sleet)))
                "sn" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_snow)))
                "t" -> image.setImageDrawable((AppCompatResources.getDrawable(
                    it, R.drawable.ic_thunderstorm)))
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
}