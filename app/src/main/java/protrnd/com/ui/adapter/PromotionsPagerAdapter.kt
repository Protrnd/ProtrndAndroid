package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import protrnd.com.data.models.Promotion
import protrnd.com.databinding.PromotionBannerItemBinding
import protrnd.com.ui.adapter.listener.PromotionClickListener
import protrnd.com.ui.viewholder.PromotionBannerViewHolder

class PromotionsPagerAdapter(var promotions: List<Promotion>) :
    RecyclerView.Adapter<PromotionBannerViewHolder>() {
    lateinit var promotionClickListener: PromotionClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PromotionBannerViewHolder(
        PromotionBannerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = promotions.size

    override fun onBindViewHolder(holder: PromotionBannerViewHolder, position: Int) {
        holder.view.root.setOnClickListener {
            promotionClickListener.click(position, promotions[position])
        }

        Glide.with(holder.view.root)
            .load(promotions[position].bannerurl)
            .into(holder.view.promotionBanner)
    }

    fun clickPromotion(promotionClickListener: PromotionClickListener) {
        this.promotionClickListener = promotionClickListener
    }
}