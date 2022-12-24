package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Comment
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.CommentsRvLayoutBinding
import protrnd.com.ui.getAgo
import protrnd.com.ui.home.HomeViewModel

class CommentViewHolder(val view: CommentsRvLayoutBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(comment: Comment, viewModel: HomeViewModel) {
        this.itemView.autoDisposeScope.launch {
            when (val profile = viewModel.getProfileById(comment.userid)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        val username = "@${profile.value.data.username}"
                        view.username.text = username
                        view.fullname.text = profile.value.data.fullname
                        if (profile.value.data.profileimg.isNotEmpty())
                            Glide.with(view.root.context)
                                .load(profile.value.data.profileimg)
                                .circleCrop()
                                .into(view.profileImage)
                        view.commentContent.text = comment.comment
                        view.dateTime.text = comment.time.getAgo()
                    }
                }
                else -> {}
            }
        }
    }
}