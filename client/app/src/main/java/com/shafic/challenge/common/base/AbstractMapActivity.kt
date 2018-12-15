package com.shafic.challenge.common.base

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.content.ContextCompat
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
import com.shafic.challenge.common.ui.AdvancedGoogleMapFragment
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.data.presentation.SimpleCity

abstract class AbstractMapActivity<B : ViewDataBinding> : AbstractBaseActivity<B>(), OnMapReadyCallback,
    AdvancedGoogleMapFragment.MapScrollListener {
    companion object {
        internal const val GAPI_ERROR_DIALOG_RESULT = 12345
    }

    //region ABSTRACT PROPERTIES
    abstract val mapFragmentId: Int
    abstract val requestsUserLocation: Boolean
    //endregion

    //region ABSTRACT METHODS
    abstract fun onMapLoaded(googleMap: GoogleMap)

    abstract fun onGoogleServicesNotAvailable()

    abstract fun onUserLocationReady(latLng: LatLng?, error: Throwable?)

    abstract fun onUserLocationPermissionFailure(fineLocationPermissionError: Int, coarseLocationPermissionError: Int)
    //endregion

    //region M A P  S C R O L L  L I S T E N E R
    override fun onMapScrollStarted() {
        //Do stuff
    }

    override fun onMapClicked() {
        //Do stuff
    }

    override fun onMapScrollEnded() {
        //Do stuff
    }
    //endregion 

    private var viewDataBinding: B? = null
    private var mapFragment: AdvancedGoogleMapFragment? = null
        get() {
            return supportFragmentManager
                .findFragmentById(mapFragmentId) as AdvancedGoogleMapFragment
        }

    internal lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapFragment?.getMapAsync(this)

        //Note: onCreated is already called in super's BaseActivity

        if (requestsUserLocation) {
            setupUserLocationListener()
        }
    }

    override fun onResume() {
        super.onResume()
        handleGoogleServicesAvailability()
        onResumed()
    }

    internal fun onResumed() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GAPI_ERROR_DIALOG_RESULT) {
            handleGoogleServicesAvailabilityResult(resultCode, data)
        }
    }

    //region OnMapReadyCallback
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        mapFragment?.setMapScrollListener(this)
        onMapLoaded(map)
    }
    //endregion

    //region UTILS
    internal fun addMarkers(list: List<SimpleCity>) {
        map.clear()
        //completion: (Map<String, String>) -> Unit
        //var mapItems = mutableMapOf<String, String>()
        list.forEach {
            val latLng = it.latLng ?: return@forEach
            addMarker(latLng, it.code)
        }
    }

    internal fun addMarker(position: LatLng, tag: String) {
        val marker = map.addMarker(MarkerOptions().position(position))
        marker.tag = tag
    }

    internal fun addPolygons(list: List<SimpleCity>, showBounds: Boolean) {
        map.clear()
        list.forEach { city ->
            val workingAreas = city.workingArea ?: return@forEach
            workingAreas.forEach { polygon ->
                if (polygon.isNotEmpty()) {
                    addPolygon(polygon, city.code, showBounds)
                }
            }
        }
    }

    internal fun addPolygon(polygon: List<LatLng>, tag: String, showBounds: Boolean) {
        val polygon1 = map.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(polygon)
        )
        // Store a data object with the polygon, used here to indicate an arbitrary type.

        polygon1.tag = MapUtil.encodePolygonTag(polygon, tag)
        MapUtil.stylePolygon(polygon1, showBounds)
    }

    internal fun moveTo(latLng: LatLng, zoom: Float? = null) {
        val cameraUpdateFactory =
            if (zoom != null) CameraUpdateFactory.newLatLngZoom(latLng, zoom) else CameraUpdateFactory.newLatLng(latLng)
        map.moveCamera(cameraUpdateFactory)
    }
    //endregion

    //region HANDLE GOOGLE SERVICES AVAILABILITY
    private fun handleGoogleServicesAvailabilityResult(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            //Retry by calling 'handleGoogleServicesAvailability()' or Quit App
            onGoogleServicesNotAvailable()
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
    //endregion

    //region USER LOCATION REQUEST [DOES NOT HANDLE PERMISSION REQUEST]
    private fun setupUserLocationListener() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        val fineLocationPermissionCheck: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermissionCheck: Int =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED
        ) {
            client?.lastLocation
                ?.addOnSuccessListener(this, OnSuccessListener { location ->
                    if (location == null) {
                        return@OnSuccessListener
                    }

                    val latLng = LatLng(location.latitude, location.longitude)
                    onUserLocationReady(latLng = latLng, error = null)
                })
                ?.addOnFailureListener(this) { error ->
                    onUserLocationReady(latLng = null, error = error)
                }
        } else {
            onUserLocationPermissionFailure(fineLocationPermissionCheck, coarseLocationPermissionCheck)
        }
    }
    //endregion
}
