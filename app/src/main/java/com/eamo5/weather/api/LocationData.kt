package com.eamo5.weather.api

import com.google.gson.annotations.SerializedName

data class LocationData(
    @SerializedName("title") val location: String,
    @SerializedName("location_type") val locationType: String,
    @SerializedName("woeid") val cityID: Int,
    @SerializedName("latt_long") val coordinates: String
)