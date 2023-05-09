package protrnd.com.ui.adapter

//import protrnd.com.ui.launchNetworkRequest
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.PostImageItemBinding
import protrnd.com.ui.adapter.listener.ImageClickListener
import protrnd.com.ui.post.FullImageVideoDialogFragment
import protrnd.com.ui.post.NewPostActivity
import protrnd.com.ui.viewholder.ImageListViewHolder
import protrnd.com.ui.viewmodels.HomeViewModel
import protrnd.com.ui.visible

class PostImagesAdapter(
    private val activity: AppCompatActivity,
    private val images: List<String>? = null,
    val uri: MutableList<Uri>? = null,
    val viewModel: ViewModel? = null,
    val post: Post? = null,
    val currentProfile: Profile
) : RecyclerView.Adapter<ImageListViewHolder>() {

    private lateinit var imageClickListener: ImageClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageListViewHolder(
        PostImageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ImageListViewHolder, position: Int) {
        if (images != null) {
            holder.bind(images, position)
            holder.view.root.setOnClickListener {
                val fullImageVideoDialog = FullImageVideoDialogFragment(
                    viewModel as HomeViewModel,
                    post,
                    images,
                    position,
                    currentProfile
                )
                fullImageVideoDialog.show(activity.supportFragmentManager, fullImageVideoDialog.tag)
            }
        } else if (uri != null) {
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