package com.shafic.challenge.data.presentation

import com.google.android.gms.maps.model.LatLng

data class RegionInfo(val zoom: Float, val center: LatLng, val visibleRegionPolygon: List<LatLng>?)
