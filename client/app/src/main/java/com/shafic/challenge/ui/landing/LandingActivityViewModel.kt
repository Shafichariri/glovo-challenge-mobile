package com.shafic.challenge.ui.landing

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.shafic.challenge.common.base.BaseViewModel

class LandingActivityViewModel : BaseViewModel() {

    private val permissionMessageLiveData: MutableLiveData<String> = MutableLiveData()
    private val permissionGrantedLiveData: MutableLiveData<Boolean> = MutableLiveData()


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
}
