package ru.internetcloud.wereami.domain.model

import org.osmdroid.util.GeoPoint

data class MapData(
    var mapCenter: GeoPoint,
    var zoomLevel: Double,
    var needToAsk: Boolean = false
)

