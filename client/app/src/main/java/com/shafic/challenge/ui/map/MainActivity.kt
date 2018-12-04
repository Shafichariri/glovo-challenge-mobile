package com.shafic.challenge.ui.map

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.shafic.challenge.R
import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.common.polygon
import com.shafic.challenge.common.toast
import com.shafic.challenge.common.ui.AdvancedGoogleMapFragment
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.data.presentation.MapDataPresentation
import com.shafic.challenge.data.presentation.SimpleCity
import com.shafic.challenge.injection.ViewModelFactory


class MainActivity : AppCompatActivity(), OnMapReadyCallback, AdvancedGoogleMapFragment.MapScrollListener {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val GAPI_ERROR_DIALOG_RESULT = 12345
        fun intent(context: Context): Intent = Intent(context, MainActivity::class.java)

    }

    private lateinit var map: GoogleMap
    private lateinit var viewModel: MainActivityViewModel

    private var mapFragment: AdvancedGoogleMapFragment? = null
        get() {
            return supportFragmentManager
                .findFragmentById(R.id.map) as AdvancedGoogleMapFragment
        }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("SHAFIC", "onNewIntent")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("SHAFIC", "onCreate")

        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(MainActivityViewModel::class.java)
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
                    addPolygons(data.value)
                }
                is MapDataPresentation.Markers -> {
                    //Draw polygons Markers
                    addMarkers(data.value)
                }
                is MapDataPresentation.ClusteredMarkers -> {
                    //Draw polygons clustered Markers
                    addMarkers(data.value)
                }
            }
        })
        mapFragment?.getMapAsync(this)

        setupUserLocationListener()
    }


    /**
     * This functions should not be launch if permission was not granted beforehand.
     * */
    @SuppressWarnings("MissingPermission")
    private fun setupUserLocationListener() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        client?.lastLocation?.addOnSuccessListener(this, OnSuccessListener { location ->
            if (location == null) {
                return@OnSuccessListener
            }

            val latLng = LatLng(location.latitude, location.longitude)
            moveTo(latLng)

            val locationInformationProvider = RxGeoCoder.LocationInformationProvider(context = this)
            viewModel.requestLocationDetails(
                latlng = latLng,
                provider = locationInformationProvider as RxGeoCoder.LocationInformationInterface
            )

        })?.addOnFailureListener(this) { e -> Log.w(TAG, "getLastLocation:onFailure", e) }
    }

    private fun addMarkers(list: List<SimpleCity>) {
        map.clear()

        list.forEach {
            val latLng = it.latLng ?: return@forEach
            addMarker(latLng, it.code)
        }
    }

    private fun addPolygons(list: List<SimpleCity>) {
        map.clear()
        list.forEach { city ->
            val workingAreas = city.workingArea ?: return@forEach
            workingAreas.forEach { polygon ->
                if (polygon.isNotEmpty()) {
                    addPolygon(polygon, city.code)
                }
            }
        }
    }

    private fun addMarker(position: LatLng, tag: String) {
        val marker = map.addMarker(MarkerOptions().position(position))
        marker.tag = tag
    }

    private fun addPolygon(polygon: List<LatLng>, tag: String) {
        val polygon1 = map.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(polygon)
        )
        // Store a data object with the polygon, used here to indicate an arbitrary type.
        polygon1.tag = tag
        MapUtil.stylePolygon(polygon1)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapFragment?.setMapScrollListener(this)
        viewModel.setMapStateReady(true)
    }

    override fun onResume() {
        super.onResume()
        handleGoogleServicesAvailability()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GAPI_ERROR_DIALOG_RESULT) {
            handleGoogleServicesAvailabilityResult(resultCode, data)
        }
    }

    private fun handleGoogleServicesAvailabilityResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            //TODO: Show a terminal Dialog telling the user that the App needs PlayServices to work
            //Retry by calling 'handleGoogleServicesAvailability()' or Quit App
            toast(this, "The App needs PlayServices to work Retry or Quit App")
        }
    }

    private fun handleGoogleServicesAvailability() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val googleAPIAvailabilityResult = googleAPI.isGooglePlayServicesAvailable(applicationContext)
        val isAvailable = (googleAPIAvailabilityResult == ConnectionResult.SUCCESS)
        if (!isAvailable) {
            //Show Error provided by Google And wait for the activity result
            val errorDialog = googleAPI.getErrorDialog(
                this, googleAPIAvailabilityResult,
                GAPI_ERROR_DIALOG_RESULT
            )
            errorDialog.show()
        }
    }

    //region MAP STATE CHANGE LISTENER
    override fun onMapScrollStarted() {

    }

    override fun onMapClicked() {

    }

    override fun onMapScrollEnded() {
        requestMapDataUpdate()
    }
//endregion

    private fun requestMapDataUpdate() {
        val polygon = map.projection.visibleRegion.polygon()
        val center = map.projection.visibleRegion.latLngBounds.center
        val zoom = map.cameraPosition.zoom
        viewModel.requestMapDisplayInfo(zoom, center, polygon)
    }

    private fun moveTo(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }
}
