package ru.internetcloud.wereami.domain.usecase

import javax.inject.Inject
import ru.internetcloud.wereami.domain.MapRepository
import ru.internetcloud.wereami.domain.model.MapData

class GetMapDataUseCase @Inject constructor(private val mapRepository: MapRepository) {

    fun getMapData(): MapData {
        return mapRepository.getMapData()
    }

}

