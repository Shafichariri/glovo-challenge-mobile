package com.shafic.challenge.data.presentation

sealed class MapDataPresentation {
    class Polygons(val value: List<SimpleCity>) : MapDataPresentation()
    class ClusteredMarkers(val value: List<SimpleCity>) : MapDataPresentation()
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
                ZoomContext.MarkersClusterFriendly -> {
                    MapDataPresentation.ClusteredMarkers(value = cityMarkers)
                }

                ZoomContext.MarkersFriendly -> {
                    MapDataPresentation.Markers(value = cityMarkers)
                }

                ZoomContext.PolygonsClusterFriendly -> {
                    MapDataPresentation.Polygons(value = cityMarkers)
                }

                ZoomContext.PolygonsClusterFriendly -> {
                    MapDataPresentation.Polygons(value = cityMarkers)
                }
                else -> {
                    null
                }
            }
        }
    }
}
