package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import protrnd.com.data.models.Comment
import protrnd.com.data.models.Profile
import protrnd.com.databinding.CommentsRvLayoutBinding
import protrnd.com.ui.getAgo

class CommentViewHolder(val view: CommentsRvLayoutBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(comment: Comment, profile: Profile) {
//        val username = "@${profile.username}"
//        view.username.text = username
        view.fullname.text = profile.fullname
        if (profile.profileimg.isNotEmpty())
            Glide.with(view.root.context)
                .load(profile.profileimg)
                .circleCrop()
                .into(view.profileImage)
        view.commentContent.text = comment.comment
        view.dateTime.text = comment.time.getAgo()
    }
}