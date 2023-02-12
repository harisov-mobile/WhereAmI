package ru.internetcloud.whereami.domain.usecase

import ru.internetcloud.whereami.domain.MapRepository
import ru.internetcloud.whereami.domain.model.MapData
import javax.inject.Inject

class GetMapDataUseCase @Inject constructor(private val mapRepository: MapRepository) {

    fun getMapData(): MapData {
        return mapRepository.getMapData()
    }
}
