package protrnd.com.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.satoshun.coroutine.autodispose.view.autoDisposeScope
import kotlinx.coroutines.launch
import protrnd.com.data.models.Notification
import protrnd.com.databinding.NotificationRvItemBinding
import protrnd.com.ui.notification.NotificationViewModel
import protrnd.com.ui.post.PostActivity
import protrnd.com.ui.profile.ProfileActivity
import protrnd.com.ui.viewholder.NotificationsViewHolder

class NotificationAdapter(
    private var notifications: MutableList<Notification> = ArrayList(),
    val viewModel: NotificationViewModel
) : RecyclerView.Adapter<NotificationsViewHolder>() {

    fun addAll(result: List<Notification>) {
        val lastIndex = notifications.size - 1
        notifications.addAll(result)
        notifyInsertChange(lastIndex,result.size,notifications.size)
    }

    fun setList(result: MutableList<Notification>) {
        if (notifications.isEmpty()) {
            notifications = result
        } else {
            val previousSize = notifications.size
            notifications = result
            notifyItemRangeRemoved(0,previousSize)
            notifyItemRangeChanged(0,previousSize)
        }
        notifyInsertChange(0,result.size,result.size)
    }

    private fun notifyInsertChange(insertStart: Int, insertSize: Int, changedSize: Int) {
        notifyItemRangeInserted(insertStart, insertSize)
        notifyItemRangeChanged(0, changedSize)
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
        holder.bind(notifications[position], viewModel)
        holder.itemView.setOnClickListener {
            holder.itemView.autoDisposeScope.launch {
                when (viewModel.setNotificationViewed(notifications[position].id)) {
                    else -> {}
                }
            }
            if (notifications[position].type == "Post") {
                it.context.startActivity(Intent(it.context, PostActivity::class.java).apply {
                    this.putExtra("post_id", notifications[position].item_id)
                })
            } else if (notifications[position].type == "Profile") {
                it.context.startActivity(Intent(it.context, ProfileActivity::class.java).apply {
                    this.putExtra("profile_id", notifications[position].senderid)
                })
            }
        }
    }

    override fun getItemCount(): Int = notifications.size
}