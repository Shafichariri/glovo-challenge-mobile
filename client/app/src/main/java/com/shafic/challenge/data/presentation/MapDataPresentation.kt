package com.shafic.challenge.data.presentation

import com.shafic.challenge.common.RxGeoCoder
import com.shafic.challenge.data.models.City


sealed class ServiceableLocation {
    class Error(val result: RxGeoCoder.Result): ServiceableLocation()
    class Serviceable(val city: City, val thoroughfare: String?): ServiceableLocation()
    class NotServiceable(val simpleCity: SimpleCity?, val result: RxGeoCoder.Result?): ServiceableLocation()
    
    var cityCode: String? = null
    get() = when (this) {
        is Error -> null
        is Serviceable -> city.code
        is NotServiceable -> simpleCity?.code
    }
}


sealed class MapDataPresentation {
    class Polygons(val value: List<SimpleCity>) : MapDataPresentation()
    class Markers(val value: List<SimpleCity>) : MapDataPresentation()

    companion object {
        fun create(
            zoom: ZoomContext,
            cityMarkers: List<SimpleCity>? = null
        ): MapDataPresentation? {
            if (cityMarkers == null) {
                throw Exception("Can not instantiate class of MapDataPresentation with a Null markers property")
            }
            return when (zoom) {
                ZoomContext.MarkersFriendly -> {
                    MapDataPresentation.Markers(value = cityMarkers)
                }
                ZoomContext.PolygonsFriendly -> {
                    MapDataPresentation.Polygons(value = cityMarkers)
                }
                else -> {
                    null
                }
            }
        }
    }
}
