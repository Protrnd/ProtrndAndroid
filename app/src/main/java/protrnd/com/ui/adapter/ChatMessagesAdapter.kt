package protrnd.com.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import protrnd.com.data.models.Chat
import protrnd.com.data.models.Profile
import protrnd.com.databinding.ChatMessageLayoutBinding
import protrnd.com.ui.adapter.listener.ChatRecyclerViewListener
import protrnd.com.ui.viewholder.ChatMessagesViewHolder
import protrnd.com.ui.viewmodels.ChatViewModel

class ChatMessagesAdapter(
    val profile: Profile,
    var chatList: ArrayList<Chat>,
    val viewModel: ChatViewModel
) : RecyclerView.Adapter<ChatMessagesViewHolder>() {
    lateinit var chatRecyclerViewListener: ChatRecyclerViewListener

    class ChatComparator(val oldList: ArrayList<Chat>, val newList: ArrayList<Chat>) :
        DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.javaClass == newItem.javaClass
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    fun setData(data: ArrayList<Chat>) {
        chatList = data
    }

    fun setNewData(newChatList: ArrayList<Chat>) {
        val diffUtil = ChatComparator(chatList, newChatList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        chatList.clear()
        chatList.addAll(newChatList)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ChatMessagesViewHolder(
        ChatMessageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: ChatMessagesViewHolder, position: Int) {
        chatRecyclerViewListener.loadChat(holder, chatList[position])
    }

    fun loadChat(chatRecyclerViewListener: ChatRecyclerViewListener) {
        this.chatRecyclerViewListener = chatRecyclerViewListener
    }
}