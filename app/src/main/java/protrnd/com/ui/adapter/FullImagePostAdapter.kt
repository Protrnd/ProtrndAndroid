package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.databinding.FullImageViewBinding
import protrnd.com.ui.viewholder.FullImageViewHolder

class FullImagePostAdapter(
    val fragmentManager: FragmentManager,
    var images: List<String>,
    val currentUserProfile: Profile,
    val post: Post
) : RecyclerView.Adapter<FullImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FullImageViewHolder(
        FullImageViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: FullImageViewHolder, position: Int) {
        holder.bind(images, position, fragmentManager, currentUserProfile, post)
    }

    override fun getItemCount(): Int {
        return images.size
    }
}