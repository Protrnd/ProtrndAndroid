package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Conversation
import protrnd.com.data.models.Profile
import protrnd.com.databinding.RecentChatProfileLayoutBinding
import protrnd.com.ui.adapter.listener.ChatProfileListener
import protrnd.com.ui.viewholder.RecentChatProfileViewHolder
import protrnd.com.ui.viewmodels.ChatViewModel

class RecentChatProfilesAdapter(
    var conversations: List<Conversation>,
    val viewModel: ChatViewModel,
    val currentProfile: Profile
) : RecyclerView.Adapter<RecentChatProfileViewHolder>() {
    lateinit var listener: ChatProfileListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecentChatProfileViewHolder(
        RecentChatProfileLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = conversations.size

    override fun onBindViewHolder(holder: RecentChatProfileViewHolder, position: Int) {
        holder.bind(conversations[position], viewModel, currentProfile)
        holder.view.root.setOnClickListener {
            listener.click(conversations[position])
        }
    }

    fun clickListener(listener: ChatProfileListener) {
        this.listener = listener
    }
}