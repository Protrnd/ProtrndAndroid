package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import protrnd.com.data.models.Post
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.adapter.listener.PromoteListener
import protrnd.com.ui.viewholder.PostsViewHolder

class PostsPagingAdapter : PagingDataAdapter<Post, PostsViewHolder>(PostComparator()) {
    private var recyclerResultsListener: SetupRecyclerResultsListener? = null
    private var promoteListener: PromoteListener? = null

    class PostComparator : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }

    interface SetupRecyclerResultsListener {
        fun setupLikes(holder: PostsViewHolder, postData: Post)
        fun setupData(holder: PostsViewHolder, postData: Post)
        fun showCommentSection(postData: Post? = null)
        fun like(holder: PostsViewHolder, postData: Post)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.view.promoteSupport.setOnClickListener {
            promoteListener?.click()
        }
//        val postData = getItem(position)!!
//
        holder.view.commentBtn.setOnClickListener {
//            recyclerResultsListener?.showCommentSection(postData)
            recyclerResultsListener?.showCommentSection(null)
        }
//
//        holder.view.likeToggle.setOnClickListener {
//            recyclerResultsListener?.like(holder, postData)
//        }
//
//        recyclerResultsListener?.setupData(holder, postData)
//
//        recyclerResultsListener?.setupLikes(holder, postData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    fun setupRecyclerResults(resultsListener: SetupRecyclerResultsListener) {
        this.recyclerResultsListener = resultsListener
    }

    fun promotePost(promoteListener: PromoteListener) {
        this.promoteListener = promoteListener
    }
}