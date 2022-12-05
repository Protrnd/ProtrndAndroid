package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Comment
import protrnd.com.databinding.CommentsRvLayoutBinding
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.viewholder.CommentViewHolder

class CommentsAdapter(private val comments: List<Comment>, val viewModel: HomeViewModel):RecyclerView.Adapter<CommentViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CommentViewHolder(
        CommentsRvLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position], viewModel)
    }

    override fun getItemCount(): Int = comments.size
}