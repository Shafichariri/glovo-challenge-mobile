package com.shafic.challenge.ui.map.useCase

import com.shafic.challenge.data.models.City
import com.shafic.challenge.repositories.CitiesRepository
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


interface FetchCitiesUseCase {
    fun getCities(): Maybe<List<City>>
}

class FetchCitiesUseCaseImp : FetchCitiesUseCase {
    override fun getCities(): Maybe<List<City>> {
        val repository: CitiesRepository = CitiesRepository.instance

        return repository.getCities()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
