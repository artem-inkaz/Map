package ui.smartpro.map.ui

import android.app.Application
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.smartpro.map.base.BaseViewModel
import ui.smartpro.map.data.model.Markers

class MarkerViewModel(app: Application) : BaseViewModel(app) {
    //    val _stateInitial: MutableStateFlow<List<Markers>?> = MutableStateFlow(null)
    private val _stateInitial = MutableStateFlow(mutableListOf<Markers>())
    val stateInitial = _stateInitial.asStateFlow()
    private var listGroupDaily: MutableList<Markers> = ArrayList()

    /**
     * получаем маркеры
     */
    fun addCurrentMarker(location: LatLng, adress: String) {
        modelScope.launch {
            listGroupDaily.add(
                Markers(
                    id = getRandomId(),
                    adress = adress,
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            )

            _stateInitial.emit(listGroupDaily)
        }
    }

    /**
     *  получаем Random ID
     */
    private fun getRandomId(): Long {
        val start = System.currentTimeMillis()
        val random = (1000..1000000).random()
        val announceId = (start / random)

        return announceId
    }
}