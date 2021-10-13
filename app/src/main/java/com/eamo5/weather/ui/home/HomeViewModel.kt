package com.eamo5.weather.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    val currentLocation: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val currentTemperature: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val day1Weather: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val day2Weather: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val day3Weather: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val day4Weather: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val day5Weather: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val day6Weather: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}