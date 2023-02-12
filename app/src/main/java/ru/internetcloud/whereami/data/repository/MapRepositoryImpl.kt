package ru.internetcloud.whereami.data.repository

import android.app.Application
import ru.internetcloud.whereami.data.preference.MapPreferences
import ru.internetcloud.whereami.domain.MapRepository
import ru.internetcloud.whereami.domain.model.MapData
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val application: Application
) : MapRepository {

    override fun saveMapData(mapData: MapData) {
        MapPreferences.saveMapCenter(context = application, mapData.mapCenter)
        MapPreferences.saveZoomLevel(context = application, mapData.zoomLevel)
    }

    override fun getMapData(): MapData {
        val mapCenter = MapPreferences.getMapCenter(context = application)
        val zoomLevel = MapPreferences.getZoomLevel(context = application)
        return MapData(mapCenter = mapCenter, zoomLevel = zoomLevel)
    }
}
