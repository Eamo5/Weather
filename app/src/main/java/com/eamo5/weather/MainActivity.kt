package com.eamo5.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.eamo5.weather.api.LocationData
import com.eamo5.weather.api.WeatherApi
import com.eamo5.weather.api.WeatherData
import com.eamo5.weather.databinding.ActivityMainBinding
import com.eamo5.weather.ui.home.HomeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

const val BASE_URL = "https://www.metaweather.com/api/location/"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Boilerplate
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // API calls
        getLocationData(this)
        getWeatherData(this)

        // Observers
        val locationObserver = Observer<String> { location ->
            findViewById<TextView>(R.id.location).text = location
        }

        val temperatureObserver = Observer<String> { temperature ->
            findViewById<TextView>(R.id.temperature).text = temperature
        }

        val day1Weather = Observer<String> { day1Weather ->
            findViewById<TextView>(R.id.day1Weather).text = day1Weather
        }

        val day2Weather = Observer<String> { day2Weather ->
            findViewById<TextView>(R.id.day2Weather).text = day2Weather
        }

        val day3Weather = Observer<String> { day3Weather ->
            findViewById<TextView>(R.id.day3Weather).text = day3Weather
        }

        val day4Weather = Observer<String> { day4Weather ->
            findViewById<TextView>(R.id.day4Weather).text = day4Weather
        }

        val day5Weather = Observer<String> { day5Weather ->
            findViewById<TextView>(R.id.day5Weather).text = day5Weather
        }

        val day6Weather = Observer<String> { day6Weather ->
            findViewById<TextView>(R.id.day6Weather).text = day6Weather
        }

        homeViewModel.currentLocation.observe(this, locationObserver)
        homeViewModel.currentTemperature.observe(this, temperatureObserver)
        homeViewModel.day1Weather.observe(this, day1Weather)
        homeViewModel.day2Weather.observe(this, day2Weather)
        homeViewModel.day3Weather.observe(this, day3Weather)
        homeViewModel.day4Weather.observe(this, day4Weather)
        homeViewModel.day5Weather.observe(this, day5Weather)
        homeViewModel.day6Weather.observe(this, day6Weather)
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
                        homeViewModel.currentLocation.value = myData.location
                    }
                }
            }

            override fun onFailure(call: Call<List<LocationData>?>, t: Throwable) {
                TODO("Not yet implemented")
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
                    // Update weather values for each day
                    homeViewModel.currentTemperature.value =
                        formatTemp(it.consolidated_weather[0].the_temp)
                    homeViewModel.day1Weather.value =
                        formatTemp(it.consolidated_weather[0].max_temp)
                    homeViewModel.day2Weather.value =
                        formatTemp(it.consolidated_weather[1].max_temp)
                    homeViewModel.day3Weather.value =
                        formatTemp(it.consolidated_weather[2].max_temp)
                    homeViewModel.day4Weather.value =
                        formatTemp(it.consolidated_weather[3].max_temp)
                    homeViewModel.day5Weather.value =
                        formatTemp(it.consolidated_weather[4].max_temp)
                    homeViewModel.day6Weather.value =
                        formatTemp(it.consolidated_weather[5].max_temp)
                }
            }

            override fun onFailure(call: Call<WeatherData?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    // Format temperature for displaying in TextView as Celsius
    private fun formatTemp(temp: Double): String {
        return temp.roundToInt().toString() + "°C"
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
}