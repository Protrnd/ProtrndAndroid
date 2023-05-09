package protrnd.com.ui.viewholder

import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
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
            viewModel.viewModelScope.launch {
                when (val profileResult = viewModel.getProfileById(conversation.receiverId)) {
                    is Resource.Success -> {
                        if (profileResult.value.successful) {
                            view.chatProfileFullName.text = profileResult.value.data.fullname
                        }
                    }
                    else -> {}
                }
            }
        } else {
            viewModel.viewModelScope.launch {
                when (val profileResult = viewModel.getProfileById(conversation.senderid)) {
                    is Resource.Success -> {
                        if (profileResult.value.successful) {
                            view.chatProfileFullName.text = profileResult.value.data.fullname
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}