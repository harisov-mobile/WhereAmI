package ru.internetcloud.whereami.presentation.map

import org.osmdroid.views.overlay.Marker
import ru.internetcloud.whereami.domain.model.MapData

data class MapState(
    var mapData: MapData,
    var isFirstTime: Boolean = false,
    var enableFollowLocation: Boolean = false,
    var marker: Marker? = null,
    var showLocationNotEnabled: Boolean = true
)
