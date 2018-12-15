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

}

interface MainFlowProvider {
    fun start()
    fun showPermissionHandler(requestCode: Int)
    fun requestCityPicker(requestCode: Int)
}

