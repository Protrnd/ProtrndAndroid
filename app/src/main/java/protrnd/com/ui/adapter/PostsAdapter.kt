package protrnd.com.ui.adapter

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.R
import protrnd.com.data.models.CommentDTO
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.Resource
import protrnd.com.databinding.BottomSheetCommentsBinding
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.enable
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.viewholder.PostsViewHolder
import protrnd.com.ui.visible

class PostsAdapter(
    private var posts: MutableList<Post> = ArrayList(),
    val viewModel: HomeViewModel,
    val lifecycleOwner: LifecycleOwner,
    val currentProfile: Profile
) : RecyclerView.Adapter<PostsViewHolder>() {

    fun addAll(result: List<Post>) {
        val lastIndex = posts.size - 1
        posts.addAll(result)
        notifyInsertChange(lastIndex, result.size, posts.size)
    }

    fun setList(result: MutableList<Post>) {
        if (posts.isEmpty()) {
            posts = result
        } else {
            val previousSize = posts.size
            posts = result
            notifyItemRangeRemoved(0, previousSize)
            notifyItemRangeChanged(0, previousSize)
        }
        notifyInsertChange(0, result.size, result.size)
    }

    private fun notifyInsertChange(insertStart: Int, insertSize: Int, changedSize: Int) {
        notifyItemRangeInserted(insertStart, insertSize)
        notifyItemRangeChanged(0, changedSize)
    }

    override fun getItemCount(): Int = posts.size

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val postData = posts[position]

        holder.view.commentBtn.setOnClickListener {
            val bottomSheet = BottomSheetDialog(holder.itemView.context, R.style.BottomSheetTheme)
            val binding =
                BottomSheetCommentsBinding.inflate(LayoutInflater.from(holder.itemView.context))
            bottomSheet.setContentView(binding.root)
            binding.commentSection.layoutManager = LinearLayoutManager(holder.itemView.context)
            viewModel.getComments(postData.identifier)
            viewModel.comments.observe(lifecycleOwner) { comments ->
                when (comments) {
                    is Resource.Success -> {
                        if (comments.value.data.isNotEmpty()) {
                            binding.commentSection.visible(true)
                            binding.noCommentsTv.visible(false)
                            val commentsText = "${comments.value.data.size} Comments"
                            binding.commentsCount.text = commentsText
                            val commentAdapter = CommentsAdapter(
                                viewModel = viewModel,
                                comments = comments.value.data
                            )
                            binding.commentSection.adapter = commentAdapter
                        }
                    }
                    else -> {}
                }
            }

            binding.sendComment.setOnClickListener {
                val commentContent = binding.commentInput.text.toString().trim()
                if (commentContent.isNotEmpty()) {
                    val comment = CommentDTO(comment = commentContent, postid = postData.identifier)
                    holder.itemView.autoDisposeScope.launch {
                        when (val result = viewModel.addComment(comment)) {
                            is Resource.Success -> {
                                binding.sendComment.enable(true)
                                if (result.value.successful) {
                                    binding.commentInput.text.clear()
                                    viewModel.getComments(postData.identifier)
                                }
                            }
                            is Resource.Loading -> {
                                binding.sendComment.enable(false)
                            }
                            is Resource.Failure -> {
                                binding.sendComment.enable(true)
                            }
                        }
                    }
                } else {
                    binding.inputField.error = "This field cannot be empty"
                }
            }

            bottomSheet.show()
            bottomSheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            bottomSheet.behavior.peekHeight = Resources.getSystem().displayMetrics.heightPixels
        }

        holder.view.likeToggle.setOnClickListener {
            val liked = holder.view.likeToggle.isChecked
            var likesResult = holder.view.likesCount.text.toString()
            likesResult = if (likesResult.contains("likes"))
                likesResult.replace(" likes", "")
            else
                likesResult.replace(" like", "")
            var count = if (likesResult.isNotEmpty()) likesResult.toInt() else 0
            holder.itemView.autoDisposeScope.launch {
                if (liked) {
                    count += 1
                    val likes = if (count > 1) "$count likes" else "$count like"
                    holder.view.likesCount.text = likes
                    when (viewModel.likePost(postData.identifier)) {
                        is Resource.Success -> {}
                        else -> {}
                    }
                } else {
                    count -= 1
                    val likes = if (count > 1) "$count likes" else "$count like"
                    holder.view.likesCount.text = likes
                    when (viewModel.unlikePost(postData.identifier)) {
                        is Resource.Success -> {}
                        else -> {}
                    }
                }
            }
        }

        holder.itemView.autoDisposeScope.launch {
            when (val likesCount = viewModel.getLikesCount(postData.id)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        val count = likesCount.value.data as Double
                        val likes =
                            if (count > 1) "${count.toInt()} likes" else "${count.toInt()} like"
                        holder.view.likesCount.text = likes
                    }
                }
                else -> {}
            }

            val isLiked = viewModel.postIsLiked(postData.identifier)
            withContext(Dispatchers.Main) {
                isLiked.observe(lifecycleOwner) {
                    when (it) {
                        is Resource.Success -> {
                            holder.view.likeToggle.isChecked = it.value.data
                        }
                        else -> {}
                    }
                }
            }

            when (val otherProfile = viewModel.getProfileById(postData.profileid)) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        holder.bind(postData, otherProfile.value.data, currentProfile)
                    }
                }
                is Resource.Loading -> {}
                is Resource.Failure -> {}
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
}