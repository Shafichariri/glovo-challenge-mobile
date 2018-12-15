package com.shafic.challenge.ui.map

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.shafic.challenge.R
import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.common.base.AbstractMapActivity
import com.shafic.challenge.common.polygon
import com.shafic.challenge.common.toast
import com.shafic.challenge.common.ui.AdvancedGoogleMapFragment
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.data.presentation.MapDataPresentation
import com.shafic.challenge.databinding.ActivityMainBinding
import com.shafic.challenge.injection.ViewModelFactory


class MainActivity : AbstractMapActivity<ActivityMainBinding>(), OnMapReadyCallback,
    AdvancedGoogleMapFragment.MapScrollListener {
    companion object {
        const val NEED_LOCATION_PERMISSIONS = 99807
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

    override val mapFragmentId: Int
        get() = R.id.map
    override val layoutId: Int
        get() = R.layout.activity_main
    override val requestsUserLocation: Boolean
        get() = true

    override fun onCreateViewDataBinding(savedInstanceState: Bundle?): ActivityMainBinding? {
        return DataBindingUtil.setContentView(this, layoutId)
    }

    override fun onCreated(savedInstanceState: Bundle?) {
        actionBar?.title = resources.getString(R.string.action_bar_title_landing)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(MainActivityViewModel::class.java)
        viewBinding()?.viewModel = viewModel
        addLiveDataObservers()
    }

    override fun onMapLoaded(googleMap: GoogleMap) {
        viewModel.setMapStateReady(true)
        viewBinding()?.let {
            val newCenter = ((it.root.height - infoBoxHeight.toFloat()) / 2) - it.serviceableIndicatorPin.height / 2
            it.serviceableIndicatorPin.y = newCenter
        }
        googleMap.setPadding(0, 0, 0, infoBoxHeight)
        googleMap.setOnPolygonClickListener {
            val pair = MapUtil.decodePolygonTag(it.tag as String)
            viewModel.polygonBounds(pair?.first, pair?.second)?.let {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(it, boundingPadding))
            }

        }
        googleMap.setOnMarkerClickListener { marker ->
            val tag = marker.tag as? String ?: return@setOnMarkerClickListener false
            viewModel.cityBounds(code = tag)?.let {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(it, boundingPadding))
            }
            return@setOnMarkerClickListener true
        }
    }

    override fun onGoogleServicesNotAvailable() {
        //TODO: Show a terminal Dialog telling the user that the App needs PlayServices to work
        toast(this, "The App needs PlayServices to work Retry or Quit App")
    }


    override fun onUserLocationReady(latLng: LatLng?, error: Throwable?) {
        latLng?.let { latLng ->
            moveTo(latLng = latLng, zoom = 16.0f)
            requestLocationInformation(latLng)
        }
        //TODO: handle error if any
    }

    override fun onUserLocationPermissionFailure(fineLocationPermissionError: Int, coarseLocationPermissionError: Int) {
        //Note: This activity does not handle location permissions by itself, delegate Action to Activity launcher
        showNeedsPermissionAlert()
    }

    override fun onMapScrollEnded() {
        requestMapDataUpdate()

        if (viewModel.shouldCheckIfLocationIsServiceable(zoom = map.cameraPosition.zoom)) {
            requestLocationInformation(map.projection.visibleRegion.latLngBounds.center)
        }
    }

    //endregion

    private fun addLiveDataObservers() {
        viewModel.activeLocationInfo.observe(this, android.arch.lifecycle.Observer { locationInfo ->
            val binding = viewBinding() ?: return@Observer
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
        })
        
        viewModel.isServiceable.observe(this, android.arch.lifecycle.Observer { serviceable ->
            this.viewBinding()?.serviceable = serviceable
        })
        viewModel.readyLiveData.observe(this, android.arch.lifecycle.Observer { ready ->
            if (ready == true) {
                requestMapDataUpdate()
            }
        })
        viewModel.mapData.observe(this, android.arch.lifecycle.Observer {
            val data = it ?: return@Observer
            when (data) {
                is MapDataPresentation.Polygons -> {
                    //Draw polygons data.value
                    addPolygons(data.value, showBounds = map.cameraPosition.zoom > 10)
                }
                is MapDataPresentation.Markers -> {
                    //Draw polygons Markers
                    addMarkers(data.value)
                }
            }
        })
    }

    private fun requestLocationInformation(lngLat: LatLng) {
        val locationInformationProvider = RxGeoCoder.LocationInformationProvider(context = this)
        viewModel.requestLocationDetails(
            latLng = lngLat,
            provider = locationInformationProvider as RxGeoCoder.LocationInformationInterface
        )
    }

    private fun requestMapDataUpdate() {
        val polygon = map.projection.visibleRegion.polygon()
        val center = map.projection.visibleRegion.latLngBounds.center
        val zoom = map.cameraPosition.zoom
        viewModel.requestMapDisplayInfo(zoom, center, polygon)
    }

    private fun showNeedsPermissionAlert() {
        val alertDialog = AlertDialog.Builder(this).create()
        val positiveButtonTitle = getString(R.string.ok)
        alertDialog.setTitle(getString(R.string.dialog_location_permission_lost_show_title))
        alertDialog.setMessage(getString(R.string.dialog_location_permission_lost_show_message))
        alertDialog.setButton(
            AlertDialog.BUTTON_NEUTRAL, positiveButtonTitle
        ) { _, _ ->
            setResult(NEED_LOCATION_PERMISSIONS)
            finish()
        }

        alertDialog.show()
    }
}
