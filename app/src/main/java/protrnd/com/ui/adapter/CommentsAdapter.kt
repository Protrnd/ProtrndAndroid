package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Comment
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.CommentsRvLayoutBinding
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.viewholder.CommentViewHolder

class CommentsAdapter(private val comments: List<Comment>, val viewModel: HomeViewModel) :
    RecyclerView.Adapter<CommentViewHolder>() {

    interface ClickListener {
        fun clickProfile(profileId: String)
    }

    private var clickProfile: ClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CommentViewHolder(
        CommentsRvLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.itemView.autoDisposeScope.launch {
            when (val result = viewModel.getProfileById(comment.userid)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        holder.bind(comment, result.value.data)
                    }
                }
                else -> {}
            }
        }

        holder.view.profileImage.setOnClickListener {
            clickProfile?.clickProfile(comment.userid)
        }
    }

    override fun getItemCount(): Int = comments.size

    fun clickListener(clickListener: ClickListener) {
        clickProfile = clickListener
    }

}