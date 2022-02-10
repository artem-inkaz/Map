package ui.smartpro.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import ui.smartpro.map.R
import ui.smartpro.map.base.BaseFragment
import ui.smartpro.map.data.model.Markers
import ui.smartpro.map.databinding.FragmentMapsBinding
import ui.smartpro.map.utils.DataConstants.Companion.markerList
import java.io.IOException
import java.util.*

class MapsFragment(override val layoutId: Int = R.layout.fragment_maps) :
    BaseFragment<FragmentMapsBinding>(), GoogleMap.OnMarkerClickListener {

    companion object {
        var PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        const val TAG = "MapsFragment"
        fun newInstance() = MapsFragment()
    }

    private lateinit var thisMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    lateinit var job: Job
    private lateinit var client: FusedLocationProviderClient
    private var currentMarkerLocation: LatLng? = null
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var radiusGeofence:Int? = 20
    private var map: GoogleMap? = null
//    val options: MarkerOptions = MarkerOptions()
    private val vm by viewModel<MarkerViewModel>()

    private val callback = OnMapReadyCallback { googleMap ->
        thisMap = googleMap
        activateMyLocation(thisMap)
    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                getMyCurrentLocation()
                getMapData()
            } else {
                checkGPSPermission()
            }
        }

    override fun initViews() {
        super.initViews()
        client = activity?.let { it1 -> LocationServices.getFusedLocationProviderClient(it1) }!!
        checkGPSPermission() // Запрашиваем все разрешения
        vm.getListMarker()
    }

    private fun activateMyLocation(googleMap: GoogleMap) {
        context?.let {
            val isPermissionGranted =
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                        PackageManager.PERMISSION_GRANTED

            googleMap.isMyLocationEnabled = isPermissionGranted
            googleMap.uiSettings.isMyLocationButtonEnabled = isPermissionGranted

        }
    }

    private fun checkGPSPermission() {
        context?.let {
            when {
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED -> {

                    getMyCurrentLocation()
                    getMapData()
                }
                // Метод для нас, чтобы знали когда необходимы пояснения показывать перед запросом:
                shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                    AlertDialog.Builder(it)
                        .setTitle("Необходим доступ к GPS")
                        .setMessage(
                            "Внимание! Для просмотра данных на карте необходимо разрешение на" +
                                    "использование Вашего местоположения"
                        )
                        .setPositiveButton("Предоставить доступ") { _, _ ->
                            permReqLauncher.launch(PERMISSIONS)
                        }
                        .setNegativeButton("Спасибо, не надо") { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
                }
                else -> {
                    permReqLauncher.launch(PERMISSIONS)
                }
            }
        }
    }

    private fun getMapData() {
        // находим нужный нам фрагмент, приводим его к типу SupportMapFragment и подготавливаем карту
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        addMarkerToMap(mapFragment)
        setMarkers(mapFragment)
    }

    private fun setMarkers(mapFragment: SupportMapFragment?){
        mapFragment?.getMapAsync { googleMap ->

            var i = 0
            doInScopeResume {
                vm.setMarkerList.collect { setMarkerList ->
                    if (setMarkerList?.size != 0 && setMarkerList != null) {
                        for (marker in setMarkerList) {
                            i += 1
                            val markerOptions = MarkerOptions()
                            val googleMarker: Markers = marker
                            val placeName = googleMarker.adress
                            currentMarkerLocation = googleMarker.latitude?.let { it1 ->
                                googleMarker.longitude?.let { it2 ->
                                    LatLng(
                                        it1,
                                        it2
                                    )
                                }
                            }
                                currentMarkerLocation?.let { markerLocation ->
                                    markerOptions
                                        .position(markerLocation)
                                        .draggable(true)
                                        .snippet("${marker.id}")
                                        .title("$placeName")
                                        .snippet("$currentMarkerLocation")
                                    markerOptions.icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_RED
                                        )
                                    )
                                    // добавляем радиус к маркеру
                                    circle = radiusGeofence?.let { radius->
                                        CircleOptions()
                                            .center(currentMarkerLocation!!)
                                            .radius(radius.toDouble())
                                            .strokeColor(
                                                ContextCompat.getColor(
                                                    context as Activity,
                                                    R.color.blueDark
                                                )
                                            )
                                            .zIndex(2f)
                                            .fillColor(ContextCompat.getColor(context as Activity, R.color.blue))
                                    }?.let { it1 ->
                                        googleMap.addCircle(
                                            it1
                                        )
                                    }
                                    this@MapsFragment.marker = thisMap.addMarker(markerOptions)
//                                    this@MapsFragment.marker?.showInfoWindow()
                                    val pos = CameraPosition.fromLatLngZoom(markerLocation, 17f)
                                    thisMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
                                    thisMap.moveCamera(
                                        CameraUpdateFactory.newLatLng(
                                            markerLocation
                                        )
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    private fun addMarkerToMap(mapFragment: SupportMapFragment?){
        mapFragment?.getMapAsync { googleMap ->

            googleMap.setOnMapClickListener {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                currentMarkerLocation = currentLatLng
              getAddressAsync(context as Activity, currentLatLng)
                // добавляем радиус к маркеру
                circle = radiusGeofence?.let { radius->
                    CircleOptions()
                        .center(currentLatLng)
                        .radius(radius.toDouble())
                        .strokeColor(
                            ContextCompat.getColor(
                                context as Activity,
                                R.color.blueDark
                            )
                        )
                        .zIndex(2f)
                        .fillColor(ContextCompat.getColor(context as Activity, R.color.blue))
                }?.let { it1 ->
                    googleMap.addCircle(
                        it1
                    )
                }
                googleMap.snapshot { }

                doInScopeResume {
                    vm.stateInitial.collect { stateInitial ->
                        if (stateInitial) {
                            // добавляем на карту маркер геозоны
                            marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(currentLatLng)
                                    .draggable(true)
                                    .title("Я тут")
                                    .snippet("$currentLatLng")
                                    .snippet("${getAddressToWindow(context as Activity, currentLatLng)}")
                                    .icon(BitmapDescriptorFactory.defaultMarker())
                            )
                            marker?.showInfoWindow()
                        }
                    }
                }
            }
        }
    }

    private fun getAddressAsync(
        context: Context,
        location: LatLng
    ) {
        val geoCoder = Geocoder(context)
        try {
            val addresses = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            val describeAdress=addresses[0].thoroughfare+", "+ addresses[0].featureName
            vm.addMarkerToMap(location, describeAdress)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getAddressToWindow(
        context: Context,
        location: LatLng
    ) : String {
        val geoCoder = Geocoder(context)
        try {
            val addresses = geoCoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            val describeAdress=addresses[0].thoroughfare+", "+ addresses[0].featureName
            return describeAdress
        } catch (e: IOException) {
            e.printStackTrace()
            return "Не известно"
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyCurrentLocation() {
        val task: Task<Location> = client.lastLocation
        task.addOnSuccessListener { location ->
            if (location !== null) {
                val mapFragment =
                    childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment?.getMapAsync { googleMap ->
                    saveMyLocation(location)
                    googleMap.uiSettings.isScrollGesturesEnabled = true
                    googleMap.uiSettings.setAllGesturesEnabled(true)
                    googleMap.uiSettings.isMapToolbarEnabled = true
                    googleMap.uiSettings.isZoomControlsEnabled = true
                    googleMap.uiSettings.isCompassEnabled = true
                    googleMap.uiSettings.isRotateGesturesEnabled = true
                    googleMap.uiSettings.isZoomGesturesEnabled = true
                }
            }
        }
    }

    private fun saveMyLocation(location: Location) {
        val loc = LatLng(
            location.latitude,
            location.longitude
        )
        val geoCoder = Geocoder(context, Locale.getDefault())
        val myPlaceByLocation: List<Address> =
            geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        val myAddress = myPlaceByLocation[0].thoroughfare+", "+ myPlaceByLocation[0].featureName
        vm.addMarkerToMap(loc, myAddress)
        val options: MarkerOptions = MarkerOptions()
            .position(loc)
            .draggable(true)
//            .snippet("$it")
            .title("Я Тут!")
            .snippet("$loc")
            .snippet("$myAddress")
            .icon(BitmapDescriptorFactory.defaultMarker())

        doInScopeResume {
            vm.stateInitial.collect { stateInitial ->
                if (stateInitial) {
                    thisMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17f))
                    marker = thisMap.addMarker(options)
                    marker?.showInfoWindow()
                }
            }
        }
    }

//    private fun dragMarker(id:Long, thisMap: GoogleMap){
//
//        thisMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
//            override fun onMarkerDragStart(arg0: Marker) {
//                circle?.remove()
//            }
//
//            override fun onMarkerDragEnd(arg0: Marker) {
//                val message = arg0.position.latitude.toString() + "" + arg0.position.longitude.toString()
//                currentMarkerLocation = LatLng(arg0.position.latitude,arg0.position.longitude)
//                circle = radiusGeofence?.let { radius->
//                    CircleOptions()
//                        .center(currentMarkerLocation!!)
//                        .radius(radius.toDouble())
//                        .strokeColor(
//                            ContextCompat.getColor(
//                                context as Activity,
//                                R.color.salat
//                            )
//                        )
//                        .zIndex(2f)
//                        .fillColor(ContextCompat.getColor(context as Activity, R.color.blue))
//                }?.let { it1 ->
//                    thisMap.addCircle(
//                        it1
//                    )
//                }
//                val geoCoder = Geocoder(context, Locale.getDefault())
//                val myPlaceByLocation: List<Address> =
//                    geoCoder.getFromLocation(arg0.position.latitude, arg0.position.longitude, 1)
//                val myNewAddress = myPlaceByLocation[0].thoroughfare+", "+ myPlaceByLocation[0].featureName
//                vm.editMarkerToMap(id, myNewAddress,arg0.position.latitude, arg0.position.longitude)
//                thisMap.animateCamera(CameraUpdateFactory.newLatLngZoom(arg0.position, 17f))
//
//                Log.d(TAG + "_END", message)
//            }
//
//            override fun onMarkerDrag(arg0: Marker) {
//                val message = arg0!!.position.latitude.toString() + "" + arg0.position.longitude.toString()
//                Log.d(TAG + "_DRAG", message)
//            }
//        })
//    }

    override fun onMarkerClick(marker: Marker): Boolean {
        marker.remove()
        return false
    }
}
