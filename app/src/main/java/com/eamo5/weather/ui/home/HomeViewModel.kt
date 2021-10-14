package com.eamo5.weather.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    // MutableLiveData

    // Days
    val daysList = listOf(
        MutableLiveData<String>(), MutableLiveData<String>(), MutableLiveData<String>(),
        MutableLiveData<String>(), MutableLiveData<String>(), MutableLiveData<String>()
    )

    // Current Weather
    val currentLocation = MutableLiveData<String>()
    val currentTemperature = MutableLiveData<String>()
}