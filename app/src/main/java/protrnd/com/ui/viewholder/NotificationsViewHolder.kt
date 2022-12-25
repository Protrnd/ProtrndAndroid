package protrnd.com.ui.viewholder

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.R
import protrnd.com.data.models.Notification
import protrnd.com.data.models.Profile
import protrnd.com.data.network.resource.Resource
import protrnd.com.databinding.NotificationRvItemBinding
import protrnd.com.ui.getAgo
import protrnd.com.ui.notification.NotificationViewModel

class NotificationsViewHolder(val view: NotificationRvItemBinding) :
    RecyclerView.ViewHolder(view.root) {
    fun bind(
        notification: Notification,
        viewModel: NotificationViewModel,
        lifecycleOwner: LifecycleOwner
    ) {
        val ago = notification.time.getAgo()
        val viewStatus = if (notification.viewed) "Viewed" else "Not Viewed"
        val timeText = "$ago \u2022 $viewStatus"
        view.notificationTime.text = timeText
        view.notificationMessage.text = notification.message
        if (!notification.viewed)
            view.root.setBackgroundColor(view.root.context.getColor(R.color.pink_light))
        this.itemView.autoDisposeScope.launch {
            when (val profile = viewModel.getProfileById(notification.senderid)) {
                is Resource.Success -> {
                    if (profile.value.data.profileimg.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            loadImage(profile.value.data)
                        }
                    }
                }
                is Resource.Loading -> {}
                else -> {
                    val otherProfile = viewModel.getProfile(notification.senderid)
                    otherProfile.asLiveData().observe(lifecycleOwner) {
                        if (it != null) {
                            loadImage(it)
                        }
                    }
                }
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