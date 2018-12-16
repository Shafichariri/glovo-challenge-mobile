package com.shafic.challenge.ui.map.useCase

import com.shafic.challenge.data.models.City
import com.shafic.challenge.injection.module.app
import io.reactivex.Maybe

interface SelectedLocationUseCase {
    fun getCityDetails(cityCode: String, countryCode: String): Maybe<City>
    fun filterCities(cities: List<City>, cityCode: String, countryCode: String)
}

class SelectedLocationUseCaseImp(private val citiesUseCase: FetchCitiesUseCase) : SelectedLocationUseCase {
    override fun getCityDetails(cityCode: String, countryCode: String): Maybe<City> {
        val backgroundThread = app().backgroundThread
        return citiesUseCase.getCities()
            .subscribeOn(backgroundThread)
            .map { cities ->
            cities.first { city ->
                city.code == cityCode && city.countryCode == countryCode
            }
        }
    }

    override fun filterCities(cities: List<City>, cityCode: String, countryCode: String) {
        cities.first { city ->
            city.code == cityCode && city.countryCode == countryCode
        }
    }
}
