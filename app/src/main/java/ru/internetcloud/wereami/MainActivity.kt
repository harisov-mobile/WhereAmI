package ru.internetcloud.wereami

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.internetcloud.wereami.databinding.ActivityMainBinding
import ru.internetcloud.wereami.domain.LocationPermissionRepository

class MainActivity : AppCompatActivity(), LocationPermissionRepository {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val scope = CoroutineScope(Dispatchers.Main)

    private lateinit var grantedCallbackForLocationPermission: () -> Unit

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 34
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        scope.launch {
            delay(5000)
            setContentView(binding.root)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun isLocationPermissionGranted(): Boolean {
        val grant = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        return PackageManager.PERMISSION_GRANTED == grant
    }

    override fun requestLocationPermission(callback: () -> Unit) {
        grantedCallbackForLocationPermission = callback
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSIONS_REQUEST_CODE
        ) // будет вызван коллбек = onRequestPermissionsResult
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.d("rustam", "onRequestPermissionResult")

        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                when {
                    grantResults.isEmpty() -> {
                    }
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        // Ура! Разрешение дано!
                        grantedCallbackForLocationPermission.invoke() // выполняем коллбек
                    }
                    else -> {
                        // Permission denied.
                    }
                }
            }
        }
    }

}


