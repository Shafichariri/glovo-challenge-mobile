package com.shafic.challenge.ui.map

import android.arch.lifecycle.MutableLiveData
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.shafic.challenge.common.Optional
import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.common.util.MapUtil.Companion.isPolygonWithinBounds
import com.shafic.challenge.data.api.CitiesService
import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.presentation.MapDataPresentation
import com.shafic.challenge.data.presentation.RegionInfo
import com.shafic.challenge.data.presentation.SimpleCity
import com.shafic.challenge.data.presentation.ZoomContext
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


class MainActivityViewModel : BaseViewModel() {

    @Inject
    lateinit var citiesApi: CitiesService

    private lateinit var subscription: Disposable
    private lateinit var readinessSubscription: Disposable
    private lateinit var regionSubscription: Disposable
    private var locationDetailsSubscription: Disposable? = null

    val readyLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val mapData: MutableLiveData<MapDataPresentation> = MutableLiveData()
    //val mapData: MutableLiveData<MapDataPresentation> = MutableLiveData()

    private val areasReadyObservable: BehaviorSubject<Boolean> = BehaviorSubject.create()
    private val mapReadyObservable: PublishSubject<Boolean> = PublishSubject.create()
    private val regionSubject: PublishSubject<RegionInfo> = PublishSubject.create()

    private var decodedCities: List<SimpleCity> = arrayListOf()
    private var cities: MutableList<City> = arrayListOf()

    init {
        setupReadinessObservable()
        setupVisibleRegionChangedObservable()
        loadCities()
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
        readinessSubscription.dispose()
        locationDetailsSubscription?.dispose()
    }

    fun requestLocationDetails(latlng: LatLng, provider: RxGeoCoder.LocationInformationInterface) {
        val locationDetailsObserver = RxGeoCoder.locationDetails(latLng = latlng, provider = provider)

        Observable
            .zip<RxGeoCoder.Result, Boolean, Pair<RxGeoCoder.Result, Boolean>>(locationDetailsObserver.toObservable(),
                areasReadyObservable, BiFunction { result, areasLoaded ->
                    return@BiFunction Pair(result, areasLoaded)
                })
            .subscribeOn(Schedulers.io())
            .filter { it.second }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnEach { }


//        locationDetailsObserver.zipWith(areasReadyObservable, {
//            
//        })
//        locationDetailsSubscription = RxGeoCoder.locationDetails(latLng = latlng, provider = provider)
//            .subscribe({ result ->
//                val error = result.error
//                val locationInfo = result.locationInformation
//
//                if (locationInfo != null) {
//                    Log.e("SHAFIC", locationInfo.toString())
//                } else if (error != null) {
//                    //TODO: Handle error
//                    Log.e("SHAFIC", error.description)
//                } else {
//                    throw Exception("Should never be here")
//                }
//            }, {
//                it.printStackTrace()
//            })
    }

    private fun setupReadinessObservable() {
        // If you introduce RxKotlin then you can use type inference
        // Observables.combineLatest(name, age) { n, a -> "$n - age:${a}" }.subscribe()
        readinessSubscription = Observable
            .combineLatest<Boolean, Boolean, Boolean>(
                areasReadyObservable,
                mapReadyObservable,
                BiFunction { areasReady, mapReady ->
                    log("readinessSubscription $areasReady $mapReady")
                    return@BiFunction areasReady && mapReady
                })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { ready -> readyLiveData.value = ready }
            .subscribe()
    }

    private fun setupVisibleRegionChangedObservable() {
        regionSubscription = regionSubject
            .subscribeOn(Schedulers.io())
            .map { regionInfo ->
                //Expected Heavy Operation
                return@map updateMapRepresentedData(regionInfo)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                log("subscribe || setupVisibleRegionChangedObservable")
                handleMapRepresentedData(it)
            }, {
                log("Error || setupVisibleRegionChangedObservable")
                print(it)
            })
    }

    @UiThread
    private fun handleMapRepresentedData(optional: Optional<MapDataPresentation>) {
        val data = optional.value ?: return
        mapData.value = data
    }

    @WorkerThread
    private fun updateMapRepresentedData(info: RegionInfo): Optional<MapDataPresentation> {
        // TODO: Keep the last filtered items and detect if the new region is a sub-region of the previous one 
        // TODO: If that was the case then do the filtered on the already filtered items

        val zoomContext = ZoomContext.create(info.zoom)

        when (zoomContext) {
            ZoomContext.MarkersClusterFriendly -> {
                //Represent each cities once on this zoom level 
                val citiesLngLat = decodedCities.distinctBy { it.code }
                    .filterNot { it.latLng == null }

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = citiesLngLat)
                )
            }
            ZoomContext.MarkersFriendly -> {
                log("ZoomContext: MarkersFriendly ")

                val visibleBounds = info.visibleRegionPolygon ?: return Optional.empty()
                var boundedMarkers: MutableList<SimpleCity> = mutableListOf()

                val simpleCities = decodedCities.toList()
                simpleCities.forEach { city ->
                    val anyCityMarker = city.latLng// ?: return@forEach
                    if (anyCityMarker != null &&
                        MapUtil.isPointWithinBounds(visibleBounds, anyCityMarker, true)
                    ) {
                        boundedMarkers.add(city)
                    }
                }

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = boundedMarkers)
                )
            }

            ZoomContext.PolygonsClusterFriendly, ZoomContext.PolygonsFriendly -> {
                log("ZoomContext: PolygonsClusterFriendly || PolygonsFriendly ")

                val visibleBounds = info.visibleRegionPolygon ?: return Optional.empty()
                val simpleCities = decodedCities.toList()

                val cityAreasWithinBounds = simpleCities.mapNotNull { city ->
                    var areas = arrayListOf<List<LatLng>>()
                    city.workingArea?.forEach { area ->
                        if (isPolygonWithinBounds(visibleBounds, area)) {
                            areas.add(area)
                        }
                    }
                    if (areas.isEmpty()) {
                        return@mapNotNull null
                    }
                    return@mapNotNull SimpleCity(city.code, latLng = city.latLng, workingArea = areas)
                }

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = cityAreasWithinBounds)
                )
            }

            ZoomContext.None -> {
                log("ZoomContext: None ")
                return Optional.empty()
            }
        }
    }

    fun setMapStateReady(ready: Boolean) {
        mapReadyObservable.onNext(ready)
        mapReadyObservable.onComplete()
    }

    private fun loadCities() {
        subscription = citiesApi.get()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSuccess { cities ->
                this.cities = cities.toMutableList()

                decodedCities = cities.map {
                    val workingArea = it.workingArea.map { encodedPolygon -> PolyUtil.decode(encodedPolygon) }
                    return@map SimpleCity(
                        it.code,
                        latLng = workingArea.firstOrNull()?.firstOrNull(),
                        workingArea = workingArea
                    )
                }
                log("loadCities || success")
            }
            .subscribe(
                { onRetrieveCitiesSuccess() },
                { onRetrieveCitiesError(it) }
            )
    }

    private fun onRetrieveCitiesSuccess() {
        areasReadyObservable.onNext(true)
        areasReadyObservable.onComplete()
    }

    private fun onRetrieveCitiesError(throwable: Throwable) {
        areasReadyObservable.onNext(false)
        throwable.printStackTrace()
    }

    fun log(message: String = "", info: Boolean = false) {
        if (!message.isBlank()) {
            Log.d("SHAFIC", "LOGGER: $message")
        }
        if (info) {
            Log.d("SHAFIC", "Cities: ${cities.count()}")
        }
    }

    fun requestMapDisplayInfo(zoom: Float, center: LatLng, visibleRegionPolygon: List<LatLng>?) {
        val regionInfo = RegionInfo(zoom, center, visibleRegionPolygon)
        log("(requestMapDisplayInfo) regionInfo: $regionInfo", info = true)

        regionSubject.onNext(regionInfo)
    }
}




