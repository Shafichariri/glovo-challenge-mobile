package com.shafic.challenge.common

import android.content.Context
import android.location.Geocoder
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.shafic.challenge.data.models.Country
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*

class RxGeoCoder private constructor() {
    data class LocationInformation(val cityCode: String, val country: Country, val latLng: LatLng)
    data class Result(val locationInformation: LocationInformation?, val error: RxGeoCoder.LocationInformationError?)
    
    companion object {
        private val TAG = RxGeoCoder::class.java.simpleName

        fun locationDetails(latLng: LatLng, provider: LocationInformationInterface): Single<Result> {
            if (!Geocoder.isPresent()) {
                return Single.just(
                    Result(
                        locationInformation = null,
                        error = LocationInformationError.GeocoderNotPresent
                    )
                )
            }
            return Single
                .fromCallable {
                    return@fromCallable provider.getFromLocation(latLng)
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    interface LocationInformationInterface {
        @WorkerThread
        fun getFromLocation(location: LatLng): Result
    }

    enum class LocationInformationError {
        GeocoderNotPresent,
        ServiceNotAvailable,
        InvalidLatLongUsed;

        val description: String
            get() {
                return when (this) {
                    GeocoderNotPresent -> "Geocoder is Not Present"
                    ServiceNotAvailable -> "Service Is Not Available"
                    InvalidLatLongUsed -> "Invalid LatLong Used"
                }
            }
    }

    class LocationInformationProvider(val context: Context) : LocationInformationInterface {

        companion object {
            private val TAG = RxGeoCoder::class.java.simpleName
        }

        private val geocoder = Geocoder(context, Locale.getDefault())

        @WorkerThread
        override fun getFromLocation(location: LatLng): Result {
            val error: LocationInformationError

            try {
                // Using getFromLocation() returns an array of Addresses for the area immediately
                // surrounding the given latitude and longitude. The results are a best guess and are
                // not guaranteed to be accurate.
                val address = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    1
                ).firstOrNull()


                val locationInformation = address?.let {
                    val country = Country(it.countryCode, it.countryName)
                    return@let LocationInformation(it.locality, country = country, latLng = location)
                }

                return Result(locationInformation, error = null)

            } catch (ioException: IOException) {
                // Catch network or other I/O problems.
                error = LocationInformationError.ServiceNotAvailable
                Log.e(TAG, error.description, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                // Catch invalid latitude or longitude values.
                error = LocationInformationError.InvalidLatLongUsed
                Log.e(
                    TAG, "$error| Latitude = $location", illegalArgumentException
                )
            }

            return Result(locationInformation = null, error = error)
        }
    }
}
