package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Conversation
import protrnd.com.data.models.Profile
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.RecentChatProfileLayoutBinding
import protrnd.com.ui.getTimeWithCenterDot
import protrnd.com.ui.setSpannableColor
import protrnd.com.ui.viewmodels.ChatViewModel

class RecentChatProfileViewHolder(val view: RecentChatProfileLayoutBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(conversation: Conversation, viewModel: ChatViewModel, profile: Profile) {
        view.timeAgo.text = getTimeWithCenterDot(conversation.time)
        view.recentChatMessage.text = conversation.recentMessage
        if (conversation.recentMessage == "Support")
            view.recentChatMessage.text = conversation.recentMessage.setSpannableColor("Support")
        if (profile.id == conversation.senderid) {
            CoroutineScope(Dispatchers.IO).launch {
                val profileResult = viewModel.getStoredProfile(conversation.receiverId)?.first()
                if (profileResult != null) {
                    withContext(Dispatchers.Main) {
                        view.chatProfileFullName.text = profileResult.fullname
                        if (profileResult.profileimg.isNotEmpty())
                            Glide.with(view.root).load(profileResult.profileimg).circleCrop().into(view.imageView7)
                    }
                }
                when (val result = viewModel.getProfileById(conversation.receiverId)) {
                    is Resource.Success -> {
                        if (result.value.successful) {
                            withContext(Dispatchers.Main) {
                                if (view.chatProfileFullName.text.toString() != result.value.data.fullname)
                                    view.chatProfileFullName.text = result.value.data.fullname
                                if (result.value.data.profileimg.isNotEmpty())
                                    Glide.with(view.root).load(result.value.data.profileimg).circleCrop().into(view.imageView7)
                            }
                        }
                    }
                    else -> {}
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val profileResult = viewModel.getStoredProfile(conversation.senderid)?.first()
                if (profileResult != null) {
                    withContext(Dispatchers.Main) {
                        view.chatProfileFullName.text = profileResult.fullname
                        if (profileResult.profileimg.isNotEmpty())
                            Glide.with(view.root).load(profileResult.profileimg).circleCrop().into(view.imageView7)
                    }
                }
                when (val result = viewModel.getProfileById(conversation.senderid)) {
                    is Resource.Success -> {
                        if (result.value.successful) {
                            withContext(Dispatchers.Main) {
                                if (view.chatProfileFullName.text.toString() != result.value.data.fullname)
                                    view.chatProfileFullName.text = result.value.data.fullname
                                if (result.value.data.profileimg.isNotEmpty())
                                    Glide.with(view.root).load(result.value.data.profileimg).circleCrop().into(view.imageView7)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}