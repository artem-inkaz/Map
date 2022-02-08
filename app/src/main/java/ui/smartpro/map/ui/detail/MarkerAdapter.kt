package ui.smartpro.map.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ui.smartpro.map.data.model.Markers
import ui.smartpro.map.databinding.ItemMarkerBinding

class MarkerAdapter:RecyclerView.Adapter<MarkerAdapter.MarkerVieHolder>() {
    private var allMarkerList: MutableList<Markers> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MarkerVieHolder(
        ItemMarkerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
    )

    override fun onBindViewHolder(holder: MarkerVieHolder, position: Int) {
        holder.bind(allMarkerList[position])
    }

    override fun getItemCount() = allMarkerList.size

    fun appendItem(request: Markers) {
        allMarkerList.add(request)
        notifyItemInserted(itemCount - 1) // С анимацией добавления
    }

    inner class MarkerVieHolder(
        private val vb: ItemMarkerBinding
    ) :
        RecyclerView.ViewHolder(vb.root) {

        fun bind(data: Markers) = with(vb) {
            vb.markerId.text = data.id.toString()
            vb.latitude.text = data.latitude.toString()
            vb.longitude.text = data.longitude.toString()
            vb.adress.text = data.adress
        }
    }
}