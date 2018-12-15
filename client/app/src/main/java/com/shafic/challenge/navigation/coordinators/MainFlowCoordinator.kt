package com.shafic.challenge.navigation.coordinators

import com.shafic.challenge.navigation.Navigator

/**
 * ONLY Handles where to go next [Not how]
 * */

class MainFlowCoordinator : MainFlowProvider {
    private val navigator: Navigator = Navigator

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

interface MainFlowProvider {
    fun start()
    fun showPermissionHandler(requestCode: Int)
    fun requestCityPicker(requestCode: Int)
    fun closePermissionHandler(resultCode: Int)
    fun goToAppSettings()
}

