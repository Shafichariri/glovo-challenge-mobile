package com.shafic.challenge.injection

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.shafic.challenge.ui.cityPicker.CityPickerViewModel
import com.shafic.challenge.ui.map.MainActivityViewModel
import com.shafic.challenge.ui.permission.PermissionsViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    //We add to the construct any property needed for the VM initialization [If Any, ex: Context for Getting DB Instance]
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel() as T
        } else if (modelClass.isAssignableFrom(CityPickerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CityPickerViewModel() as T
        } else if (modelClass.isAssignableFrom(PermissionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PermissionsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
