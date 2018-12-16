package com.shafic.challenge.ui.cityPicker.useCase

import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.models.Country
import com.shafic.challenge.ui.cityPicker.list.CityPickerAdapterItem


interface CountriesAndCitiesGroupingUseCase {
    fun group(countries: List<Country>?, cities: List<City>?): List<CityPickerAdapterItem>
}

class CountriesAndCitiesGroupingUseCaseImp : CountriesAndCitiesGroupingUseCase {
    override fun group(countries: List<Country>?, cities: List<City>?): List<CityPickerAdapterItem> {
        val citiesGroupedByCountry = cities?.groupBy { it.countryCode }?.toSortedMap() ?: return emptyList()
        return citiesGroupedByCountry
            .map { entry ->
                var items = mutableListOf<CityPickerAdapterItem>()
                val country = countries?.find { it.code == entry.key } ?: return@map items
                val countryItem = CityPickerAdapterItem.CountryItem(country = country)
                items.add(countryItem)

                val cities = entry.value
                val cityItems = cities.map { CityPickerAdapterItem.CityItem(it) }.sortedBy { it.city.name }
                items.addAll(cityItems)
                return@map items
            }.flatten()
    }
}
