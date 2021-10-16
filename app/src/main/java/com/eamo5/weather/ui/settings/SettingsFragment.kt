package com.eamo5.weather.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.eamo5.weather.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }


}