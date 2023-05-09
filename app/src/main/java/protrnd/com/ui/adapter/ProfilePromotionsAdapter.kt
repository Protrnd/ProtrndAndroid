package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.PromoBannerBinding
import protrnd.com.ui.viewholder.ProfilePromotionsViewHolder

class ProfilePromotionsAdapter : RecyclerView.Adapter<ProfilePromotionsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProfilePromotionsViewHolder(
        PromoBannerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = 5

    override fun onBindViewHolder(holder: ProfilePromotionsViewHolder, position: Int) {
    }
}