package protrnd.com.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.Gson
import protrnd.com.data.models.Post
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.adapter.listener.PromoteSupportListener
import protrnd.com.ui.viewholder.PostsViewHolder

class PostsPagingAdapter : PagingDataAdapter<Post, PostsViewHolder>(PostComparator()) {
    private var recyclerResultsListener: SetupRecyclerResultsListener? = null
    private var promoteSupportListener: PromoteSupportListener? = null

    class PostComparator : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }

    interface SetupRecyclerResultsListener {
        fun setupLikes(holder: PostsViewHolder, postData: Post)
        fun setupData(holder: PostsViewHolder, postData: Post)
        fun showCommentSection(postData: Post)
        fun like(holder: PostsViewHolder, postData: Post)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val postData = getItem(position)!!

        holder.view.promoteSupport.setOnClickListener {
            promoteSupportListener?.click(postData)
        }

        holder.view.commentBtn.setOnClickListener {
            recyclerResultsListener?.showCommentSection(postData)
        }

        holder.view.likeToggle.setOnClickListener {
            recyclerResultsListener?.like(holder, postData)
        }

        recyclerResultsListener?.setupData(holder, postData)

        recyclerResultsListener?.setupLikes(holder, postData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    fun setupRecyclerResults(resultsListener: SetupRecyclerResultsListener) {
        this.recyclerResultsListener = resultsListener
    }

    fun promoteSupportPost(promoteListener: PromoteSupportListener) {
        this.promoteSupportListener = promoteListener
    }
}