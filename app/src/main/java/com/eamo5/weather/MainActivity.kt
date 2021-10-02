package com.eamo5.weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.eamo5.weather.api.LocationData
import com.eamo5.weather.api.WeatherApi
import com.eamo5.weather.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URL = "https://www.metaweather.com/api/location/"

class MainActivity : AppCompatActivity() {

    var cityID: Int = 0

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        getLocationData()

    }

    private fun getLocationData() {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(WeatherApi::class.java)

        val retrofitGetData = retrofitBuilder.getData()

        retrofitGetData.enqueue(object : Callback<List<LocationData>?> {
            override fun onResponse(
                call: Call<List<LocationData>?>,
                response: Response<List<LocationData>?>
            ) {
                val responseBody = response.body()

                if (responseBody != null) {
                    for (myData in responseBody) {
                        cityID = myData.cityID
                    }
                }
            }

            override fun onFailure(call: Call<List<LocationData>?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }
}