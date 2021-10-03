package com.eamo5.weather.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    /*private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text*/

    val currentLocation: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
}