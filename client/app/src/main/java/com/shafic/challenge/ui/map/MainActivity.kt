package com.shafic.challenge.ui.map

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.shafic.challenge.R
import com.shafic.challenge.common.*
import com.shafic.challenge.common.base.AbstractMapActivity
import com.shafic.challenge.common.dialogs.DialogProvider
import com.shafic.challenge.common.dialogs.DialogProviderImplementation
import com.shafic.challenge.common.ui.AdvancedGoogleMapFragment
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.common.util.Util
import com.shafic.challenge.data.presentation.MapDataPresentation
import com.shafic.challenge.data.presentation.ServiceableLocation
import com.shafic.challenge.databinding.ActivityMainBinding
import com.shafic.challenge.injection.ViewModelFactory
import com.shafic.challenge.navigation.coordinators.MainFlowCoordinator
import com.shafic.challenge.ui.cityPicker.CityPickerActivity
import com.shafic.challenge.ui.permission.PermissionsActivity
import com.vanniktech.rxpermission.RealRxPermission
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

sealed class DialogType {
    class CityPicker(val extra: String?) : DialogType()
    class GeoCoderError(val message: String?) : DialogType()
}

class MainActivity : AbstractMapActivity<ActivityMainBinding>(), OnMapReadyCallback,
    AdvancedGoogleMapFragment.MapScrollListener {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        fun intent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

    private val infoBoxHeight: Int by lazy {
        baseContext?.resources?.getDimensionPixelSize(
            R.dimen
                .info_box_height
        ) ?: 0
    }
    private val boundingPadding: Int by lazy {
        resources?.getDimensionPixelSize(R.dimen.bounds_padding) ?: 0
    }
    private lateinit var viewModel: MainActivityViewModel
    private val dialogSubject: BehaviorSubject<DialogType> = BehaviorSubject.create()
    private val compositeDisposable = CompositeDisposable()
    private var networkErorrDisposable: Disposable? = null
    
    private val dialogProvider: DialogProvider by lazy { DialogProviderImplementation(context = this) }

    override val pinIcon: BitmapDescriptor by lazy {
        return@lazy Util.loadBitmapFromVector(
            applicationContext,
            R.drawable.v_icon_pin_medium_selected,
            R.color.colorPrimary
        )
            .getDescriptor()
    }
    override val mapFragmentId: Int
        get() = R.id.map_fragment
    override val layoutId: Int
        get() = R.layout.activity_main
    override val requestsUserLocation: Boolean
        get() = true

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        handleCityPickerResult(requestCode, resultCode, data)
        handlePermissionsResult(requestCode, resultCode, data)
    }

    private fun handlePermissionsResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // isGranted, if Null activity result was not ours
        val isGranted = PermissionsActivity.handleActivityResult(requestCode, resultCode, data) ?: return
        if (isGranted) {
            setupUserLocationListener()
        } else {
            dialogProvider.createCityPickerDialog { viewModel.showCityPickerWithResult() }?.show()
        }
        updateDisplayData()
    }

    private fun handleCityPickerResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // selectedItem, if Null activity result was not ours
        val selectedItem = CityPickerActivity.handleActivityResult(requestCode, resultCode, data) ?: return
        viewModel.updateSelectedLocation(selectedItem)
    }

    override fun onCreateViewDataBinding(savedInstanceState: Bundle?): ActivityMainBinding? {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(MainActivityViewModel::class.java)
        viewModel.setFlowCoordinator(MainFlowCoordinator(this))
        viewModel.setLocationInformationProvider(RxGeoCoder.LocationInformationProvider(context = this) as RxGeoCoder.LocationInformationInterface)

        viewBinding()?.viewModel = viewModel
        addLiveDataObservers()
        updateDisplayData()
        setupDialogSubject()
    }

    override fun onResume() {
        super.onResume()
        setupNetworkErrorListener()
        viewModel.loadDataIfNeeded()
    }
    
    override fun onPause() {
        networkErorrDisposable?.dispose()
        super.onPause()
    }
    
    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onMapLoaded(googleMap: GoogleMap) {
        viewModel.setMapStateReady(true)
        centerPinVerticallyInMap()
        googleMap.setPadding(0, 0, 0, infoBoxHeight)
    }

    override fun onMapMarkerClicked(marker: Marker): Boolean {
        val tag = marker.tag as? String ?: return super.onMapMarkerClicked(marker)
        val latLng = viewModel.cityBounds(code = tag) ?: return super.onMapMarkerClicked(marker)
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLng, boundingPadding))
        return true
    }

    override fun onMapPolygonClicked(polygon: Polygon) {
        val pair = MapUtil.decodePolygonTag(polygon.tag as String)
        val latLng = viewModel.polygonBounds(pair?.first, pair?.second) ?: return
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLng, boundingPadding))
    }


    override fun onGoogleServicesNotAvailable() {
        toast(this, "The App needs PlayServices to work Retry or Quit App")
    }


    override fun onUserLocationReady(latLng: LatLng?, error: Throwable?) {
        latLng?.let { latLng ->
            moveTo(latLng = latLng, zoom = 16.0f)
            requestLocationInformation(latLng)
        }
    }

    override fun onUserLocationPermissionFailure(fineLocationPermissionError: Int, coarseLocationPermissionError: Int) {
        //Note: This activity does not handle location permissions by itself, it delegates Action to another Activity
        dialogProvider.createNeedsPermissionAlert { viewModel.handlePermissions() }?.show()
    }

    override fun onMapScrollEnded() {
        requestMapDataUpdate()
    }

    //endregion
    private fun centerPinVerticallyInMap() {
        val binding = viewBinding() ?: return
        val pinView = binding.serviceableIndicatorPin
        val root = binding.root
        val newCenter = ((root.height - infoBoxHeight.toFloat()) / 2) - pinView.height / 2
        pinView.y = newCenter
    }

    private fun addLiveDataObservers() {
        viewModel.activeLocationInfo().observe(this, android.arch.lifecycle.Observer { locationInfo ->
            updateBindingData(locationInfo)
        })
        viewModel.selectedCityName().observe(this, android.arch.lifecycle.Observer { city ->
            this.viewBinding()?.viewModel = viewModel
            val bounds = viewModel.selectedCityLatLngBounds() ?: return@Observer
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, boundingPadding))
        })
        viewModel.isServiceable().observe(this, android.arch.lifecycle.Observer { serviceable ->
            this.viewBinding()?.serviceable = serviceable
            if (serviceable == false) {
                viewBinding()?.textviewCenterServiceable?.text = getString(R.string.not_serviceable_message)
                if (viewModel.isAreaVisibleEnough(map.cameraPosition.zoom)) {
                    dialogSubject.onNext(DialogType.CityPicker(null))
                }
            }
        })
        viewModel.readyLiveData().observe(this, android.arch.lifecycle.Observer { ready ->
            if (ready == true) {
                requestMapDataUpdate()
            }
        })
        viewModel.getGoeCoderErrorMessage().observe(this, android.arch.lifecycle.Observer { message ->
            this.viewBinding()?.serviceable = false

            dialogSubject.onNext(DialogType.GeoCoderError(message))
        })
        viewModel.mapData().observe(this, android.arch.lifecycle.Observer {
            val data = it ?: return@Observer
            when (data) {
                is MapDataPresentation.Polygons -> {
                    //Draw polygons data.value
                    addPolygons(data.value, showBounds = map.cameraPosition.zoom > 10)
                }
                is MapDataPresentation.Markers -> {
                    viewBinding()?.textviewCenterServiceable?.text = getString(R.string.zoom_to_check_service_message)
                    //Draw polygons Markers
                    addMarkers(data.value)
                }
            }
        })
    }

    private fun updateBindingData(locationInfo: ServiceableLocation.Serviceable?) {
        val binding = viewBinding() ?: return
        binding.textviewCenterInfo.text =
                resources.getString(R.string.text_view_city_thoroughfare, locationInfo?.thoroughfare ?: "")
        binding.textviewCityName.text =
                resources.getString(R.string.text_view_city_name, locationInfo?.city?.name ?: "")
        binding.textviewCityCountry.text =
                resources.getString(R.string.text_view_city_country, locationInfo?.city?.countryCode ?: "")
        binding.textviewCityCurrency.text =
                resources.getString(R.string.text_view_city_currency, locationInfo?.city?.currency ?: "")
        binding.textviewCityLanguage.text =
                resources.getString(R.string.text_view_city_language, locationInfo?.city?.languageCode ?: "")
        binding.textviewCityTimezone.text =
                resources.getString(R.string.text_view_city_timezone, locationInfo?.city?.timeZone ?: "")
    }

    private fun requestLocationInformation(lngLat: LatLng) {
        viewModel.requestLocationDetails(latLng = lngLat)
    }

    private fun requestMapDataUpdate() {
        val polygon = map.projection.visibleRegion.polygon()
        val center = map.projection.visibleRegion.latLngBounds.center
        val zoom = map.cameraPosition.zoom
        viewModel.requestMapDisplayInfo(zoom, center, polygon)
    }

    private fun setupDialogSubject() {
        val disposable = dialogSubject
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    is DialogType.CityPicker -> dialogProvider.createCityPickerDialog { viewModel.showCityPickerWithResult() }?.show()
                    is DialogType.GeoCoderError -> dialogProvider.createGeoCoderErrorDialog(it.message)?.show()
                }
            }, { it.printStackTrace() })
        compositeDisposable.add(disposable)
    }

    private fun updateDisplayData() {
        val isGranted = isLocationPermissionGranted()
        val messageId =
            if (isGranted) R.string.text_view_permission_granted else R.string.text_view_permission_not_granted
        viewModel.setPermissionMessage(resources.getString(messageId))
        viewModel.setIsPermissionGranted(isGranted)
        val binding = viewBinding()
        binding?.viewModel = viewModel
    }

    private fun isLocationPermissionGranted(): Boolean {
        return RealRxPermission.getInstance(application).isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun setupNetworkErrorListener() {
        networkErorrDisposable = RxBus.events()
            .debounce(200, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event: BaseEvent? ->
                when (event) {
                    is BaseEvent.ConnectionFailed -> {
                        //Error Info: val error = event.error
                        viewModel.reset()
                        dialogProvider.createNetworkErrorDialog { viewModel.loadCities() }?.show()
                    }
                }
            }, { throwable: Throwable? ->
                throwable?.printStackTrace()
            })
    }
}
