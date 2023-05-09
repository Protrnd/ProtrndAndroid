package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Post
import protrnd.com.databinding.ProfilePostRvItemBinding
import protrnd.com.ui.adapter.listener.ImagePostItemClickListener
import protrnd.com.ui.viewholder.ImageThumbnailViewHolder

class ImageThumbnailPostAdapter(private val posts: List<Post>) :
    RecyclerView.Adapter<ImageThumbnailViewHolder>() {
    lateinit var imagePostItemClickListener: ImagePostItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageThumbnailViewHolder(
        ProfilePostRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ImageThumbnailViewHolder, position: Int) {
        holder.bind(posts[position].uploadurls[0])
        holder.itemView.setOnClickListener {
            imagePostItemClickListener.postItemClickListener(post = posts[position])
        }
    }

    override fun getItemCount(): Int = posts.size

    fun imageClickListener(imagePostItemClickListener: ImagePostItemClickListener) {
        this.imagePostItemClickListener = imagePostItemClickListener
    }
}