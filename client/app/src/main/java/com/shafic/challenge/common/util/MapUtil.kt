package com.shafic.challenge.common.util

import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil
import java.util.*

class MapUtil {
    companion object {
        private const val GAPI_ERROR_DIALOG_RESULT = 12345
        private const val COLOR_BLACK_ARGB = -0x1000000
        private const val COLOR_WHITE_ARGB = -0x1
        private const val COLOR_GREEN_ARGB = -0xc771c4
        private const val COLOR_PURPLE_ARGB = -0x7e387c
        private const val COLOR_ORANGE_ARGB = -0xa80e9
        private const val COLOR_BLUE_ARGB = -0x657db

        private const val POLYGON_STROKE_WIDTH_PX = 8.0f
        private const val PATTERN_DASH_LENGTH_PX = 20
        private const val PATTERN_GAP_LENGTH_PX = 20
        private val DOT = Dot()
        private val DASH = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
        private val GAP = Gap(PATTERN_GAP_LENGTH_PX.toFloat())
        // Create a stroke pattern of a gap followed by a dash.
        private val PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH)

        // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
        private val PATTERN_POLYGON_BETA = Arrays.asList(DOT, GAP, DASH, GAP)

        fun stylePolygon(polygon: Polygon) {
            var type = ""
            // Get the data object stored with the polygon.
            if (polygon.tag != null) {
                type = polygon.tag.toString()
            }

            var pattern: List<PatternItem>? = null
            var strokeColor = COLOR_BLACK_ARGB
            var fillColor = COLOR_WHITE_ARGB

            pattern = PATTERN_POLYGON_ALPHA
            strokeColor = COLOR_GREEN_ARGB
            fillColor = COLOR_PURPLE_ARGB

            polygon.strokePattern = pattern
            polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX
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

        fun isPointWithinBounds(bounds: List<LatLng>, point: LatLng, geodesic: Boolean = true, 
                                tolerance: Double = 1000.0): Boolean {
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
    }


}
