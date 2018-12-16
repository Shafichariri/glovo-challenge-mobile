package com.shafic.challenge.ui.map.useCase

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.data.presentation.ServiceableLocation
import com.shafic.challenge.data.presentation.SimpleCity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers


interface LocationDetailsUseCase {
    fun fetch(
        latlng: LatLng, provider: RxGeoCoder.LocationInformationInterface, areasReadyObservable: Observable<Boolean>,
        decodedCities: List<SimpleCity>
    ): Observable<ServiceableLocation>
}

class LocationDetailsUseCaseImp(private val convertToCityUseCase: ConvertGeoLocationToCityUseCase) :
    LocationDetailsUseCase {
    override fun fetch(
        latlng: LatLng,
        provider: RxGeoCoder.LocationInformationInterface,
        areasReadyObservable: Observable<Boolean>,
        decodedCities: List<SimpleCity>
    ): Observable<ServiceableLocation> {
        val locationDetailsObserver = RxGeoCoder.locationDetails(latLng = latlng, provider = provider).toObservable()

        val latestFromObservable =
            locationDetailsObserver.withLatestFrom(areasReadyObservable, combiner = { result, areasLoaded ->
                Log.e("SHAFIC", "withLatestFrom $areasLoaded ${result.locationInformation?.city}")
                return@withLatestFrom Pair(result, areasLoaded)
            })

        return latestFromObservable
            .subscribeOn(Schedulers.io())
            .filter {
                Log.e("SHAFIC", "withLatestFrom ${it.second}")
                return@filter it.second
            }
            .flatMap { pair ->
                return@flatMap convertToCityUseCase.map(pair.first, decodedCities)
            }
            .observeOn(AndroidSchedulers.mainThread())
    }
}
