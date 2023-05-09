package protrnd.com.ui.adapter.listener

import protrnd.com.data.models.Chat
import protrnd.com.ui.viewholder.ChatMessagesViewHolder

interface ChatRecyclerViewListener {
    fun loadChat(holder: ChatMessagesViewHolder, chatMessage: Chat)
}