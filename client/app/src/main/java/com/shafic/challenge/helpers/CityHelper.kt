package com.shafic.challenge.helpers

import com.google.android.gms.maps.model.LatLng
import com.shafic.challenge.common.util.MapUtil
import com.shafic.challenge.data.models.City
import com.shafic.challenge.data.presentation.SimpleCity

class CityHelper {
    companion object {
        /**
         * Filters only the cities if the approximate calculated center of the working areas is within bounds.
         * 1. Get the Center of the working areas of a city
         * 2. Filters only the centers that are within bounds
         * */
        fun filterByAproxCenter(list: List<SimpleCity>?, withinBounds: List<LatLng>?): List<SimpleCity>? {
            val visibleBounds = withinBounds ?: return null
            val cities = list ?: return null

            return cities
                .mapNotNull {
                    //Get the Center of the working areas of a city
                    val cityWorkingAreas = it.workingArea
                    val allAreasLatLngs = cityWorkingAreas?.flatten() ?: return@mapNotNull null

                    //Return the city with the new aprox center of working area [Using Kotlin's Copy constructor]
                    return@mapNotNull it.copy(latLng = MapUtil.centerOfLatLngs(allAreasLatLngs), workingArea = null)
                }
                .filter {
                    //Check if the calculated center of the working areas of a city is within the region bounds and filter
                    val latLng = it.latLng ?: return@filter false

                    return@filter MapUtil.isPointWithinBounds(visibleBounds, latLng, true)
                }
        }

        fun filterByWorkingAreas(list: List<SimpleCity>?, withinBounds: List<LatLng>?, inverseCheck: Boolean): List<SimpleCity>? {

            val visibleBounds = withinBounds ?: return null
            val simpleCities = list ?: return null

            return simpleCities
                .mapNotNull { city ->
                    val areas = city.workingArea
                        ?.filter { area ->
                            //Filter the City's working areas that are visible within the given bounds
                            return@filter isAreaVisible(visibleBounds, area, inverseCheck = inverseCheck)
                        }
                        ?: return@mapNotNull null

                    //If not empty return only the Filtered working area of the city [Using Kotlin's Copy constructor]
                    return@mapNotNull if (areas.isEmpty()) null else city.copy(workingArea = areas)
                }
        }

        private fun isAreaVisible(visibleBounds: List<LatLng>, area: List<LatLng>, inverseCheck: Boolean): Boolean {
            if (!inverseCheck) {
                return MapUtil.isPolygonWithinBounds(visibleBounds, area)
            }
            return MapUtil.isPolygonWithinBounds(visibleBounds, area) ||
                    MapUtil.isPolygonWithinBounds(area, visibleBounds)
        }

        fun filterBy(latLng: LatLng, andCountryCode: String?, list: List<SimpleCity>?): List<SimpleCity>? {
            val countryCode = andCountryCode ?: return null
            val cities = list ?: return null

            return cities
                .filter { it.countryCode == countryCode }.mapNotNull { city ->
                    val workingArea = city.workingArea?.filter { workingArea ->
                        MapUtil.isPointWithinBounds(bounds = workingArea, point = latLng, geodesic = true)
                    }

                    if (workingArea?.size ?: 0 > 0) {
                        return@mapNotNull city.copy(workingArea = workingArea)
                    } else {
                        return@mapNotNull null
                    }
                }
        }

        fun convertToDecodedCities(cities: List<City>): List<SimpleCity> {
            return cities.map {
                //Decode working areas
                val workingArea = it.workingArea.map { encodedPolygon -> MapUtil.decodePolygon(encodedPolygon) }

                //Create a SimpleCity using the City model info with the decoded areas
                //Note: Use any point in the areas as latlng presenter of a city
                return@map SimpleCity(
                    it.code,
                    latLng = workingArea.firstOrNull()?.firstOrNull(),
                    workingArea = workingArea,
                    countryCode = it.countryCode
                )
            }
        }
    }
}
