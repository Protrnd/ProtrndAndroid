package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.ImageVideoItemLayoutBinding
import protrnd.com.ui.adapter.listener.ImagePostItemClickListener
import protrnd.com.ui.viewholder.ImageVideoViewHolder

class ImageVideoResultAdapter(private val uris: ArrayList<String>) :
    RecyclerView.Adapter<ImageVideoViewHolder>() {
    lateinit var imagePostItemClickListener: ImagePostItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageVideoViewHolder(
        ImageVideoItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ImageVideoViewHolder, position: Int) {
        holder.bind(uris[position])
//        holder.itemView.setOnClickListener {
//            imagePostItemClickListener.postItemClickListener(post = posts[position])
//        }
    }

    override fun getItemCount(): Int = uris.size

    fun imageClickListener(imagePostItemClickListener: ImagePostItemClickListener) {
        this.imagePostItemClickListener = imagePostItemClickListener
    }
}