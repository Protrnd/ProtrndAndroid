package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.PromotionBannerItemBinding
import protrnd.com.ui.viewholder.PromotionBannerViewHolder

class PromotionsPagerAdapter: RecyclerView.Adapter<PromotionBannerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PromotionBannerViewHolder(
        PromotionBannerItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: PromotionBannerViewHolder, position: Int) {
    }
}