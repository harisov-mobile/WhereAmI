package ru.internetcloud.whereami.presentation.map

import org.osmdroid.bonuspack.routing.OSRMRoadManager
import javax.inject.Inject

class TransportationSwitcher @Inject constructor() {

    fun getNextTransportationMode(transportationMode: String): String {
        return when (transportationMode) {
            OSRMRoadManager.MEAN_BY_FOOT -> OSRMRoadManager.MEAN_BY_BIKE
            OSRMRoadManager.MEAN_BY_BIKE -> OSRMRoadManager.MEAN_BY_CAR
            OSRMRoadManager.MEAN_BY_CAR -> OSRMRoadManager.MEAN_BY_FOOT
            else -> OSRMRoadManager.MEAN_BY_FOOT
        }
    }
}
