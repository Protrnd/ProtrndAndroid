package protrnd.com.ui.viewholder

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import protrnd.com.data.models.Notification
import protrnd.com.data.models.Profile
import protrnd.com.databinding.NotificationRvItemBinding
import protrnd.com.ui.getAgo
import protrnd.com.ui.setSpannableBold
import protrnd.com.ui.viewmodels.NotificationViewModel

class NotificationsViewHolder(val view: NotificationRvItemBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(
        notification: Notification,
        viewModel: NotificationViewModel,
        lifecycleOwner: LifecycleOwner
    ) {
        val ago = notification.time.getAgo()
        val message = "${notification.message}. "
        val displayText = message + ago
        view.notificationMessage.text = displayText.setSpannableBold(message)
//        this.itemView.autoDisposeScope.launch {
//            when (val profile = viewModel.getProfileById(notification.senderid)) {
//                is Resource.Success -> {
//                    if (profile.value.data.profileimg.isNotEmpty()) {
//                        val fullname = profile.value.data.fullname
//                        val message = "$fullname ${notification.message}"
//                        val displayText = message + ago
//                        view.notificationMessage.text = displayText.setSpannableBold(message)
//
//                        withContext(Dispatchers.Main) {
//                            loadImage(profile.value.data)
//                        }
//                    }
//                }
//                is Resource.Loading -> {}
//                else -> {
//                    val otherProfile = viewModel.getProfile(notification.senderid)
//                    otherProfile.asLiveData().observe(lifecycleOwner) {
//                        if (it != null) {
//                            loadImage(it)
//                        }
//                    }
//                }
//            }
//        }
    }

    private fun loadImage(profile: Profile) {
        Glide.with(view.root)
            .load(profile.profileimg)
            .circleCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(view.profileImage)
    }
}