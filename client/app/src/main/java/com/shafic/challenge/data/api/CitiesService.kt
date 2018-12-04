package com.shafic.challenge.data.api

import com.shafic.challenge.data.models.City
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface CitiesService {
    companion object {
        const val ENDPOINT = "cities"
    }

    @GET("$ENDPOINT")
    fun get(): Single<Array<City>>

    @GET("$ENDPOINT/{code}")
    fun get(@Path("code") code: String): Single<City>
}
