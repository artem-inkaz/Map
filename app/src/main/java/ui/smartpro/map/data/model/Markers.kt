package ui.smartpro.map.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Markers(
    var id: Long = 0,
    var adress: String? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
): Parcelable
