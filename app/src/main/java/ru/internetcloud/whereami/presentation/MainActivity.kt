package ru.internetcloud.whereami.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.internetcloud.whereami.R
import ru.internetcloud.whereami.databinding.ActivityMainBinding
import ru.internetcloud.whereami.domain.LocationPermissionRepository
import ru.internetcloud.whereami.presentation.map.MapFragment

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

        if (savedInstanceState == null) {
            setContentView(R.layout.splash_screen)

            scope.launch {
                delay(2000)
                setContentView(binding.root)
                val startFragment = MapFragment()
                showFragment(fragment = startFragment, withBackStack = false)
            }
        } else {
            setContentView(binding.root)
            // если во время заставки SplashScreen повернуть экран, то у активити устанавливаем макет activity_main.xml
            // и надо самому отобразить фрагмент,
            // а если поворот обычный, то Операционна Система Андроид сама будет
            // восстанавливать фрагмент после поворота, и мне ничего делать не надо:
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment == null) {
                val startFragment = MapFragment()
                showFragment(fragment = startFragment, withBackStack = false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun showFragment(fragment: Fragment, withBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()

        if (withBackStack) {
            transaction.replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            transaction.add(R.id.fragment_container, fragment)
                .commit()
        }
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
