package protrnd.com.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import protrnd.com.R
import protrnd.com.data.models.Notification
import protrnd.com.data.network.Resource
import protrnd.com.databinding.NotificationRvItemBinding
import protrnd.com.ui.getAgo
import protrnd.com.ui.notification.NotificationViewModel

class NotificationsViewHolder(val view: NotificationRvItemBinding): RecyclerView.ViewHolder(view.root) {
    fun bind(notification: Notification, viewModel: NotificationViewModel) {
        val ago = notification.time.getAgo()
        val viewStatus = if (notification.viewed) "Viewed" else "Not Viewed"
        val timeText = "$ago \u2022 $viewStatus"
        view.notificationTime.text = timeText
        view.notificationMessage.text = notification.message
        if (!notification.viewed)
            view.root.setBackgroundColor(view.root.context.getColor(R.color.pink_light))
        this.itemView.autoDisposeScope.launch {
            when(val profile = viewModel.getProfileById(notification.senderid)) {
                is Resource.Success -> {
                    if (profile.value.data.profileimg.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            Glide.with(view.root)
                                .load(profile.value.data.profileimg)
                                .circleCrop()
                                .into(view.profileImage)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}