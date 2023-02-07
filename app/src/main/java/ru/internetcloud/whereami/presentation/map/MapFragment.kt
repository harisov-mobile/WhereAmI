package ru.internetcloud.whereami.presentation.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import ru.internetcloud.whereami.App
import ru.internetcloud.whereami.BuildConfig
import ru.internetcloud.whereami.R
import ru.internetcloud.whereami.databinding.FragmentMapBinding
import ru.internetcloud.whereami.di.ApplicationComponent
import ru.internetcloud.whereami.di.ViewModelFactory
import ru.internetcloud.whereami.domain.LocationPermissionRepository
import ru.internetcloud.whereami.presentation.dialog.QuestionDialogFragment


class MapFragment : Fragment(), FragmentResultListener {

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

    private var locationOverlay: MyLocationNewOverlay? = null

    companion object {
        private const val ZOOM_LEVEL = 17.8

        private val REQUEST_OPEN_SETTINGS_KEY = "open_settings_key"
        private val ARG_ANSWER = "arg_answer"
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

        val currentMapCenter = binding.mapview.getMapCenter()

        // в конструкторе GeoPoint надо наоборот передавать параметры:
        val geoPoint = GeoPoint(currentMapCenter.latitude, currentMapCenter.longitude)
        mapViewModel.setMapCenter(geoPoint)
        mapViewModel.setZoomLevel(binding.mapview.zoomLevelDouble)

        locationOverlay?.let { currentLocationOverlay ->
            mapViewModel.setEnableFollowLocation(currentLocationOverlay.isFollowLocationEnabled)
        }
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        subscribeChilds()
    }

    override fun onResume() {
        super.onResume()
        binding.mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapview.onPause()
    }

    private fun subscribeChilds() {
        // (диалоговое окно - "Открыть настройки?")
        childFragmentManager.setFragmentResultListener(REQUEST_OPEN_SETTINGS_KEY, viewLifecycleOwner, this)
    }

    private fun observeViewModel() {
        mapViewModel.mapStateLiveData.observe(viewLifecycleOwner) { mapState ->
            updateUI(mapState)
        }
    }

    private fun updateUI(mapState: MapState) {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireActivity().filesDir

        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.controller.setZoom(mapState.mapData.zoomLevel)
        binding.mapview.controller.setCenter(mapState.mapData.mapCenter)

        // приближение жестами: на эмуляторе зажать CTRL + зажать левую кнопку мыши
        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)#how-to-enable-rotation-gestures
        binding.mapview.setMultiTouchControls(true)
        binding.mapview.overlays.add(RotationGestureOverlay(binding.mapview))

        // Компас
        val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), binding.mapview)
        compassOverlay.enableCompass()
        binding.mapview.overlays.add(compassOverlay)

        // Шкала
        val dm : DisplayMetrics = requireActivity().resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(binding.mapview)
        scaleBarOverlay.setCentred(true)
        //play around with these values to get the location on screen in the right place for your application
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        binding.mapview.overlays.add(scaleBarOverlay)

        if (mapState.isFirstTime) {
            mapViewModel.setIsFirstTime(false)
            locationPermissionRepository?.let { currentLocationPermissionRepository ->
                if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                    mapViewModel.setEnableFollowLocation(true)
                    showMyLocation(enableFollowLocation = true, ZOOM_LEVEL) // в первый раз чтобы масштаб крупный был
                } else {
                    currentLocationPermissionRepository.requestLocationPermission {
                        // коллбек выполнится если пользователь даст разрешение
                        mapViewModel.setEnableFollowLocation(true)
                        showMyLocation(enableFollowLocation = true, ZOOM_LEVEL)
                    }
                }
            }
        } else {
            locationPermissionRepository?.let { currentLocationPermissionRepository ->
                if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                    val enableFollowLocation = mapViewModel.mapStateLiveData.value?.enableFollowLocation ?: false
                    showMyLocation(enableFollowLocation)
                }
            }
        }
    }

    private fun showMyLocation(enableFollowLocation: Boolean, requiredZoom: Double? = null) {
        // преобразование в bitmap:
        val bitmapIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_man_yellow_with_border_red)!!.toBitmap()

        // получение геолокации пользователя:
        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)#how-to-add-the-my-location-overlay
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.mapview).apply {
            this.enableMyLocation()
            this.setPersonIcon(bitmapIcon)
            this.setDirectionIcon(bitmapIcon) // замена белой стрелки

            if (enableFollowLocation) {
                this.enableFollowLocation()
            }
        }

        requiredZoom?.let { zoom ->
            binding.mapview.controller.setZoom(zoom)
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
                mapViewModel.setEnableFollowLocation(true)
                locationOverlay?.let {
                    it.enableFollowLocation()
                } ?: let {
                    showMyLocation(enableFollowLocation = true)
                }
            } else {
                // открываю диалог с предложением открыть настройки приложения
                QuestionDialogFragment
                    .newInstance(
                        getString(R.string.offer_to_open_settings),
                        REQUEST_OPEN_SETTINGS_KEY,
                        ARG_ANSWER
                    )
                    .show(childFragmentManager, REQUEST_OPEN_SETTINGS_KEY)
            }
        }

    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        // когда из диалогового окна прилетит ответ:
        when (requestKey) {
            // ответ на вопрос: "Записать данные?"
            REQUEST_OPEN_SETTINGS_KEY -> {
                val openSettings: Boolean = result.getBoolean(ARG_ANSWER, false)
                if (openSettings) {
                    // запустить экран, который приведет пользователя в настройки:
                    val settingsIntent = Intent()
                    settingsIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val myApplicaionUri = Uri.fromParts(
                        "package",
                        BuildConfig.APPLICATION_ID,
                        null
                    )
                    settingsIntent.data = myApplicaionUri
                    settingsIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(settingsIntent)
                }
            }

        }
    }

}
