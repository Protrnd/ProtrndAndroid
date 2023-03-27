package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.databinding.RecentChatProfileLayoutBinding
import protrnd.com.ui.adapter.listener.ChatProfileListener
import protrnd.com.ui.viewholder.RecentChatProfileViewHolder

class RecentChatProfilesAdapter: RecyclerView.Adapter<RecentChatProfileViewHolder>() {
    lateinit var listener: ChatProfileListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecentChatProfileViewHolder(
        RecentChatProfileLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount(): Int = 6

    override fun onBindViewHolder(holder: RecentChatProfileViewHolder, position: Int) {
        holder.view.root.setOnClickListener {
            listener.click()
        }
    }

    fun clickListener(listener: ChatProfileListener) {
        this.listener = listener
    }
}