package protrnd.com.ui.viewholder

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import protrnd.com.databinding.PostImageItemBinding
import protrnd.com.ui.post.NewPostActivity
import protrnd.com.ui.visible

class ImageListViewHolder(val view: PostImageItemBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(imageUrls: List<String>, position: Int) {
        Glide.with(view.postImage)
            .load(imageUrls[position])
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.postImage)

        if (imageUrls[position].contains(".mp4")) {
//            CoroutineScope(Dispatchers.IO).launch {
//                val retriever = MediaMetadataRetriever()
//                retriever.setDataSource(imageUrls[position], HashMap<String, String>())
//                val image =
//                    retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
//                withContext(Dispatchers.Main) {
//                    Glide.with(view.postImage)
//                        .load(image)
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .placeholder(R.drawable.texture_img)
//                        .into(view.postImage)
//                }
//            }
            view.playBtn.visible(true)
        } else {
//            Glide.with(view.postImage)
//                .load(imageUrls[position])
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(view.postImage)
        }
    }

    fun bind(activity: NewPostActivity?, imageUri: Uri) {
        Glide.with(view.root).load(imageUri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.postImage)
        if (activity != null) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(activity, imageUri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
            val isVideo = "yes" == hasVideo
            if (isVideo)
                view.playBtn.visible(true)
        }
    }
}