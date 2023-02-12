package ru.internetcloud.whereami.domain.usecase

import ru.internetcloud.whereami.domain.MapRepository
import ru.internetcloud.whereami.domain.model.MapData
import javax.inject.Inject

class SaveMapDataUseCase @Inject constructor(private val mapRepository: MapRepository) {

    fun saveMapData(mapData: MapData) {
        mapRepository.saveMapData(mapData)
    }
}
