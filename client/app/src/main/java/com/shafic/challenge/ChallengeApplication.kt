package com.shafic.challenge

import android.app.Application
import com.shafic.challenge.injection.component.ApiComponent
import com.shafic.challenge.injection.component.AppComponent
import com.shafic.challenge.injection.module.ApiModule
import com.shafic.challenge.injection.module.AppModule
import com.shafic.challenge.repositories.CitiesRepository
import com.shafic.challenge.repositories.CitiesRepositoryImp

class ChallengeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize lateinits
        AppComponent.instance = AppModule(context = this)
        ApiComponent.instance = ApiModule
        CitiesRepository.instance = CitiesRepositoryImp
    }
}
