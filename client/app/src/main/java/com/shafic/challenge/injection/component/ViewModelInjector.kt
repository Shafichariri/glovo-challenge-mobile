package com.shafic.challenge.injection.component

import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.injection.module.NetworkModule
import com.shafic.challenge.ui.cityPicker.CityPickerViewModel
import com.shafic.challenge.ui.map.MainActivityViewModel
import dagger.Component
import javax.inject.Singleton

/**
 * Component providing inject() methods for presenters.
 * Creates Dagger[ViewModelInjector] prepended to interface component name
 */
@Singleton
@Component(modules = [(NetworkModule::class)])
interface ViewModelInjector {
    /**
     * Injects required dependencies into the specified MainActivityViewModel.
     * @param viewModel MainActivityViewModel in which to inject the dependencies
     */
    fun inject(viewModel: MainActivityViewModel)

    /**
     * Injects required dependencies into the specified CityPickerViewModel.
     * @param viewModel CityPickerViewModel in which to inject the dependencies
     */
    fun inject(viewModel: CityPickerViewModel)

    //TODO: Add third vm injector
    fun inject(viewModel: BaseViewModel)


    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector

        fun networkModule(networkModule: NetworkModule): Builder
    }
}
