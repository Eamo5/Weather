package com.eamo5.weather

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.eamo5.weather.api.LocationData
import com.eamo5.weather.api.WeatherApi
import com.eamo5.weather.api.WeatherData
import com.eamo5.weather.databinding.ActivityMainBinding
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
    private lateinit var homeImageViews: List<ImageView>
    private lateinit var homeTextViews: List<TextView>
    private lateinit var currentLocation: TextView
    private lateinit var currentTemperature: TextView
    private lateinit var currentWeatherIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TextViews
        currentLocation = findViewById(R.id.location)
        currentTemperature = findViewById(R.id.temperature)

        homeTextViews = listOf<TextView>(
            findViewById(R.id.day1Weather),
            findViewById(R.id.day2Weather),
            findViewById(R.id.day3Weather),
            findViewById(R.id.day4Weather),
            findViewById(R.id.day5Weather),
            findViewById(R.id.day6Weather),
        )

        // ImageViews
        currentWeatherIcon = findViewById(R.id.weatherIcon)

        homeImageViews = listOf<ImageView>(
            findViewById(R.id.day1WeatherIcon),
            findViewById(R.id.day2WeatherIcon),
            findViewById(R.id.day3WeatherIcon),
            findViewById(R.id.day4WeatherIcon),
            findViewById(R.id.day5WeatherIcon),
            findViewById(R.id.day6WeatherIcon),
        )

        // API calls
        getLocationData(this)
        getWeatherData(this)

        // Navbar
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_settings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
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
                        currentLocation.text = myData.location
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
                    // States
                    val states = mutableListOf<String>()

                    // Update weather images / states for each day
                    homeImageViews.forEachIndexed { index, imageView ->
                        if (index == 0) {
                            setWeatherIcon(currentWeatherIcon,
                                it.consolidated_weather[index].weather_state_abbr)
                            currentTemperature.text = formatTemp(it.consolidated_weather[0].the_temp)
                        }
                        setWeatherIcon(imageView, it.consolidated_weather[index].weather_state_abbr)
                        states.add(it.consolidated_weather[index].weather_state_abbr)
                        homeTextViews[index].text = formatTemp(it.consolidated_weather[index].max_temp)
                    }

                    // Save associated weather abbreviation for restoring ImageView
                    val sharedPref = this@MainActivity.getPreferences(Context.MODE_PRIVATE) ?: return
                    with (sharedPref.edit()) {
                        states.forEachIndexed { index, s ->
                            putString("state${index}", s)
                        }
                        apply()
                    }
                }
            }

            override fun onFailure(call: Call<WeatherData?>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }

    // Format temperature for displaying in TextView as Celsius
    private fun formatTemp(temp: Double): String {
        return temp.roundToInt().toString() + "°C"
    }

    fun setWeatherIcon(image: ImageView, state: String) {
        when (state) {
            "c" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_clear)))
            "hc" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_heavy_cloud)))
            "hr" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_heavy_rain)))
            "lc" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_light_cloud)))
            "lr" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_light_rain)))
            "s" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_showers)))
            "sl" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_sleet)))
            "sn" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_snow)))
            "t" -> image.setImageDrawable((AppCompatResources.getDrawable
                (this@MainActivity, R.drawable.ic_thunderstorm)))
        }
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