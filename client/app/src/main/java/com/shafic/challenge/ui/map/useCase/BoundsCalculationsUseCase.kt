package com.shafic.challenge.ui.map.useCase

import com.google.android.gms.maps.model.LatLngBounds
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.data.presentation.SimpleCity

interface BoundsCalculationsUseCase {
    fun boundsOfPolygon(code: String?, path: String?): LatLngBounds?
    fun boundsOfCity(code: String, inDecodedCities: List<SimpleCity>): LatLngBounds?
}

class BoundsCalculationsUseCaseImp : BoundsCalculationsUseCase {
    override fun boundsOfPolygon(code: String?, path: String?): LatLngBounds? {
        if (code == null || code.count() != 3 || path == null) {
            //Path or Country code are not valid
            return null
        }
        val polygon = MapUtil.decodePolygon(path)
        return MapUtil.createLatLngsBounds(polygon)
    }

    override fun boundsOfCity(code: String, inDecodedCities: List<SimpleCity>): LatLngBounds? {
        if (code.count() != 3) {
            //Country code is not valid
            return null
        }

        val city = inDecodedCities.first { it.code == code }
        //inDecodedCities.filter { it.code == code }.first()
        val workingAreas = city.workingArea?.flatten() ?: return null
        return MapUtil.createLatLngsBounds(workingAreas)
    }
}
