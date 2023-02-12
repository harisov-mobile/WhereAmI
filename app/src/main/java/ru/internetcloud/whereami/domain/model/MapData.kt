package ru.internetcloud.whereami.domain.model

import org.osmdroid.util.GeoPoint

data class MapData(
    var mapCenter: GeoPoint,
    var zoomLevel: Double
)
