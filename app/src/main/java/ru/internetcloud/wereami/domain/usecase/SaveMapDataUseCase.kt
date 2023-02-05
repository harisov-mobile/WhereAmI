package ru.internetcloud.wereami.domain.usecase

import javax.inject.Inject
import ru.internetcloud.wereami.domain.MapRepository
import ru.internetcloud.wereami.domain.model.MapData

class SaveMapDataUseCase @Inject constructor(private val mapRepository: MapRepository) {

    fun saveMapData(mapData: MapData) {
        mapRepository.saveMapData(mapData)
    }

}
