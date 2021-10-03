package com.eamo5.weather.api

import retrofit2.Call
import retrofit2.http.GET

interface WeatherApi {

    @GET("search/?query=melbourne")
    fun getData(): Call<List<LocationData>>

    @GET("1103816")
    fun getWeatherData(): Call<List<WeatherData>>
}