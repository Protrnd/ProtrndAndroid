package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.PromotionLocationSelectedBinding

class SelectedLocationViewHolder(val view: PromotionLocationSelectedBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(location: String) {
        view.root.text = location
    }
}