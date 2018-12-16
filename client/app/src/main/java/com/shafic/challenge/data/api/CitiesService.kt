package com.shafic.challenge.data.api

import com.shafic.challenge.data.models.City
import io.reactivex.Maybe
import retrofit2.http.GET
import retrofit2.http.Path

interface CitiesService {
    companion object {
        const val ENDPOINT = "cities"
    }

    //Replace Single by Maybe operator  or City by Optional<City>
    @GET("$ENDPOINT")
    fun get(): Maybe<List<City>>

    @GET("$ENDPOINT/{code}")
    fun get(@Path("code") code: String): Maybe<City>
}
