package com.shafic.challenge.common.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polygon
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

class MapUtil {
    companion object {
        private const val COLOR_SEMI_PURPLE_ARGB = 0x7F6d65e2
        private const val COLOR_SEMI_TRANSPARENT = 0x7F73c9d3
        private const val POLYGON_STROKE_WIDTH_PX = 1.0f
        private const val POLYGON_DECODING_TAG_DELIMITER = "|:|"
        
        fun stylePolygon(polygon: Polygon, showBounds: Boolean) {
            var strokeColor = COLOR_SEMI_PURPLE_ARGB
            var fillColor = COLOR_SEMI_TRANSPARENT

            polygon.strokeWidth = if (showBounds) POLYGON_STROKE_WIDTH_PX else 0.0f
            polygon.strokeColor = strokeColor
            polygon.fillColor = fillColor
        }

        fun joinPolygons(polygon: List<List<LatLng>>): List<LatLng> {
            val allPoints = polygon.flatMap { it }
            return PolyUtil.simplify(allPoints, 500.0)
        }

        fun isPointCloseToPolygon(point: LatLng, polygon: List<LatLng>, tolerance: Double = 1000.0): Boolean {
            if (polygon.isEmpty()) return false
            val simplified = PolyUtil.simplify(polygon, tolerance)
            simplified.forEach {
                if (PolyUtil.isLocationOnEdge(it, polygon, true, tolerance)) {
                    return true
                }
            }
            return false
        }

        fun isPointWithinBounds(
            bounds: List<LatLng>, point: LatLng, geodesic: Boolean = true,
            tolerance: Double = 1000.0
        ): Boolean {
            return PolyUtil.containsLocation(point, bounds, geodesic)
            // || PolyUtil.isLocationOnEdge(point, bounds, geodesic, tolerance)
        }

        fun isPolygonWithinBounds(bounds: List<LatLng>, polygon: List<LatLng>, geodesic: Boolean = true): Boolean {
            if (polygon.isEmpty()) return false
            //val simplified = PolyUtil.simplify(polygon, 500.0)
            polygon.forEach {
                if (PolyUtil.containsLocation(it, bounds, geodesic)) {
                    return true
                }
            }
            return false
        }

        fun computeDistanceBetween(from: LatLng, to: LatLng): Double {
            return SphericalUtil.computeDistanceBetween(from, to)
        }

        fun isOfLatLngs(latLngs: List<LatLng>): LatLng {
            return createLatLngsBounds(latLngs = latLngs).center
        }

        fun centerOfLatLngs(latLngs: List<LatLng>): LatLng {
            return createLatLngsBounds(latLngs = latLngs).center
        }

        fun createLatLngsBounds(latLngs: List<LatLng>): LatLngBounds {
            val centerBuilder = LatLngBounds.builder()
            for (point in latLngs) {
                centerBuilder.include(point)
            }
            return centerBuilder.build()
        }

        fun decodePolygon(path: String): List<LatLng> {
            return PolyUtil.decode(path)
        }

        fun encodePolygon(list: List<LatLng>): String {
            return PolyUtil.encode(list)
        }

        fun decodePolygonTag(tag: String): Pair<String, String>? {
            //Split on DELIMITED
            val list = tag.split(POLYGON_DECODING_TAG_DELIMITER, ignoreCase = true, limit = 2)
            if (list.size != 2) {
                return null
            }
            //first: CITY_CODE  second: ENCODED_PATH
            return Pair(list[0], list[1])
        }

        fun encodePolygonTag(list: List<LatLng>, countryCode: String): String {
            val path = encodePolygon(list)
            //CITY_CODE DELIMITED ENCODED_PATH
            return "$countryCode$POLYGON_DECODING_TAG_DELIMITER$path"
        }
    }


}
