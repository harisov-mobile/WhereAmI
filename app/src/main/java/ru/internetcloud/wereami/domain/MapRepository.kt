package ru.internetcloud.wereami.domain

import ru.internetcloud.wereami.domain.model.MapData

interface MapRepository {

    fun saveMapData(mapData: MapData)

    fun getMapData(): MapData
}
