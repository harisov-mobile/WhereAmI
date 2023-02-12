package ru.internetcloud.whereami.presentation.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import ru.internetcloud.whereami.domain.usecase.GetMapDataUseCase
import ru.internetcloud.whereami.domain.usecase.SaveMapDataUseCase
import javax.inject.Inject

class MapViewModel @Inject constructor(
    getMapDataUseCase: GetMapDataUseCase,
    private val saveMapDataUseCase: SaveMapDataUseCase
) : ViewModel() {

    private val _mapStateLiveData = MutableLiveData<MapState>()
    val mapStateLiveData: LiveData<MapState>
        get() = _mapStateLiveData

    init {
        val mapData = getMapDataUseCase.getMapData()
        _mapStateLiveData.value = MapState(mapData = mapData, isFirstTime = true, enableFollowLocation = false)
    }

    fun setMapCenter(geoPoint: GeoPoint) {
        _mapStateLiveData.value?.mapData?.mapCenter = geoPoint
    }

    fun setZoomLevel(zoomLevel: Double) {
        _mapStateLiveData.value?.mapData?.zoomLevel = zoomLevel
    }

    fun setIsFirstTime(value: Boolean) {
        _mapStateLiveData.value?.isFirstTime = value
    }

    fun setEnableFollowLocation(value: Boolean) {
        _mapStateLiveData.value?.enableFollowLocation = value
    }

    fun setShowLocationNotEnabled(value: Boolean) {
        _mapStateLiveData.value?.showLocationNotEnabled = value
    }

    fun setMarker(marker: Marker) {
        _mapStateLiveData.value?.marker = marker
    }

    override fun onCleared() {
        super.onCleared()
        _mapStateLiveData.value?.let { mapState ->
            saveMapDataUseCase.saveMapData(mapState.mapData)
        }
    }
}
