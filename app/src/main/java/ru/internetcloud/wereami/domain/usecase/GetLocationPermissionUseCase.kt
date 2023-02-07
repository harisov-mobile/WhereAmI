package ru.internetcloud.wereami.domain.usecase

import javax.inject.Inject
import ru.internetcloud.wereami.domain.LocationPermissionRepository

class GetLocationPermissionUseCase @Inject constructor(
    private val locationPermissionRepository: LocationPermissionRepository) {

    fun isLocationPermissionGranted(): Boolean {
        return locationPermissionRepository.isLocationPermissionGranted()
    }

    fun requestLocationPermission(callback: () -> Unit) {
        locationPermissionRepository.requestLocationPermission(callback)
    }
}
