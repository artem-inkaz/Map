package ui.smartpro.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
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
import kotlinx.coroutines.Job
import org.koin.androidx.viewmodel.ext.android.viewModel
import ui.smartpro.map.R
import ui.smartpro.map.base.BaseFragment
import ui.smartpro.map.databinding.FragmentMapsBinding
import java.io.IOException
import java.util.*

class MapsFragment(override val layoutId: Int = R.layout.fragment_maps) :
    BaseFragment<FragmentMapsBinding>() {

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
    private val vm by viewModel<MarkerViewModel>()
    private val callback = OnMapReadyCallback { googleMap ->
        thisMap = googleMap
        val sydney = LatLng(-34.0, 151.0)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        activateMyLocation(thisMap) // Сетим появление штатной кнопки для показа моего места

    }

    private val permReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value
            }
            if (granted) {
                getMyCurrentLocation()
                getMapData()
                initSearchByAddress()
            } else {
                checkGPSPermission()
            }
        }



    override fun initViews() {
        super.initViews()

        client = activity?.let { it1 -> LocationServices.getFusedLocationProviderClient(it1) }!!
        checkGPSPermission() // Запрашиваем все разрешения
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
                    initSearchByAddress()

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
    }

    private fun addMarkerToMap(mapFragment: SupportMapFragment?){
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setOnMapClickListener {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                currentMarkerLocation = currentLatLng
                getAddressAsync(context as Activity, currentLatLng)
                // добавляем радиус геозоны
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
                // добавляем на карту маркер геозоны
                marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("Я тут")
                        .icon(BitmapDescriptorFactory.defaultMarker())
                )
                marker?.showInfoWindow()
            }
        }
    }

    private fun initSearchByAddress() {
//        val geoCoder = Geocoder(context, Locale.getDefault())
//        Thread {
//            try {
//                val addresses = geoCoder.getFromLocationName(place, 1)
//                if (addresses.size > 0) {
//                    goToAddress(addresses, it)
//                }
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }.start()
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
            vm.addCurrentMarker(location, describeAdress)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun goToAddress(
        addresses: MutableList<Address>,
        view: View
    ) {

        val location = LatLng(
            addresses[0].latitude,
            addresses[0].longitude
        )

        val geoCoder = Geocoder(context, Locale.getDefault())
        val myPlaceByLocation: List<Address> =
            geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        val myAddress = myPlaceByLocation[0].getAddressLine(0)

        val bundle = Bundle()
        bundle.putString("Location by Search", myAddress)
        view.post {
            setMarker(location, myAddress)
            thisMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    17f
                )
            )
        }
    }

    private fun setMarker(
        location: LatLng,
        searchText: String
    ): Marker? {
        return thisMap.addMarker(
            MarkerOptions()
                .position(location)
                .title(searchText)
                .icon(BitmapDescriptorFactory.defaultMarker())
        )
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
        val options: MarkerOptions = MarkerOptions()
            .position(loc)
            .title("Я Тут!")
            .icon(BitmapDescriptorFactory.defaultMarker())

        thisMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 17f))
        thisMap.addMarker(options)

        val geoCoder = Geocoder(context, Locale.getDefault())
        val myPlaceByLocation: List<Address> =
            geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        val myAddress = myPlaceByLocation[0].getAddressLine(0)
        val bundle = Bundle()
        bundle.putString("Location by Search", myAddress)
    }
}