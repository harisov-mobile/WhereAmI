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
import com.google.android.material.snackbar.Snackbar
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
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
import javax.inject.Inject

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
        subscribeChildFragments()
    }

    override fun onResume() {
        super.onResume()
        // согласно документации https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)
        binding.mapview.onResume()
    }

    override fun onPause() {
        super.onPause()
        // согласно документации https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)
        binding.mapview.onPause()
    }

    private fun subscribeChildFragments() {
        // (диалоговое окно - "Открыть настройки?")
        childFragmentManager.setFragmentResultListener(REQUEST_OPEN_SETTINGS_KEY, viewLifecycleOwner, this)
    }

    private fun observeViewModel() {
        mapViewModel.mapStateLiveData.observe(viewLifecycleOwner) { mapState ->
            updateUI(mapState)
        }
    }

    private fun updateUI(mapState: MapState) {
        showMap(mapState)
    }

    private fun setupClickListeners() {
        binding.floatingActionButton.setOnClickListener {
            moveToCurrentLocation()
        }
    }

    override fun onFragmentResult(requestKey: String, result: Bundle) {
        // когда из диалогового окна прилетит ответ:
        when (requestKey) {
            // ответ на вопрос: "Открыть настройки?"
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

    private fun setMarker(geoPoint: GeoPoint, markerTitle: String) {
        val marker = Marker(binding.mapview).apply {
            position = geoPoint
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker)
            title = markerTitle
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            setOnMarkerClickListener { currentMarker, mapView ->
                // buildRoute(currentMarker)

                val snackBar = Snackbar.make(
                    binding.root,
                    markerTitle,
                    Snackbar.LENGTH_INDEFINITE
                )
                snackBar.setAction("OK") {
                    snackBar.dismiss()
                } // если не исчезает - вызови dismiss()
                snackBar.show()

                true
            }
        }
        // polylineMap.put(marker, null)

        binding.mapview.overlays.add(marker)
        binding.mapview.invalidate()
        mapViewModel.setMarker(marker)
    }

    private fun showMap(mapState: MapState) {
        initMap(mapState)
        setGestures()
        setCompas()
        setScale()
        setMapClickListener(mapState)
        showLocation(mapState)
        showMarker(mapState.marker)
    }

    private fun initMap(mapState: MapState) {
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidBasePath = requireActivity().filesDir

        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)
        binding.mapview.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapview.controller.setZoom(mapState.mapData.zoomLevel)
        binding.mapview.controller.setCenter(mapState.mapData.mapCenter)
    }

    private fun setGestures() {
        // приближение жестами: на эмуляторе зажать CTRL + зажать левую кнопку мыши
        // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)#how-to-enable-rotation-gestures
        binding.mapview.setMultiTouchControls(true)
        binding.mapview.overlays.add(RotationGestureOverlay(binding.mapview))
    }

    private fun setCompas() {
        // Компас
        val compassOverlay = CompassOverlay(context, InternalCompassOrientationProvider(context), binding.mapview)
        compassOverlay.enableCompass()
        binding.mapview.overlays.add(compassOverlay)
    }

    private fun setScale() {
        // Шкала
        val dm: DisplayMetrics = requireActivity().resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(binding.mapview)
        scaleBarOverlay.setCentred(true)
        // play around with these values to get the location on screen in the right place for your application
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
        binding.mapview.overlays.add(scaleBarOverlay)
    }

    private fun setMapClickListener(mapState: MapState) {
        // Нажатия на карту:
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                mapState.marker?.let { currentMarker ->
                    // если уже есть какой-то маркер, то я его удаляю:
                    binding.mapview.overlays.remove(currentMarker)
                }
                p?.let { geoPoint ->
                    val markerTitle = "МАРКЕР"
                    setMarker(geoPoint, markerTitle)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                // длинные нажатия не обрабатываю
                return true
            }
        }
        val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
        binding.mapview.overlays.add(mapEventsOverlay)
    }

    private fun showLocation(mapState: MapState) {
        if (mapState.isFirstTime) {
            mapViewModel.setIsFirstTime(false)
            locationPermissionRepository?.let { currentLocationPermissionRepository ->
                if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                    mapViewModel.setEnableFollowLocation(true)
                    mapViewModel.setShowLocationNotEnabled(true)
                    showCurrentLocation(
                        enableFollowLocation = true,
                        showLocationNotEnabled = true,
                        ZOOM_LEVEL
                    ) // в первый раз чтобы масштаб крупный был
                } else {
                    currentLocationPermissionRepository.requestLocationPermission {
                        // коллбек выполнится если пользователь даст разрешение
                        mapViewModel.setEnableFollowLocation(true)
                        mapViewModel.setEnableFollowLocation(true)
                        showCurrentLocation(
                            enableFollowLocation = true,
                            showLocationNotEnabled = true,
                            ZOOM_LEVEL
                        ) // в первый раз чтобы масштаб крупный был
                    }
                }
            }
        } else {
            locationPermissionRepository?.let { currentLocationPermissionRepository ->
                if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                    val enableFollowLocation = mapViewModel.mapStateLiveData.value?.enableFollowLocation ?: false
                    val showLocationNotEnabled = mapViewModel.mapStateLiveData.value?.showLocationNotEnabled ?: false
                    showCurrentLocation(
                        enableFollowLocation = enableFollowLocation,
                        showLocationNotEnabled = showLocationNotEnabled
                    )
                }
            }
        }
    }

    private fun showCurrentLocation(
        enableFollowLocation: Boolean,
        showLocationNotEnabled: Boolean,
        requiredZoom: Double? = null
    ) {
        locationPermissionRepository?.let { currentLocationPermissionRepository ->
            if (currentLocationPermissionRepository.isLocationEnabled()) {
                locationOverlay?.enableFollowLocation() ?: let {
                    // преобразование в bitmap:
                    val bitmapIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_man_yellow_with_border_red)!!
                            .toBitmap()

                    // получение геолокации пользователя:
                    // https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Kotlin)#how-to-add-the-my-location-overlay
                    locationOverlay =
                        MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.mapview).apply {
                            this.enableMyLocation()
                            this.setPersonIcon(bitmapIcon)
                            this.setDirectionIcon(bitmapIcon) // замена белой стрелки на человечка желтого с красной каемочкой

                            if (enableFollowLocation) {
                                this.enableFollowLocation()
                            }
                        }

                    requiredZoom?.let { zoom ->
                        binding.mapview.controller.setZoom(zoom)
                    }

                    binding.mapview.overlays.add(locationOverlay)
                }
            } else {
                locationOverlay?.let { currentLocationOverlay ->
                    binding.mapview.overlays.remove(currentLocationOverlay)
                    binding.mapview.invalidate()
                    locationOverlay = null
                }

                mapViewModel.setEnableFollowLocation(false)

                if (showLocationNotEnabled) {
                    val snackBar = Snackbar.make(
                        binding.root,
                        R.string.location_is_not_enabled,
                        Snackbar.LENGTH_INDEFINITE
                    )
                    snackBar.setAction("OK") {
                        snackBar.dismiss()
                    } // если не исчезает - вызови dismiss()
                    snackBar.show()
                    mapViewModel.setShowLocationNotEnabled(false)
                } else {
                    // Ничего не делаю
                }
            }
        }
    }

    private fun moveToCurrentLocation() {
        locationPermissionRepository?.let { currentLocationPermissionRepository ->
            if (currentLocationPermissionRepository.isLocationPermissionGranted()) {
                mapViewModel.setShowLocationNotEnabled(true)
                mapViewModel.setEnableFollowLocation(true)
                showCurrentLocation(enableFollowLocation = true, showLocationNotEnabled = true)
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

    private fun showMarker(currentMarker: Marker?) {
        currentMarker?.let {
            setMarker(it.position, it.title)
        }
    }
}
