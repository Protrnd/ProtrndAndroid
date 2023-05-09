package protrnd.com.ui.viewholder

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import protrnd.com.databinding.ProfilePostRvItemBinding

class ImageThumbnailViewHolder(val view: ProfilePostRvItemBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(thumbnail: String) {
        view.root.layoutParams = ViewGroup.LayoutParams(
            (Resources.getSystem().displayMetrics.widthPixels / 4),
            (Resources.getSystem().displayMetrics.widthPixels / 4)
        )
        Glide.with(view.root.context)
            .load(thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.imageThumbnail)
    }
}