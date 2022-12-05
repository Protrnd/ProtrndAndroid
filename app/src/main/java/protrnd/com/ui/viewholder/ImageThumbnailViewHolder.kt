package protrnd.com.ui.viewholder

import android.content.res.Resources
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import protrnd.com.data.models.Post
import protrnd.com.databinding.ProfilePostRvItemBinding

class ImageThumbnailViewHolder(val view: ProfilePostRvItemBinding): RecyclerView.ViewHolder(view.root) {
    fun bind(post: Post) {
        view.root.layoutParams = ViewGroup.LayoutParams((Resources.getSystem().displayMetrics.widthPixels / 3.15).toInt(),(Resources.getSystem().displayMetrics.widthPixels / 3.15).toInt())
        Glide.with(view.root.context)
            .load(post.uploadurls[0])
            .into(view.imageThumbnail)
    }
}