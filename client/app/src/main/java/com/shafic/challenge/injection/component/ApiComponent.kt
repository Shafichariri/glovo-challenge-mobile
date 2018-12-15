package com.shafic.challenge.injection.component

import com.google.gson.Gson
import com.shafic.challenge.data.api.CitiesService
import com.shafic.challenge.data.api.CountriesService
import io.reactivex.Scheduler
import okhttp3.OkHttpClient
import retrofit2.Retrofit

interface ApiComponent {
    val retrofit: Retrofit
    val okHttpClient: OkHttpClient
    val gson: Gson
    val citiesApi: CitiesService
    val countriesApi: CountriesService
    val mainThread: Scheduler
    val backgroundThread: Scheduler

    companion object {
        lateinit var instance: ApiComponent
    }
}
