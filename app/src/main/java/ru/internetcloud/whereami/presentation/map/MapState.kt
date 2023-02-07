package ru.internetcloud.whereami.presentation.map

import ru.internetcloud.whereami.domain.model.MapData

data class MapState(
    var mapData: MapData,
    var isFirstTime: Boolean = false,
    var enableFollowLocation: Boolean = false
)
