package protrnd.com.ui.viewholder

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import protrnd.com.databinding.ImageVideoItemLayoutBinding

class ImageVideoViewHolder(val view: ImageVideoItemLayoutBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(thumbnail: String) {
        view.root.layoutParams = ViewGroup.LayoutParams(
            (Resources.getSystem().displayMetrics.widthPixels / 3),
            (Resources.getSystem().displayMetrics.widthPixels / 3)
        )
        Glide.with(view.root.context)
            .load(thumbnail)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.imageThumbnail)
        view.toggleBtn.setOnCheckedChangeListener { _, isChecked ->
            view.checkBtn.isChecked = isChecked
        }
    }
}