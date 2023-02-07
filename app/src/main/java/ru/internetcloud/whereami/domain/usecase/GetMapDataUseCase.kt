package ru.internetcloud.whereami.domain.usecase

import javax.inject.Inject
import ru.internetcloud.whereami.domain.MapRepository
import ru.internetcloud.whereami.domain.model.MapData

class GetMapDataUseCase @Inject constructor(private val mapRepository: MapRepository) {

    fun getMapData(): MapData {
        return mapRepository.getMapData()
    }

}

