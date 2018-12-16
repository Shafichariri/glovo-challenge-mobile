package com.shafic.challenge.repositories

import com.shafic.challenge.data.api.CitiesService
import com.shafic.challenge.data.models.City
import com.shafic.challenge.injection.module.api
import io.reactivex.Maybe


interface CitiesRepository {
    val citiesApi: CitiesService
    var cities: MutableList<City>
    fun getCities(): Maybe<List<City>>
    fun getCity(code: String): Maybe<City>

    companion object {
        lateinit var instance: CitiesRepository
    }
}

object CitiesRepositoryImp : Repository(), CitiesRepository {
    override val citiesApi: CitiesService
        get() = api().citiesApi

    override var cities: MutableList<City> = mutableListOf()

    override fun getCities(): Maybe<List<City>> {
        if (cities.isNotEmpty()) {
            //This can be replace be a call to the persistence layer if available
            return Maybe.just(cities)
        }

        return citiesApi.get()
            .subscribeOn(CitiesRepositoryImp.backgroundThread)
            .doOnSuccess { cities = it.toMutableList() }
    }

    override fun getCity(code: String): Maybe<City> {
        return citiesApi.get(code = code)
    }
}
