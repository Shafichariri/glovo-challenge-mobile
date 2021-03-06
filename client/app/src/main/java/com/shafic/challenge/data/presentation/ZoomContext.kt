package com.shafic.challenge.data.presentation

enum class ZoomContext {
    None, // Invalid Zoom Option
    PolygonsFriendly,
    MarkersFriendly;

    private val worldRadius = 156543.03392

    fun metersPerPixel(zoom: Double, lat: Double): Double {
        return worldRadius * Math.cos(lat * Math.PI / 180) / Math.pow(2.0, zoom)
    }

    companion object {
        fun create(zoom: Float): ZoomContext {
            return when (zoom) {
                in 0.0..8.0 -> {
                    ZoomContext.MarkersFriendly
                }
                in 8.0..20.0 -> {
                    ZoomContext.PolygonsFriendly
                }
                else -> {
                    ZoomContext.None
                }
            }
        }
        
        fun shouldCheckInverse(zoom: Float): Boolean {
            return (zoom > 12.0)
        }

        fun isAreaVisibleEnough(zoom: Float): Boolean {
            return (zoom > 11.0)
        }
    }
}
