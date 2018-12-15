package com.shafic.challenge.ui.map

import android.arch.lifecycle.MutableLiveData
import android.support.annotation.UiThread
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.shafic.challenge.common.Optional
import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.data.api.CitiesService
import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.presentation.*
import com.shafic.challenge.injection.module.api
import com.shafic.challenge.managers.CityManager
import com.shafic.challenge.navigation.coordinators.MainFlowProvider
import com.shafic.challenge.ui.permission.PermissionsActivity.Companion.PERMISSIONS_REQUEST_CODE
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit


class MainActivityViewModel : BaseViewModel() {
    data class LocationChangeData(val latLng: LatLng, val provider: RxGeoCoder.LocationInformationInterface)

//    @Inject
    var citiesApi: CitiesService = api().citiesApi

    private lateinit var subscription: Disposable
    private lateinit var readinessSubscription: Disposable
    private lateinit var regionSubscription: Disposable
    private var locationDetailsSubscription: Disposable? = null
    private var locationChangedSubscription: Disposable? = null

    val readyLiveData: MutableLiveData<Boolean> = MutableLiveData()
    val mapData: MutableLiveData<MapDataPresentation> = MutableLiveData()
    val isServiceable: MutableLiveData<Boolean> = MutableLiveData()
    val activeLocationInfo: MutableLiveData<ServiceableLocation.Serviceable?> = MutableLiveData()

    private val areasReadyObservable: ReplaySubject<Boolean> = ReplaySubject.create()
    private val mapReadyObservable: PublishSubject<Boolean> = PublishSubject.create()
    private val regionSubject: PublishSubject<RegionInfo> = PublishSubject.create()
    private val locationChangedSubject: PublishSubject<LocationChangeData> = PublishSubject.create()

    private var decodedCities: List<SimpleCity> = arrayListOf()
    private var cities: MutableList<City> = arrayListOf()
    private var flow: MainFlowProvider? = null

    init {
        setupReadinessObservable()
        setupVisibleRegionChangedObservable()
        loadCities()
        setupLocationChangedObservable()
    }

    fun handlePermissions() {
        flow?.showPermissionHandler(PERMISSIONS_REQUEST_CODE)
    }

    fun setFlowCoordinator(flow: MainFlowProvider) {
        this.flow = flow
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
        readinessSubscription.dispose()
        locationDetailsSubscription?.dispose()
        locationChangedSubscription?.dispose()
    }

    fun polygonBounds(code: String?, path: String?): LatLngBounds? {
        if (code == null || code.count() != 3 || path == null) {
            //Path or Country code are not valid
            return null
        }
        val polygon = MapUtil.decodePolygon(path)
        return MapUtil.createLatLngsBounds(polygon)
    }

    fun cityBounds(code: String): LatLngBounds? {
        if (code.count() != 3) {
            //Country code is not valid
            return null
        }
        val city = decodedCities.filter { it.code == code }.first()
        val workingAreas = city.workingArea?.flatten() ?: return null
        return MapUtil.createLatLngsBounds(workingAreas)

    }

    fun requestLocationDetails(latLng: LatLng, provider: RxGeoCoder.LocationInformationInterface) {
        locationChangedSubject.onNext(LocationChangeData(latLng, provider))
    }

    private fun setupLocationChangedObservable() {
        locationChangedSubscription = locationChangedSubject
            .debounce(300, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchLocationDetails(it.latLng, it.provider)
            }, {
                it.printStackTrace()
            })
    }

    private fun fetchLocationDetails(latlng: LatLng, provider: RxGeoCoder.LocationInformationInterface) {
        val locationDetailsObserver = RxGeoCoder.locationDetails(latLng = latlng, provider = provider).toObservable()

        locationDetailsSubscription = Observable
            .zip<RxGeoCoder.Result, Boolean, Pair<RxGeoCoder.Result, Boolean>>(locationDetailsObserver,
                areasReadyObservable, BiFunction { result, areasLoaded ->
                    Log.e("SHAFIC", "combineLatest $areasLoaded ${result.locationInformation?.city}")
                    return@BiFunction Pair(result, areasLoaded)
                })
            .subscribeOn(Schedulers.io())
            .filter { it.second }
            .flatMap { pair ->
                return@flatMap serviceMapper(pair.first, decodedCities)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { serviceableLocation ->
                when (serviceableLocation) {
                    is ServiceableLocation.Serviceable -> {
                        //TODO: ServiceableLocationInformation
                        Log.e("SHAFIC", "Serviceable || ${serviceableLocation.city}")
                        isServiceable.value = true
                        activeLocationInfo.value = serviceableLocation
                    }
                    is ServiceableLocation.NotServiceable -> {
                        //TODO: Not Serviceable
                        Log.e("SHAFIC", "Not Serviceable")
                        isServiceable.value = false
                        activeLocationInfo.value = null
                    }
                    is ServiceableLocation.Error -> {
                        //TODO: Handle Error
                        Log.e("SHAFIC", serviceableLocation.result.error?.description)
                    }
                }
            }
            .subscribe({ }, { throwable ->
                throwable?.printStackTrace()
            })
    }

    private fun serviceMapper(
        result: RxGeoCoder.Result, cities: List<SimpleCity>
    ): Observable<ServiceableLocation> {
        val info = result.locationInformation ?: return Observable.just(ServiceableLocation.Error(result = result))

        val countryCode = info.country.code
        val cities = CityManager.filterBy(latLng = info.latLng, andCountryCode = countryCode, list = cities)

        val serviceableLocation = ServiceableLocation.NotServiceable(cities?.firstOrNull(), result = result)
        val cityCode = serviceableLocation.cityCode ?: return Observable.just(serviceableLocation)
        val thoroughfare = serviceableLocation.result?.locationInformation?.thoroughfare

        return getCityByCode(code = cityCode)
            .toObservable()
            .subscribeOn(Schedulers.io())
            .map {
                val city = it //?: return@map ServiceableLocation.Error(result = result)
                return@map ServiceableLocation.Serviceable(
                    city = city,
                    thoroughfare = thoroughfare
                )
            }
    }

    private fun getCityByCode(code: String): Maybe<City> {
        return citiesApi.get(code = code)
    }

    private fun setupReadinessObservable() {
        // If you introduce RxKotlin then you can use type inference
        // Observables.combineLatest(name, age) { n, a -> "$n - age:${a}" }.subscribe()
        readinessSubscription = Observable
            .combineLatest<Boolean, Boolean, Boolean>(
                areasReadyObservable,
                mapReadyObservable,
                BiFunction { areasReady, mapReady ->
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
            ZoomContext.MarkersFriendly -> {
                //Represent each cities once on this zoom level
                val cities = CityManager.filterByAproxCenter(
                    list = decodedCities.toList(),
                    withinBounds = info.visibleRegionPolygon
                )

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = cities)
                )
            }
            ZoomContext.PolygonsFriendly -> {

                val cities = CityManager.filterByWorkingAreas(
                    list = decodedCities.toList(),
                    withinBounds = info.visibleRegionPolygon
                )

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = cities)
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
                this.cities = cities.toMutableList()// ?: return@doOnSuccess
                this.decodedCities = CityManager.convertToDecodedCities(this.cities)
            }
            .subscribe({ onRetrieveCitiesSuccess() }, { onRetrieveCitiesError(it) })
    }

    private fun onRetrieveCitiesSuccess() {
        areasReadyObservable.onNext(true)
        areasReadyObservable.onComplete()
    }

    private fun onRetrieveCitiesError(throwable: Throwable) {
        areasReadyObservable.onNext(false)
        throwable.printStackTrace()
    }

    fun log(message: String = "EMPTY_LOG") {
        if (!message.isBlank()) {
            Log.d("SHAFIC", "LOGGER: $message")
        }
    }

    fun requestMapDisplayInfo(zoom: Float, center: LatLng, visibleRegionPolygon: List<LatLng>?) {
        val regionInfo = RegionInfo(zoom, center, visibleRegionPolygon)
        log("zoom: $zoom")
        regionSubject.onNext(regionInfo)
    }

    fun shouldCheckIfLocationIsServiceable(zoom: Float): Boolean {
        return when (ZoomContext.create(zoom = zoom)) {
            ZoomContext.PolygonsFriendly -> true
            else -> false
        }
    }
}




