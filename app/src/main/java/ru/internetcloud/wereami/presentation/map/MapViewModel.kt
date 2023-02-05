package ru.internetcloud.wereami.presentation.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject
import org.osmdroid.util.GeoPoint
import ru.internetcloud.wereami.data.repository.MapRepositoryImpl
import ru.internetcloud.wereami.domain.MapRepository
import ru.internetcloud.wereami.domain.model.MapData
import ru.internetcloud.wereami.domain.usecase.GetLocationPermissionUseCase
import ru.internetcloud.wereami.domain.usecase.GetMapDataUseCase
import ru.internetcloud.wereami.domain.usecase.SaveMapDataUseCase

class MapViewModel @Inject constructor(
    getMapDataUseCase: GetMapDataUseCase,
    private val saveMapDataUseCase: SaveMapDataUseCase
) : ViewModel() {

    private val _currentMapData = MutableLiveData<MapData>()
    val currentMapData: LiveData<MapData>
        get() = _currentMapData

    init {
        var mapData = getMapDataUseCase.getMapData()
        mapData.needToAsk = true
        _currentMapData.value = mapData
    }

    fun setMapCenter(geoPoint: GeoPoint) {
       _currentMapData.value?.mapCenter = geoPoint
    }

    fun setZoomLevel(zoomLevel: Double) {
        _currentMapData.value?.zoomLevel = zoomLevel
    }

    fun setNeedToAsk(needToAsk: Boolean) {
        _currentMapData.value?.needToAsk = needToAsk
    }

    override fun onCleared() {
        super.onCleared()
        _currentMapData.value?.let { mapData ->
            saveMapDataUseCase.saveMapData(mapData)
        }
    }

}

