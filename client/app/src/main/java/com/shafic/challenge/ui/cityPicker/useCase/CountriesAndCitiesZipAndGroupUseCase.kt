package com.shafic.challenge.ui.cityPicker.useCase

import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.models.Country
import com.shafic.challenge.repositories.CitiesRepository
import com.shafic.challenge.repositories.CountriesRepository
import com.shafic.challenge.repositories.CountriesRepositoryImp
import com.shafic.challenge.ui.cityPicker.list.CityPickerAdapterItem
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction


interface CountriesAndCitiesZipAndGroupUseCase {
    fun zip(groupingUseCase: CountriesAndCitiesGroupingUseCase): Maybe<List<CityPickerAdapterItem>>
}

class CountriesAndCitiesZipAndGroupUseCaseImp : CountriesAndCitiesZipAndGroupUseCase {
    private val countriesRepository: CountriesRepository = CountriesRepositoryImp()
    private val citiesRepository: CitiesRepository = CitiesRepository.instance

    override fun zip(groupingUseCase: CountriesAndCitiesGroupingUseCase): Maybe<List<CityPickerAdapterItem>> {
        return Maybe.zip<List<Country>, List<City>, List<CityPickerAdapterItem>>(countriesRepository.getCountries(),
            citiesRepository.getCities(),
            BiFunction { countries, cities ->
                return@BiFunction groupingUseCase.group(countries, cities)
            })
    }
}
