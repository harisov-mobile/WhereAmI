package ru.internetcloud.whereami.domain.usecase

import javax.inject.Inject
import ru.internetcloud.whereami.domain.MapRepository
import ru.internetcloud.whereami.domain.model.MapData

class SaveMapDataUseCase @Inject constructor(private val mapRepository: MapRepository) {

    fun saveMapData(mapData: MapData) {
        mapRepository.saveMapData(mapData)
    }

}
