package ui.smartpro.map.ui.detail

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ui.smartpro.map.R
import ui.smartpro.map.base.BaseFragment
import ui.smartpro.map.databinding.FragmentDetailMarkerBinding

class DetailMarkerFragment(override val layoutId: Int = R.layout.fragment_detail_marker) :
    BaseFragment<FragmentDetailMarkerBinding>() {

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
        markerAdapter = MarkerAdapter()
        recyclerView.adapter = markerAdapter
    }


}