package protrnd.com.ui.viewholder

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog
import protrnd.com.R
import protrnd.com.databinding.PostImageItemBinding
import protrnd.com.databinding.VideoPlayerLayoutBinding
import protrnd.com.ui.hideSystemUI
import protrnd.com.ui.post.NewPostActivity
import protrnd.com.ui.showSystemUI
import protrnd.com.ui.visible

class ImageListViewHolder(val view: PostImageItemBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(imageUrl: String) {
        Glide.with(view.root).load(imageUrl).diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.postImage)
        if (imageUrl.contains(".mp4"))
            view.playBtn.visible(true)
        this.itemView.setOnClickListener {
            if (imageUrl.contains(".mp4")) {
                val bottomSheet = BottomSheetDialog(this.itemView.context, R.style.BottomSheetTheme)
                val binding =
                    VideoPlayerLayoutBinding.inflate(LayoutInflater.from(this.itemView.context))
                bottomSheet.setContentView(binding.root)
                binding.root.minHeight = Resources.getSystem().displayMetrics.heightPixels
                this.itemView.hideSystemUI((this.itemView.context as Activity).window)
                binding.videoView.setVideoPath(imageUrl)
                binding.videoView.isDrawingCacheEnabled = true
                binding.videoView.start()
                binding.closeVideo.setOnClickListener {
                    binding.videoView.stopPlayback()
                    binding.videoView.clearFocus()
                    bottomSheet.dismiss()
                    this.itemView.showSystemUI((this.itemView.context as Activity).window)
                }
                bottomSheet.setOnDismissListener {
                    this.itemView.showSystemUI((this.itemView.context as Activity).window)
                }
                bottomSheet.show()
                bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                bottomSheet.behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
            }
        }
    }

    fun bind(activity: NewPostActivity?, imageUri: Uri) {
        Glide.with(view.root).load(imageUri).diskCacheStrategy(DiskCacheStrategy.ALL)
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