package com.shafic.challenge.data.presentation

import com.google.android.gms.maps.model.LatLng

data class SimpleCity(
    val code: String,
    val latLng: LatLng? = null,
    val workingArea: List<List<LatLng>>? = null,
    val countryCode: String
)

fun SimpleCity.id(): String {
    return "$code-$countryCode"
}
