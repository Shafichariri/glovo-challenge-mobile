package com.shafic.challenge.data.api

import com.shafic.challenge.data.models.Country
import io.reactivex.Maybe
import retrofit2.http.GET

interface CountriesService {
    companion object {
        const val ENDPOINT = "countries"
    }

    @GET("$ENDPOINT")
    fun get(): Maybe<List<Country>>
}   
