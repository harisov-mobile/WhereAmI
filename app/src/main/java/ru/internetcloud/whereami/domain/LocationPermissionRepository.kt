package ru.internetcloud.whereami.domain

interface LocationPermissionRepository {

    fun isLocationPermissionGranted(): Boolean

    fun requestLocationPermission(callback: () -> Unit)

    fun isLocationEnabled(): Boolean
}
