package com.eamo5.weather.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {

    @GET("search")
    fun getLocationData(@Query("query") location: String): Call<List<LocationData>>

    @GET("{woeid}")
    fun getWeatherData(@Path("woeid") woeid: Int): Call<WeatherData>

    /*@GET("1103816")
    fun getConsolidatedWeather(): Call<ConsolidatedWeather>*/
}