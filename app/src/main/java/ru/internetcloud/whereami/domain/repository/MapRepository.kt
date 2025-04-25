package ru.internetcloud.whereami.domain

import ru.internetcloud.whereami.domain.model.MapData

interface MapRepository {

    fun saveMapData(mapData: MapData)

    fun getMapData(): MapData
}
