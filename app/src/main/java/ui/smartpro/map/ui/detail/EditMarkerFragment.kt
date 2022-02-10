package ui.smartpro.map.ui.detail

import org.koin.androidx.viewmodel.ext.android.viewModel
import ui.smartpro.map.R
import ui.smartpro.map.base.BaseFragment
import ui.smartpro.map.data.model.Markers
import ui.smartpro.map.databinding.FragmentEditMarkerBinding
import ui.smartpro.map.ui.MarkerViewModel

class EditMarkerFragment(override val layoutId: Int = R.layout.fragment_edit_marker) :
    BaseFragment<FragmentEditMarkerBinding>() {

    companion object {
        const val BUNDLE_EXTRA = "marker"
    }

    // Для загрузки из MovieList
    private lateinit var markerBundle: Markers
    private val vm by viewModel<MarkerViewModel>()

    override fun initViews() {
        super.initViews()

        if (arguments?.getParcelable<Markers>(BUNDLE_EXTRA) != null) {
            markerBundle = arguments?.getParcelable<Markers>(BUNDLE_EXTRA)!!
            markerBundle.let { movie ->
                displayMarker(movie)
            }
        }

        binding.includedEditLayout.editMarker.setOnClickListener {
            editMarker()
        }

    }

    private fun displayMarker(marker: Markers) {
        with(binding.includedEditLayout) {
            latitudeEdit.append(marker.latitude.toString())
            longitudeEdit.append(marker.longitude.toString())
            adressEdit.append(marker.adress)
            markerId.text = marker.id.toString()
        }
    }

    private fun editMarker() {
        with(binding.includedEditLayout) {
            val id = markerId.text.toString().trim()
            val adress = adressEdit.text.toString().trim()
            val latitude = latitudeEdit.text.toString().trim()
            val longitude = longitudeEdit.text.toString().trim()
            if (id.isNotEmpty() && adress.isNotEmpty() && latitude.isNotEmpty() &&
                longitude.isNotEmpty()
            )
                vm.editMarkerToMap(id.toLong(), adress, latitude.toDouble(), longitude.toDouble())
        }
    }
}