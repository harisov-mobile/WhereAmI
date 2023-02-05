package ru.internetcloud.wereami.domain

interface LocationPermissionRepository {

    fun isLocationPermissionGranted(): Boolean

    fun requestLocationPermission(callback: () -> Unit)
}
