package protrnd.com.ui.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.Notification
import protrnd.com.databinding.NotificationRvItemBinding
import protrnd.com.ui.isNetworkAvailable
import protrnd.com.ui.notification.NotificationViewModel
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.snackbar
import protrnd.com.ui.startAnimation
import protrnd.com.ui.viewholder.NotificationsViewHolder

class NotificationAdapter(
    val viewModel: NotificationViewModel,
    val activity: Activity,
    val lifecycleOwner: LifecycleOwner
) : PagingDataAdapter<Notification, NotificationsViewHolder>(NotificationComparator()) {

    class NotificationComparator : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NotificationsViewHolder(
        NotificationRvItemBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
    )

    override fun onBindViewHolder(holder: NotificationsViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item, viewModel, lifecycleOwner)
            holder.itemView.setOnClickListener {
                if (activity.isNetworkAvailable()) {
                    holder.itemView.autoDisposeScope.launch {
                        viewModel.setNotificationViewed(item.id)
                    }
                    val id: String
                    val name: String
                    val toA = if (item.type == "Post") {
                        name = "post_id"
                        id = item.item_id
                        PostActivity::class.java
                    } else {
                        name = "profile_id"
                        id = item.senderid
                        ProfileActivity::class.java
                    }
                    it.context.startActivity(Intent(it.context, toA).apply {
                        putExtra(name, id)
                    })
                    activity.startAnimation()
                } else {
                    holder.itemView.snackbar("Please check your network connection")
                }
            }
        }
    }
}