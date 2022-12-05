package protrnd.com.ui.adapter

import android.annotation.SuppressLint
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
import protrnd.com.ui.viewholder.NotificationsViewHolder

class NotificationAdapter(
    private var notifications: MutableList<Notification> = ArrayList(),
    val viewModel: NotificationViewModel
) : RecyclerView.Adapter<NotificationsViewHolder>() {
    fun addAll(result: List<Notification>) {
        val lastIndex = notifications.size - 1
        notifications.addAll(result)
        notifyItemRangeInserted(lastIndex, result.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(result: MutableList<Notification>) {
        notifications = result
        notifyDataSetChanged()
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
            if (notifications[position].type == "Post") {
                holder.itemView.autoDisposeScope.launch {
                    when (viewModel.setNotificationViewed(notifications[position].id)) {
                        else -> {}
                    }
                }
                it.context.startActivity(Intent(it.context, PostActivity::class.java).apply {
                    this.putExtra("post_id", notifications[position].item_id)
                })
            }
        }
    }

    override fun getItemCount(): Int = notifications.size
}