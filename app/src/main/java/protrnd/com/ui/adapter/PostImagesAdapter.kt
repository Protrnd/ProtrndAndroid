package protrnd.com.ui.adapter

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.PostImageItemBinding
import protrnd.com.ui.adapter.listener.ImageClickListener
//import protrnd.com.ui.launchNetworkRequest
import protrnd.com.ui.post.NewPostActivity
import protrnd.com.ui.viewholder.ImageListViewHolder
import protrnd.com.ui.visible

class PostImagesAdapter(
    private val activity: Activity,
    private val images: List<String>? = null,
    val uri: MutableList<Uri>? = null
) : RecyclerView.Adapter<ImageListViewHolder>() {

    lateinit var imageClickListener: ImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageListViewHolder(
        PostImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ImageListViewHolder, position: Int) {
        if (images != null) {
            holder.bind(images[position])
        }
        if (uri != null) {
            when (activity) {
                is NewPostActivity -> {
                    holder.bind(activity = activity, uri[position])
                    holder.view.removeImage.visible(true)
                    holder.view.removeImage.setOnClickListener {
                        uri.remove(uri[position])
                        this.notifyItemRemoved(position)
                        notifyItemRangeChanged(position, itemCount - 1)
                    }
                    holder.view.postImage.setOnClickListener {
                        imageClickListener.imageClickListener(uri[position], position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        if (images != null)
            return images.size
        if (uri != null)
            return uri.size
        return 0
    }

    fun viewClick(imageClickListener: ImageClickListener) {
        this.imageClickListener = imageClickListener
    }
}