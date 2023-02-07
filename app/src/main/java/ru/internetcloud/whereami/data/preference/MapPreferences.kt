package ru.internetcloud.whereami.data.preference

import android.content.Context
import android.preference.PreferenceManager
import org.osmdroid.util.GeoPoint

object MapPreferences {

    private const val MAP_CENTER_LONGITUDE = "map_center_longitude"
    private const val MAP_CENTER_LATITUDE = "map_center_latitude"
    private const val ZOOM_LEVEL = "zoom_level"

    fun saveMapCenter(context: Context, geoPoint: GeoPoint) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(MAP_CENTER_LONGITUDE, geoPoint.longitude.toString())
            .apply()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(MAP_CENTER_LATITUDE, geoPoint.latitude.toString())
            .apply()
    }

    fun getMapCenter(context: Context): GeoPoint {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val longitude = prefs.getString(MAP_CENTER_LONGITUDE, "0")!!.toDouble()
        val latitude = prefs.getString(MAP_CENTER_LATITUDE, "0")!!.toDouble()

        return GeoPoint(latitude, longitude)
    }

    fun saveZoomLevel(context: Context, zoomLevel: Double) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(ZOOM_LEVEL, zoomLevel.toString())
            .apply()
    }

    fun getZoomLevel(context: Context): Double {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val zoomLevel = prefs.getString(ZOOM_LEVEL, "2.2")!!.toDouble() // 2.2 - чтобы вся карта мира вошла
        return zoomLevel
    }
}
