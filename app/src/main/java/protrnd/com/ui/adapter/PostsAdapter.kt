package protrnd.com.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.likePost
import protrnd.com.ui.reload
import protrnd.com.ui.setupLikes
import protrnd.com.ui.showCommentSection
import protrnd.com.ui.viewholder.PostsViewHolder

class PostsAdapter(
    var posts: MutableList<Post> = ArrayList(),
    val viewModel: HomeViewModel,
    val lifecycleOwner: LifecycleOwner,
    val currentProfile: Profile,
    val activity: Activity
) : RecyclerView.Adapter<PostsViewHolder>() {

    private val otherProfileHash = HashMap<String, Profile>()

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
            holder.itemView.context.showCommentSection(
                viewModel,
                lifecycleOwner,
                holder.itemView.autoDisposeScope,
                otherProfileHash["otherProfile"]!!,
                currentProfile,
                postData.identifier
            )
        }

        holder.view.likeToggle.setOnClickListener {
            likePost(
                holder.view.likeToggle,
                holder.view.likesCount,
                holder.itemView.autoDisposeScope,
                viewModel,
                postData.identifier,
                otherProfileHash["otherProfile"]!!,
                currentProfile
            )
        }

        holder.itemView.autoDisposeScope.launch {
            setupLikes(
                viewModel,
                postData.id,
                lifecycleOwner,
                holder.view.likesCount,
                holder.view.likeToggle
            )

            getOtherProfile(holder, postData)
        }
    }

    private suspend fun getOtherProfile(holder: PostsViewHolder, postData: Post) {
        when (val otherProfile = viewModel.getProfileById(postData.profileid)) {
            is Resource.Success -> {
                withContext(Dispatchers.Main) {
                    otherProfileHash["otherProfile"] = otherProfile.value.data
                    holder.bind(activity, postData, otherProfile.value.data, currentProfile)
                }
            }
            is Resource.Loading -> {}
            else -> {
                reload {
                    holder.itemView.autoDisposeScope.launch {
                        withContext(Dispatchers.Main) {
                            holder.bind(
                                activity,
                                postData,
                                null,
                                currentProfile
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
}