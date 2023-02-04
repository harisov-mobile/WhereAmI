package ru.internetcloud.wereami.domain.usecase

import ru.internetcloud.wereami.domain.LocationPermissionRepository

class GetLocationPermissionUseCase(private val locationPermissionRepository: LocationPermissionRepository) {

    fun isLocationPermissionGranted(): Boolean {
        return locationPermissionRepository.isLocationPermissionGranted()
    }

    fun requestLocationPermission(callback: () -> Unit) {
        locationPermissionRepository.requestLocationPermission(callback)
    }
}
