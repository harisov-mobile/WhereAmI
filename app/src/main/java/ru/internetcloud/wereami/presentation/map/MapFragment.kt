package ru.internetcloud.wereami.presentation.map

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.internetcloud.wereami.App
import ru.internetcloud.wereami.BuildConfig
import ru.internetcloud.wereami.R
import ru.internetcloud.wereami.databinding.FragmentMapBinding
import ru.internetcloud.wereami.di.ApplicationComponent
import ru.internetcloud.wereami.di.ViewModelFactory
import ru.internetcloud.wereami.domain.LocationPermissionRepository
import ru.internetcloud.wereami.domain.model.MapData

class MapFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val mapViewModel: MapViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)
    }

    private val component: ApplicationComponent by lazy {
        (requireActivity().application as App).component
    }

    private var _binding: FragmentMapBinding? = null
    private val binding: FragmentMapBinding
        get() = _binding!!

    private var locationPermissionRepository: LocationPermissionRepository? = null

    private lateinit var locationOverlay: MyLocationNewOverlay

    companion object {
        private const val ZOOM_LEVEL = 17.8
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        locationPermissionRepository = context as LocationPermissionRepository
    }

    override fun onDetach() {
        super.onDetach()
        locationPermissionRepository = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        Log.i("rustam", "onDestroyView")

        val currentMapCenter = binding.mapview.getMapCenter()

        // в конструкторе GeoPoint надо наоборот передавать параметры:
        val geoPoint = GeoPoint(currentMapCenter.latitude, currentMapCenter.longitude)
        mapViewModel.setMapCenter(geoPoint)
        mapViewModel.setZoomLevel(binding.mapview.zoomLevelDouble)
        _binding = null

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        mapViewModel.currentMapData.observe(viewLifecycleOwner) { mapData ->
            updateUI(mapData)
        }
    }

    private fun updateUI(mapData: MapData) {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireActivity().filesDir

        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.controller.setZoom(mapData.zoomLevel)
        binding.mapview.controller.setCenter(mapData.mapCenter)

        // приближение жестами: на эмуляторе зажать CTRL + зажать левую кнопку мыши
        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)#how-to-enable-rotation-gestures
        binding.mapview.setMultiTouchControls(true)
        binding.mapview.overlays.add(RotationGestureOverlay(binding.mapview))

        locationPermissionRepository?.let { currentLocationPermissionRepository ->
            if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                showMyLocation()
            } else {
                if (mapData.needToAsk) {
                    mapViewModel.setNeedToAsk(false)
                    currentLocationPermissionRepository.requestLocationPermission {
                        showMyLocation()
                    }
                }
            }
        }
    }

    private fun showMyLocation() {
        // преобразование в bitmap:
        val bitmapIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_man_yellow_with_border_red)!!.toBitmap()

        // получение геолокации пользователя:
        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)#how-to-add-the-my-location-overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.mapview)
        locationOverlay.enableMyLocation()
        locationOverlay.setPersonIcon(bitmapIcon)
        locationOverlay.setDirectionIcon(bitmapIcon) // замена белой стрелки
        locationOverlay.enableFollowLocation()
        if (binding.mapview.zoomLevelDouble < ZOOM_LEVEL) {
            binding.mapview.controller.setZoom(ZOOM_LEVEL)
        }
        binding.mapview.overlays.add(locationOverlay)
    }

    private fun setupClickListeners() {
        binding.floatingActionButton.setOnClickListener {
            showCurrentLocation()
        }
    }

    private fun showCurrentLocation() {
        locationPermissionRepository?.let { currentLocationPermissionRepository ->
            if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                locationOverlay.enableFollowLocation()
            } else {
                mapViewModel.currentMapData.value?.let { mapData ->
                    if (mapData.needToAsk) {
                        mapViewModel.setNeedToAsk(false)
                        currentLocationPermissionRepository.requestLocationPermission {
                            showMyLocation()
                        }
                    }
                }
            }
        }

    }

    override fun onStop() {
        super.onStop()
        Log.i("rustam", "onStop")
    }
}
