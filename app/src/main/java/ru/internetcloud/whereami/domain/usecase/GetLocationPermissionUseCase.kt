package ru.internetcloud.whereami.domain.usecase

import javax.inject.Inject
import ru.internetcloud.whereami.domain.LocationPermissionRepository

class GetLocationPermissionUseCase @Inject constructor(
    private val locationPermissionRepository: LocationPermissionRepository) {

    fun isLocationPermissionGranted(): Boolean {
        return locationPermissionRepository.isLocationPermissionGranted()
    }

    fun requestLocationPermission(callback: () -> Unit) {
        locationPermissionRepository.requestLocationPermission(callback)
    }
}
