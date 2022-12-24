package protrnd.com.ui.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Post
import protrnd.com.data.models.Profile
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.PostItemBinding
import protrnd.com.ui.*
import protrnd.com.ui.home.HomeViewModel
import protrnd.com.ui.viewholder.PostsViewHolder

class PostsPagingAdapter(
    val viewModel: HomeViewModel,
    val lifecycleOwner: LifecycleOwner,
    val currentProfile: Profile,
    val activity: Activity
) : PagingDataAdapter<Post, PostsViewHolder>(PostComparator()) {

    class PostComparator : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val postData = getItem(position)!!

        val otherProfileHash = HashMap<String, Profile>()

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
            val liked = holder.view.likeToggle.isChecked
            if (activity.isNetworkAvailable())
                likePost(
                    holder.view.likeToggle,
                    holder.view.likesCount,
                    holder.itemView.autoDisposeScope,
                    viewModel,
                    postData.identifier,
                    otherProfileHash["otherProfile"]!!,
                    currentProfile
                )
            else
                holder.view.likeToggle.isChecked = !liked
        }

        holder.itemView.autoDisposeScope.launch {
            if (activity.isNetworkAvailable())
                setupLikes(
                    viewModel,
                    postData.id,
                    lifecycleOwner,
                    holder.view.likesCount,
                    holder.view.likeToggle
                )

            val otherProfile = viewModel.getProfile(postData.profileid)
            otherProfile?.asLiveData()?.observe(lifecycleOwner) {
                if (it != null) {
                    otherProfileHash["otherProfile"] = it
                    storeAndBindData(postData, it, holder)
                }
            }
            if (activity.isNetworkAvailable())
                getOtherProfile(holder, postData, otherProfileHash)
        }
    }

    private suspend fun getOtherProfile(
        holder: PostsViewHolder,
        postData: Post,
        otherProfileHash: HashMap<String, Profile>
    ) {
        when (val otherProfile = viewModel.getProfileById(postData.profileid)) {
            is Resource.Success -> {
                otherProfileHash["otherProfile"] = otherProfile.data?.data!!
                withContext(Dispatchers.Main) {
                    storeAndBindData(postData, otherProfileHash["otherProfile"]!!, holder)
                }
                viewModel.saveProfile(otherProfile.value.data)
            }
            is Resource.Loading -> {}
            else -> {
                reload {
                    holder.itemView.autoDisposeScope.launch {
                        getOtherProfile(holder, postData, otherProfileHash)
                    }
                }
            }
        }
    }

    private fun storeAndBindData(postData: Post, profile: Profile, holder: PostsViewHolder) {
        holder.bind(activity, postData, profile, currentProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        return PostsViewHolder(
            PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
}