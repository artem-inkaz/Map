package ui.smartpro.map.ui.detail

import android.os.Bundle
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ui.smartpro.map.R
import ui.smartpro.map.`interface`.OnItemViewClickListener
import ui.smartpro.map.base.BaseFragment
import ui.smartpro.map.data.model.Markers
import ui.smartpro.map.databinding.FragmentDetailMarkerBinding
import ui.smartpro.map.ui.detail.EditMarkerFragment.Companion.BUNDLE_EXTRA
import ui.smartpro.map.utils.DataConstants.Companion.markerList

class DetailMarkerFragment(override val layoutId: Int = R.layout.fragment_detail_marker) :
    BaseFragment<FragmentDetailMarkerBinding>(), OnItemViewClickListener {

    companion object {
        const val TAG = "DetailMarkerFragment"
        fun newInstance() = DetailMarkerFragment()

    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var markerAdapter: MarkerAdapter
    override fun initViews() {
        super.initViews()

        recyclerView = binding.markerRv
        recyclerView.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        recyclerView.setHasFixedSize(true)
        markerAdapter = MarkerAdapter(this)
        recyclerView.adapter = markerAdapter
        markerList?.let { markerAdapter.setAllRequests(it) }
    }

        override fun onItemViewClick(markers: Markers) {
            // сохранение при клике по записи MovieList
//        viewModel.saveMoviesLocally(movie)
            val bundle = Bundle().also {
                it.putParcelable(BUNDLE_EXTRA, markers)
            }

            Navigation.findNavController(requireActivity(), R.id.nav_main_fragment).also {
                it.navigate(R.id.editMarkerFragment, bundle)
            }
        }
}