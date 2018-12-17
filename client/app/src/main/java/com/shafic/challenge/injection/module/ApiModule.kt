package com.shafic.challenge.injection.module

import com.google.gson.GsonBuilder
import com.shafic.challenge.BuildConfig
import com.shafic.challenge.data.api.CitiesService
import com.shafic.challenge.data.api.CountriesService
import com.shafic.challenge.injection.component.ApiComponent
import com.shafic.challenge.network.ErrorInterceptor
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

//Class Accessor Even if class is a singleton 
fun api(): ApiComponent = ApiComponent.instance

internal object ApiModule : ApiComponent {
    private val API_BASE_URL by lazy {
        BuildConfig.BASE_URL + BuildConfig.API_PATH_EXTENSION
    }

    val loggingInterceptor = HttpLoggingInterceptor()
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    override val okHttpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor)
        .addInterceptor(ErrorInterceptor())
        .build()

    override val gson = GsonBuilder().create()

    override val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
        .baseUrl(API_BASE_URL)
        .build()


    override val countriesApi = retrofit.create(CountriesService::class.java)
    override val citiesApi = retrofit.create(CitiesService::class.java)

    override val mainThread: Scheduler
        get() = AndroidSchedulers.mainThread()
    override val backgroundThread: Scheduler
        get() = Schedulers.io()
}
