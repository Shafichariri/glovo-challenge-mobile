package com.shafic.challenge.ui.landing

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.navigation.Navigator
import com.shafic.challenge.navigation.coordinators.MainFlowProvider
import com.shafic.challenge.ui.cityPicker.CityPickerActivity

class LandingActivityViewModel : BaseViewModel() {
    private var flow: MainFlowProvider? = null
    private val permissionMessageLiveData: MutableLiveData<String> = MutableLiveData()
    private val permissionGrantedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun setFlowCoordinator(flow: MainFlowProvider) {
        this.flow = flow
    }

    fun getPermissionMessage(): LiveData<String> {
        return permissionMessageLiveData
    }

    fun isPermissionGranted(): LiveData<Boolean> {
        return permissionGrantedLiveData
    }

    fun setPermissionMessage(message: String) {
        permissionMessageLiveData.value = message
    }

    fun setIsPermissionGranted(granted: Boolean) {
        permissionGrantedLiveData.value = granted
    }

    fun goToAppSettings() {
        Navigator.goToAppSettings()
    }

    fun goToMapActivity() {
        Navigator.showMap()
    }

    fun goToCityPicker() {
        Navigator.showCityPicker(CityPickerActivity.SELECTION_REQUEST_CODE)
    }
}
