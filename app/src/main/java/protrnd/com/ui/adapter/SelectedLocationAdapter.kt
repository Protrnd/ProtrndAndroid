package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.PromotionLocationSelectedBinding
import protrnd.com.ui.adapter.listener.PositionClickListener
import protrnd.com.ui.viewholder.SelectedLocationViewHolder

class SelectedLocationAdapter(private val locations: ArrayList<String> = ArrayList()) :
    RecyclerView.Adapter<SelectedLocationViewHolder>() {
    private lateinit var positionClick: PositionClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SelectedLocationViewHolder(
        PromotionLocationSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SelectedLocationViewHolder, position: Int) {
        holder.bind(location = locations[position])
        holder.itemView.setOnClickListener {
            positionClick.positionClick(position)
        }
    }

    override fun getItemCount() = locations.size

    fun positionClick(positionClickListener: PositionClickListener) {
        positionClick = positionClickListener
    }
}