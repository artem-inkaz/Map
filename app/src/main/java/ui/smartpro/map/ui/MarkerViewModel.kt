package ui.smartpro.map.ui

import android.app.Application
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.smartpro.map.base.BaseViewModel
import ui.smartpro.map.data.model.Markers
import ui.smartpro.map.utils.DataConstants

@OptIn(InternalCoroutinesApi::class)
class MarkerViewModel(app: Application) : BaseViewModel(app) {
    val setMarker: MutableStateFlow<Markers> = MutableStateFlow(Markers())
    val idMarker: MutableStateFlow<Long> = MutableStateFlow(0)
    val setMarkerList: MutableStateFlow<MutableList<Markers>?> = MutableStateFlow(mutableListOf())

    private val _stateInitial = MutableStateFlow(false)
    val stateInitial = _stateInitial.asStateFlow()
    private var markerData: MutableList<Markers> = ArrayList()
    private var editMarker: Markers = Markers()

    /**
     * получаем маркеры
     */
    fun addMarkerToMap(location: LatLng, adress: String) {
        modelScope.launch {
            val marker = DataConstants.markerList?.firstOrNull {
                it.latitude == location.latitude &&
                it.longitude == location.longitude ||
                it.adress == adress
            }
            if (marker == null) {
                markerData.add(
                    Markers(
                        id = getRandomId(),
                        adress = adress,
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                )
                idMarker.value = getRandomId()
                setMarkerList.value = markerData
                setMarker.value = markerData.last()
                _stateInitial.emit(true)
               DataConstants.markerList?.add(setMarker.value)
            }
        }
    }

    /**
     * редактируем маркер
     */
    fun editMarkerToMap(id: Long, adress: String?,latitude: Double?, longitude: Double?) {
        modelScope.launch {
            val marker = DataConstants.markerList?.filter {
                it.id == id

            }?.forEach {
                it.adress = adress
                it.latitude = latitude
                it.longitude = longitude
            }
        }
    }

    /**
     * редактируем перетаскиваемый маркер
     */
    fun editDragMarkerToMap(adress: String?,latitude: Double?, longitude: Double?) {
        modelScope.launch {
            val marker = DataConstants.markerList?.filter {
                it.adress == adress &&
                it.latitude == latitude &&
                it.longitude == longitude

            }?.forEach {
                it.adress = adress
                it.latitude = latitude
                it.longitude = longitude
            }
        }
    }

    /**
     *  получаем Random ID
     */
    private fun getRandomId(): Long {
        val random = (1..100).random()
        val announceId = random.toLong()

        return announceId
    }

    fun getListMarker(){
        modelScope.launch {
            setMarkerList.value = DataConstants.markerList
        }
    }
}