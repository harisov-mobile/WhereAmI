package ru.internetcloud.whereami.presentation.map

import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import ru.internetcloud.whereami.domain.model.MapData

data class MapState(
    var mapData: MapData,
    var isFirstTime: Boolean = false,
    var enableFollowLocation: Boolean = false,
    var marker: Marker? = null,
    var routeStartPoint: GeoPoint? = null,
    var polyline: Polyline? = null,
    var showLocationNotEnabled: Boolean = true,
    var transportationMode: String = OSRMRoadManager.MEAN_BY_FOOT
)
