package com.shafic.challenge.injection.module

///**
// * Module which provides all required dependencies about network
// */
//@Module
//// Safe here as we are dealing with a Dagger 2 module
//@Suppress("unused")
//object NetworkModule {
//    /**
//     * Provides the Cities service implementation.
//     * @param retrofit the Retrofit object used to instantiate the service
//     * @return the Cities service implementation.
//     */
//    @Provides
//    @Reusable
//    @JvmStatic
//    internal fun provideCitiesApi(retrofit: Retrofit): CitiesService {
//        return retrofit.create(CitiesService::class.java)
//    }
//
//    /**
//     * Provides the Countries service implementation.
//     * @param retrofit the Retrofit object used to instantiate the service
//     * @return the Countries service implementation.
//     */
//    @Provides
//    @Reusable
//    @JvmStatic
//    internal fun provideCountriesApi(retrofit: Retrofit): CountriesService {
//        return retrofit.create(CountriesService::class.java)
//    }
//
//    /**
//     * Provides the Retrofit object.
//     * @return the Retrofit object
//     */
//    @Provides
//    @Reusable
//    @JvmStatic
//    internal fun provideRetrofitInterface(): Retrofit {
//        return Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
//            .baseUrl(NetworkConstants.API_BASE_URL)
//            .build()
//    }
//}
