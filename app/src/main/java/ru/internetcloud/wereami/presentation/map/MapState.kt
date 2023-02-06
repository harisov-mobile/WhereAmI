package ru.internetcloud.wereami.presentation.map

import ru.internetcloud.wereami.domain.model.MapData

data class MapState(
    var mapData: MapData,
    var isFirstTime: Boolean = false,
    var enableFollowLocation: Boolean = false
)
