package com.shafic.challenge.navigation.coordinators

import android.support.v7.app.AppCompatActivity
import com.shafic.challenge.navigation.NavigationProvider
import com.shafic.challenge.navigation.Navigator
import java.lang.ref.WeakReference

/**
 * ONLY Handles where to go next using a navigator [Not how]
 * */

interface MainFlowProvider {
    fun start()
    fun showPermissionHandler(requestCode: Int)
    fun requestCityPicker(requestCode: Int)
    fun closePermissionHandler(resultCode: Int)
    fun goToAppSettings()
}

class MainFlowCoordinator(activity: AppCompatActivity) : MainFlowProvider {
    private val navigator: NavigationProvider = Navigator(WeakReference(activity))

    override fun start() {
        navigator.showMap()
    }

    override fun showPermissionHandler(requestCode: Int) {
        navigator.showPermissionHandler(requestCode)
    }

    override fun requestCityPicker(requestCode: Int) {
        navigator.showCityPicker(requestCode)
    }

    override fun closePermissionHandler(resultCode: Int) {
        navigator.finishPermissionHandler(resultCode)
    }

    override fun goToAppSettings() {
        navigator.goToAppSettings()
    }
}

