package com.shafic.challenge.ui.map.useCase

import com.shafic.challenge.common.Optional
import com.shafic.challenge.data.presentation.MapDataPresentation
import com.shafic.challenge.data.presentation.RegionInfo
import com.shafic.challenge.data.presentation.SimpleCity
import com.shafic.challenge.data.presentation.ZoomContext
import com.shafic.challenge.helpers.CityHelper


interface MapDataChangedUseCase {
    fun update(info: RegionInfo, decodedCities: List<SimpleCity>): Optional<MapDataPresentation>
    fun hasServiceableAreas(optionalDataPresentation: Optional<MapDataPresentation>): Boolean
}

class MapDataChangedUseCaseImp : MapDataChangedUseCase {

    override fun hasServiceableAreas(optionalDataPresentation: Optional<MapDataPresentation>): Boolean {
        val result = optionalDataPresentation.value ?: return false
            when(result) {
                is MapDataPresentation.Polygons -> {
                    return result.value.isNotEmpty()
                }
            }
        return false
    }

    override fun update(info: RegionInfo, decodedCities: List<SimpleCity>): Optional<MapDataPresentation> {
        // TODO: Keep the last filtered items and detect if the new region is a sub-region of the previous one 
        // TODO: If that was the case then do the filtered on the already filtered items

        val zoomContext = ZoomContext.create(info.zoom)
        
        when (zoomContext) {
            ZoomContext.MarkersFriendly -> {
                //Represent each cities once on this zoom level
                val cities = CityHelper.filterByAproxCenter(
                    list = decodedCities.toList(),
                    withinBounds = info.visibleRegionPolygon
                )

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = cities)
                )
            }
            ZoomContext.PolygonsFriendly -> {
                val cities = CityHelper.filterByWorkingAreas(
                    list = decodedCities.toList(),
                    withinBounds = info.visibleRegionPolygon,
                    inverseCheck = ZoomContext.shouldCheckInverse(info.zoom)
                )

                return Optional(
                    MapDataPresentation
                        .create(zoomContext, cityMarkers = cities)
                )
            }

            ZoomContext.None -> {
                return Optional.empty()
            }
        }
    }
}
