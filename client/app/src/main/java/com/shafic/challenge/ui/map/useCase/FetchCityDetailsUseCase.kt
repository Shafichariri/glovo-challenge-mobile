package com.shafic.challenge.ui.map.useCase

import com.shafic.challenge.data.models.City
import com.shafic.challenge.repositories.CitiesRepository
import io.reactivex.Maybe


interface FetchCityDetailsUseCase {
    fun city(code: String): Maybe<City>
}

class FetchCityDetailsUseCaseImp : FetchCityDetailsUseCase {
    override fun city(code: String): Maybe<City> {
        val citiesRepository: CitiesRepository = CitiesRepository.instance

        return citiesRepository.getCity(code)
    }
}
