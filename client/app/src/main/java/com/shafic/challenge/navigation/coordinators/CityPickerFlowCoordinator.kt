package com.shafic.challenge.navigation.coordinators

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.shafic.challenge.navigation.NavigationProvider
import com.shafic.challenge.navigation.Navigator
import java.lang.ref.WeakReference


interface CityPickerFlowProvider {
    fun closeCityPicker(resultCode: Int, intent: Intent)
}

class CityPickerFlowCoordinator(activity: AppCompatActivity) : CityPickerFlowProvider {
    private val navigator: NavigationProvider = Navigator(WeakReference(activity))

    override fun closeCityPicker(resultCode: Int, intent: Intent) {
        navigator.closeCityPicker(resultCode, intent)
    }
}
