package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.data.models.Notification
import protrnd.com.data.models.Profile
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.NotificationRvItemBinding
import protrnd.com.ui.getAgo
import protrnd.com.ui.setSpannableBold
import protrnd.com.ui.viewmodels.NotificationViewModel

class NotificationsViewHolder(val view: NotificationRvItemBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(
        notification: Notification,
        viewModel: NotificationViewModel
    ) {
        val ago = notification.time.getAgo()
        val message = "${notification.message}. "
        val displayText = message + ago
        view.notificationMessage.text = displayText.setSpannableBold(message)
        CoroutineScope(Dispatchers.IO).launch {
            when (val profile = viewModel.getProfileById(notification.senderid)) {
                is Resource.Success -> {
                    if (profile.value.data.profileimg.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            loadImage(profile.value.data)
                        }
                    }
                }
                is Resource.Loading -> {}
                is Resource.Failure -> {}
            }
        }
    }

    private fun loadImage(profile: Profile) {
        Glide.with(view.root)
            .load(profile.profileimg)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.profileImage)
    }
}