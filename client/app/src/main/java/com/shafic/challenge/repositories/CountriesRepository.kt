package com.shafic.challenge.repositories

import com.shafic.challenge.data.api.CountriesService
import com.shafic.challenge.data.models.Country
import com.shafic.challenge.injection.module.api
import io.reactivex.Maybe

interface CountriesRepository {
    val countriesApi: CountriesService
    fun getCountries(): Maybe<List<Country>>
}

class CountriesRepositoryImp : Repository(), CountriesRepository {
    override val countriesApi: CountriesService
        get() = api().countriesApi

    override fun getCountries(): Maybe<List<Country>> {
        return countriesApi.get()
            .subscribeOn(backgroundThread)
    }
}
