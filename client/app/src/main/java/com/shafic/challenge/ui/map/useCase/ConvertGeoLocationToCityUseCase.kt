package com.shafic.challenge.ui.map.useCase

import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.data.presentation.ServiceableLocation
import com.shafic.challenge.data.presentation.SimpleCity
import com.shafic.challenge.helpers.CityHelper
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers


interface ConvertGeoLocationToCityUseCase {
    fun map(result: RxGeoCoder.Result, cities: List<SimpleCity>): Observable<ServiceableLocation>
}

class ConvertGeoLocationToCityUseCaseImp(private val fetchCityUseCase: FetchCityDetailsUseCase = FetchCityDetailsUseCaseImp()) :
    ConvertGeoLocationToCityUseCase {
    override fun map(result: RxGeoCoder.Result, cities: List<SimpleCity>): Observable<ServiceableLocation> {

        val info = result.locationInformation ?: return Observable.just(ServiceableLocation.Error(result = result))

        val countryCode = info.country.code
        val cities = CityHelper.filterBy(latLng = info.latLng, andCountryCode = countryCode, list = cities)

        val serviceableLocation = ServiceableLocation.NotServiceable(cities?.firstOrNull(), result = result)
        val cityCode = serviceableLocation.cityCode ?: return Observable.just(serviceableLocation)
        val thoroughfare = serviceableLocation.result?.locationInformation?.thoroughfare

        return fetchCityUseCase.city(code = cityCode)
            .toObservable()
            .subscribeOn(Schedulers.io())
            .map {
                val city = it
                return@map ServiceableLocation.Serviceable(
                    city = city,
                    thoroughfare = thoroughfare
                )
            }
    }
}

