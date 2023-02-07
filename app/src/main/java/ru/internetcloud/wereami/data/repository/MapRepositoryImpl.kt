package ru.internetcloud.wereami.data.repository

import android.app.Application
import javax.inject.Inject
import ru.internetcloud.wereami.data.preference.MapPreferences
import ru.internetcloud.wereami.domain.MapRepository
import ru.internetcloud.wereami.domain.model.MapData

class MapRepositoryImpl @Inject constructor(
    private val application: Application
): MapRepository {

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
