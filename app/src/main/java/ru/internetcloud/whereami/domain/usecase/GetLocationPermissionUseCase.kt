package ru.internetcloud.whereami.domain.usecase

import ru.internetcloud.whereami.domain.LocationPermissionRepository
import javax.inject.Inject

class GetLocationPermissionUseCase @Inject constructor(
    private val locationPermissionRepository: LocationPermissionRepository
) {

    fun isLocationPermissionGranted(): Boolean {
        return locationPermissionRepository.isLocationPermissionGranted()
    }

    fun requestLocationPermission(callback: () -> Unit) {
        locationPermissionRepository.requestLocationPermission(callback)
    }
}
