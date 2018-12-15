package com.shafic.challenge.common.base

import android.arch.lifecycle.ViewModel
import com.shafic.challenge.injection.component.DaggerViewModelInjector
import com.shafic.challenge.injection.component.ViewModelInjector
import com.shafic.challenge.injection.module.NetworkModule
import com.shafic.challenge.ui.cityPicker.CityPickerViewModel
import com.shafic.challenge.ui.landing.LandingActivityViewModel
import com.shafic.challenge.ui.map.MainActivityViewModel

abstract class BaseViewModel : ViewModel() {

    private val injector: ViewModelInjector = DaggerViewModelInjector
        .builder()
        .networkModule(NetworkModule)
        .build()

    init {
        inject()
    }

    /**
     * Injects the required dependencies
     */
    private fun inject() {
        when (this) {
            is MainActivityViewModel -> injector.inject(this)
            is LandingActivityViewModel -> injector.inject(this)
            is CityPickerViewModel -> injector.inject(this)
        }
    }
}
