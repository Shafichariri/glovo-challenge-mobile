package com.shafic.challenge.injection.component

import com.shafic.challenge.injection.module.NetworkModule
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
     * @param mainActivityViewModel MainActivityViewModel in which to inject the dependencies
     */
    fun inject(mainActivityViewModel: MainActivityViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector

        fun networkModule(networkModule: NetworkModule): Builder
    }
}
