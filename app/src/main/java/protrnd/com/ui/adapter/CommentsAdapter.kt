package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.Comment
import protrnd.com.data.models.Profile
import protrnd.com.data.network.MemoryCache
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.CommentsRvLayoutBinding
import protrnd.com.ui.viewholder.CommentViewHolder
import protrnd.com.ui.viewmodels.HomeViewModel

class CommentsAdapter(
    private val comments: List<Comment>,
    val viewModel: HomeViewModel,
    private val lifecycleOwner: LifecycleOwner
) :
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

        val profileLive = MutableLiveData<Profile>()
        val liveData: LiveData<Profile> = profileLive

        liveData.observe(lifecycleOwner) {
            holder.bind(comment, it)
        }

        val profile = MemoryCache.profiles[comment.userid]
        if (profile != null) {
            val profileResult: Profile = profile
            profileLive.postValue(profileResult)
        }

        holder.itemView.autoDisposeScope.launch {
            when (val result = viewModel.getProfileById(comment.userid)) {
                is Resource.Success -> {
                    profileLive.postValue(result.value.data)
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