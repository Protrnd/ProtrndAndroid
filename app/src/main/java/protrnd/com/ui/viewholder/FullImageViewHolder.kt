package protrnd.com.ui.viewholder

import android.media.MediaMetadataRetriever
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.FullImageViewBinding
import protrnd.com.ui.post.FullImageVideoDialogFragment
import protrnd.com.ui.visible

class FullImageViewHolder(val view: FullImageViewBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(
        imageUrls: List<String>,
        position: Int,
        fragmentManager: FragmentManager,
        currentUserProfile: Profile,
        post: Post
    ) {
        if (imageUrls[position].contains(".mp4")) {
            view.playBtn.visible(true)
            CoroutineScope(Dispatchers.IO).launch {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(imageUrls[position], HashMap<String, String>())
                val bmp = retriever.getFrameAtTime(1000)
                withContext(Dispatchers.Main) {
                    Glide.with(view.postImage)
                        .load(bmp)
                        .into(view.postImage)
                }
            }
        } else {
            Glide.with(view.postImage)
                .load(imageUrls[position])
                .into(view.postImage)
        }

        view.playBtn.setOnClickListener {
            if (imageUrls[position].contains(".mp4")) {
                val bottomSheet = FullImageVideoDialogFragment(
                    viewModel = null,
                    imageUrls = imageUrls,
                    post = post,
                    position = position,
                    currentProfile = currentUserProfile
                )
                bottomSheet.show(fragmentManager, bottomSheet.tag)
            }
        }
    }
}