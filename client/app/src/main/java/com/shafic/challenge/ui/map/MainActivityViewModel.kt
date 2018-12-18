package com.shafic.challenge.ui.map

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.annotation.UiThread
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.shafic.challenge.R
import com.shafic.challenge.common.Optional
import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.common.base.BaseViewModel
import com.shafic.challenge.common.util.Util
import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.presentation.*
import com.shafic.challenge.helpers.CityHelper
import com.shafic.challenge.injection.module.app
import com.shafic.challenge.navigation.coordinators.MainFlowProvider
import com.shafic.challenge.ui.cityPicker.CityPickerActivity
import com.shafic.challenge.ui.map.useCase.*
import com.shafic.challenge.ui.permission.PermissionsActivity.Companion.PERMISSIONS_REQUEST_CODE
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit


class MainActivityViewModel : BaseViewModel() {
    data class LocationChangeData(val latLng: LatLng, val provider: RxGeoCoder.LocationInformationInterface)

    private val calculatorUseCase: BoundsCalculationsUseCase = BoundsCalculationsUseCaseImp()
    private val mapDataChangeUseCase: MapDataChangedUseCase = MapDataChangedUseCaseImp()
    private val fetchCitiesUseCase: FetchCitiesUseCase = FetchCitiesUseCaseImp()
    private val convertToCityUseCase: ConvertGeoLocationToCityUseCase = ConvertGeoLocationToCityUseCaseImp(
        FetchCityDetailsUseCaseImp()
    )
    private val serviceableLocationUseCase: LocationDetailsUseCase = LocationDetailsUseCaseImp(convertToCityUseCase)
    private val selectedCityUseCase: SelectedLocationUseCase = SelectedLocationUseCaseImp(fetchCitiesUseCase)

    private lateinit var geocoderProvider: RxGeoCoder.LocationInformationInterface

    private val compositeDisposable = CompositeDisposable()

    private val readyLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val mapData: MutableLiveData<MapDataPresentation> = MutableLiveData()
    private val isServiceable: MutableLiveData<Boolean> = MutableLiveData()
    private val activeLocationInfo: MutableLiveData<ServiceableLocation.Serviceable?> = MutableLiveData()

    private val permissionMessageLiveData: MutableLiveData<String> = MutableLiveData()
    private val permissionGrantedLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val selectedCityLiveData: MutableLiveData<String> = MutableLiveData()
    private val goeCoderErrorMessage: MutableLiveData<String> = MutableLiveData()
    private val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val areasReadyObservable: ReplaySubject<Boolean> = ReplaySubject.create()
    private val mapReadyObservable: PublishSubject<Boolean> = PublishSubject.create()
    private val regionSubject: PublishSubject<RegionInfo> = PublishSubject.create()
    private val locationChangedSubject: PublishSubject<LocationChangeData> = PublishSubject.create()

    private var decodedCities: List<SimpleCity> = arrayListOf()
    private var cities: MutableList<City> = arrayListOf()
    private var flow: MainFlowProvider? = null
    private var selectedCity: City? = null
        set(value) {
            field = value
            var displayName = value?.name
            if (displayName != null) {
                displayName = Util.formatString(R.string.text_view_location_selected, displayName)
            }

            selectedCityLiveData.value = Util.stringOrDefualt(displayName, R.string.text_view_no_location_selected)
        }

    init {
        setupReadinessObservable()
        setupVisibleRegionChangedObservable()
        loadCities()
        setupLocationChangedObservable()
    }

    fun loadDataIfNeeded() {
        if (cities.isEmpty()) {
            loadCities()
        }
    }

    fun reset() {
        isLoadingLiveData.value = false
    }

    fun readyLiveData(): LiveData<Boolean> {
        return readyLiveData
    }

    fun mapData(): LiveData<MapDataPresentation> {
        return mapData
    }

    fun isServiceable(): LiveData<Boolean> {
        return isServiceable
    }

    fun activeLocationInfo(): LiveData<ServiceableLocation.Serviceable?> {
        return activeLocationInfo
    }

    fun getPermissionMessage(): LiveData<String> {
        return permissionMessageLiveData
    }

    fun isPermissionGranted(): LiveData<Boolean> {
        return permissionGrantedLiveData
    }

    fun isLoading(): LiveData<Boolean> {
        return isLoadingLiveData
    }

    fun selectedCityName(): LiveData<String> {
        return selectedCityLiveData
    }

    fun getGoeCoderErrorMessage(): LiveData<String> {
        return goeCoderErrorMessage
    }

    fun setPermissionMessage(message: String) {
        permissionMessageLiveData.value = message
    }

    fun setIsPermissionGranted(granted: Boolean) {
        permissionGrantedLiveData.value = granted
    }

    fun updateSelectedLocation(cityItem: CityPickerActivity.SelectedItem) {
        val disposable = selectedCityUseCase.getCityDetails(cityItem.cityCode, cityItem.countryCode)
            .map { Pair(it, cityBounds(it.code)) }
            .observeOn(app().mainThread)
            .subscribe({ pair ->
                this.selectedCity = pair.first
            }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun selectedCityLatLngBounds(): LatLngBounds? {
        val city = selectedCity ?: return null
        return cityBounds(city.code)
    }

    fun setLocationInformationProvider(locationInformationProvider: RxGeoCoder.LocationInformationInterface) {
        this.geocoderProvider = locationInformationProvider
    }

    fun handlePermissions() {
        flow?.showPermissionHandler(PERMISSIONS_REQUEST_CODE)
    }

    fun showCityPickerWithResult() {
        flow?.requestCityPicker(CityPickerActivity.SELECTION_REQUEST_CODE)
    }

    fun setFlowCoordinator(flow: MainFlowProvider) {
        this.flow = flow
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun polygonBounds(code: String?, path: String?): LatLngBounds? {
        return calculatorUseCase.boundsOfPolygon(code, path)
    }

    fun cityBounds(code: String): LatLngBounds? {
        return calculatorUseCase.boundsOfCity(code, decodedCities)
    }

    fun requestLocationDetails(latLng: LatLng) {
        locationChangedSubject.onNext(LocationChangeData(latLng, geocoderProvider))
    }

    private fun setupLocationChangedObservable() {
        val disposable = locationChangedSubject
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                fetchLocationDetails(it.latLng, it.provider)
            }, {
                it.printStackTrace()
            })
        compositeDisposable.add(disposable)
    }

    private fun fetchLocationDetails(latlng: LatLng, provider: RxGeoCoder.LocationInformationInterface) {
        isLoadingLiveData.value = true
        val disposable = serviceableLocationUseCase
            .fetch(latlng, provider, areasReadyObservable, decodedCities)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext { serviceableLocation ->
                isLoadingLiveData.value = false
                when (serviceableLocation) {
                    is ServiceableLocation.Serviceable -> {
                        isServiceable.value = true
                        activeLocationInfo.value = serviceableLocation
                    }
                    is ServiceableLocation.NotServiceable -> {
                        isServiceable.value = false
                        activeLocationInfo.value = null
                    }
                    is ServiceableLocation.Error -> {
                        val message = serviceableLocation.result.error?.description ?: ""
                        goeCoderErrorMessage.value = message
                    }
                }
            }
            .subscribe({ }, { throwable ->
                throwable?.printStackTrace()
            })
        compositeDisposable.add(disposable)
    }

    private fun setupReadinessObservable() {
        val disposable = Observable
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
        compositeDisposable.add(disposable)
    }

    private fun setupVisibleRegionChangedObservable() {
        val disposable = regionSubject
            .subscribeOn(Schedulers.io())
            .map { regionInfo ->
                //Expected a Heavy Operation
                val result = mapDataChangeUseCase.update(regionInfo, decodedCities.toList())
                if (mapDataChangeUseCase.hasServiceableAreas(result)) {
                    requestLocationDetails(regionInfo.center)
                }
                return@map result
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                handleMapRepresentedData(it)
            }, {
                it.printStackTrace()
            })
        compositeDisposable.add(disposable)
    }

    @UiThread
    private fun handleMapRepresentedData(optional: Optional<MapDataPresentation>) {
        val data = optional.value ?: return
        mapData.value = data
    }

    fun setMapStateReady(ready: Boolean) {
        mapReadyObservable.onNext(ready)
        mapReadyObservable.onComplete()
    }

    fun loadCities() {
        isLoadingLiveData.value = true
        val disposable = fetchCitiesUseCase.getCities()
            .doOnSuccess { cities ->
                this.cities = cities.toMutableList()
                this.decodedCities = CityHelper.convertToDecodedCities(this.cities)
            }
            .subscribe({ onRetrieveCitiesSuccess() }, { onRetrieveCitiesError(it) })
        compositeDisposable.add(disposable)
    }

    private fun onRetrieveCitiesSuccess() {
        areasReadyObservable.onNext(true)
        areasReadyObservable.onComplete()
        isLoadingLiveData.value = false
    }

    private fun onRetrieveCitiesError(throwable: Throwable) {
        areasReadyObservable.onNext(false)
        throwable.printStackTrace()
        isLoadingLiveData.value = false
    }

    fun requestMapDisplayInfo(zoom: Float, center: LatLng, visibleRegionPolygon: List<LatLng>?) {
        val regionInfo = RegionInfo(zoom, center, visibleRegionPolygon)
        regionSubject.onNext(regionInfo)
    }

    fun isAreaVisibleEnough(zoom: Float): Boolean {
        return ZoomContext.isAreaVisibleEnough(zoom)
    }
}
