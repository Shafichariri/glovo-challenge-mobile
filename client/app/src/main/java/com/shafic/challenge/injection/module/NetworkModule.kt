package com.shafic.challenge.injection.module

import com.google.gson.GsonBuilder
import com.shafic.challenge.data.api.CitiesService
import com.shafic.newassignment.network.NetworkConstants
import dagger.Module
import dagger.Provides
import dagger.Reusable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Module which provides all required dependencies about network
 */
@Module
// Safe here as we are dealing with a Dagger 2 module
@Suppress("unused")
object NetworkModule {
    /**
     * Provides the Cities service implementation.
     * @param retrofit the Retrofit object used to instantiate the service
     * @return the Cities service implementation.
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideCitiesApi(retrofit: Retrofit): CitiesService {
        return retrofit.create(CitiesService::class.java)
    }

    /**
     * Provides the Retrofit object.
     * @return the Retrofit object
     */
    @Provides
    @Reusable
    @JvmStatic
    internal fun provideRetrofitInterface(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .baseUrl(NetworkConstants.API_BASE_URL)
            .build()
    }
}
